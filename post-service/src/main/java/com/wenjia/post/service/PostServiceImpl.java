package com.wenjia.post.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.PostDTO;
import com.wenjia.api.domain.po.Post;
import com.wenjia.api.domain.po.User;
import com.wenjia.api.domain.vo.PostVO;
import com.wenjia.api.domain.vo.ScrollResult;
import com.wenjia.api.domain.vo.ShopVO;
import com.wenjia.api.service.*;
import com.wenjia.common.constant.Constant;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.PostException;
import com.wenjia.post.annotation.PostCacheEvict;
import com.wenjia.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@DubboService
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper,Post> implements PostService {

    @DubboReference
    private UserService userService;
    @DubboReference
    private ThumbService thumbService;
    @DubboReference
    private ShopService shopService;
    @DubboReference
    private FavoriteService favoriteService;
    @DubboReference
    private FollowService followService;
    //todo 消息发送者
    //private PostProducer postProducer;
    private final RedisTemplate<String,Object> redisTemplate;

    @Override
    public void add(PostDTO postDTO) {
        //先检查数据
        checkFormat(postDTO);
        //进行类转换
        Post post = BeanUtil.copyProperties(postDTO,Post.class);
        //填充字段
        post.setCreateTime(LocalDateTime.now());
        //数据库插入操作
        save(post);
        //将动态添加到缓存中
        String key=RedisConstant.POST_KEY+post.getId();
        redisTemplate.opsForHash().putAll(key,BeanUtil.beanToMap(post));
        //todo 将动态添加到自己的发件箱里面
        //JedisUtil.zadd(RedisConstant.OUTBOX_KEY+post.getPublisherType()+":"+post.getPublisherId(),post.getId(),post.getCreateTime().toEpochSecond(ZoneOffset.ofHours(8)));
        //todo 发送异步消息进行动态的推送
        //postProducer.sendPostPushMessage(post);
    }

    @Override
    public ScrollResult<Post> queryPage(Long cursor, Integer offset) {
        Long userId = BaseContext.getCurrentId();
        ScrollResult<Post> scrollResult = new ScrollResult<>();

        String nullKey=RedisConstant.NULL_RESULT_KEY+userId+":"+cursor+":"+offset;
        //处理缓存穿透
        String withString = (String) redisTemplate.opsForValue().get(nullKey);
        if(withString!=null){
            scrollResult.setOffset(offset);
            scrollResult.setRecords(null);
            scrollResult.setCursor(cursor);
            return scrollResult;
        }

        //先查询用户收件箱缓存
        Set<Object> strings = redisTemplate.opsForZSet().reverseRangeByScore(RedisConstant.INBOX_KEY + userId, cursor,
                0L, offset, Constant.SCROLL_PAGE_SIZE);
        //根据收件箱里面的postId查找所有的post
        List<Post> postList=new ArrayList<>();
        //维护一下最小的时间戳和重复个数
        for(Object s:strings){
            Long postId = Long.parseLong(s.toString());
            Post post = getByIdWithCache(postId);
            //如果这个post并不存在也没事，因为这样postList大小会不足从而查询数据库中的动态
            if(post!=null) {
                //后台只有时分秒，没有毫秒，所以在push时只能传递秒的时间戳
                postList.add(post);
                long timestamp = post.getCreateTime().toEpochSecond(ZoneOffset.ofHours(8));
                if(timestamp == cursor){
                    offset++;
                }else{
                    cursor=timestamp;
                    offset=1;
                }
            }
        }
        //如果缓存中的数据不够
        if(postList.size()<3){
            //查询数据库中的冷数据
            LocalDateTime time = LocalDateTime.ofEpochSecond(cursor, 0, ZoneOffset.ofHours(8));
            //这个是多表查询
            //先查询用户关注商铺的列表
            List<Long> shopIds = followService.followWithShopIds(userId);
            //查询用户关注的商铺的动态
            List<Post> shops = lambdaQuery().le(Post::getCreateTime, time).eq(Post::getType, 1)
                    .in(Post::getPublisherId, shopIds).orderByDesc(Post::getCreateTime).last("limit " + (3 - postList.size()+offset)).list();
            //查询用户关注用户的列表
            List<Long> userIds = followService.followWithUserIds(userId);
            //查询用户关注的用户的动态
            List<Post> users = lambdaQuery().le(Post::getCreateTime, time).eq(Post::getType, 0)
                    .in(Post::getPublisherId, userIds).orderByDesc(Post::getCreateTime).last("limit " + (3 - postList.size()+offset)).list();
            //再使用stream流进行合并
            List<Post> posts = Stream.of(shops, users).flatMap(Collection::stream)//再看一下下面的那个排序写对了没
                    .sorted((a, b) -> a.getCreateTime().isBefore(b.getCreateTime()) ? -1 : 1)
                    .skip(offset)
                    .limit(3 - postList.size()).toList();
            for(Post post:posts){
                postList.add(post);
                long timestamp = post.getCreateTime().toEpochSecond(ZoneOffset.ofHours(8));
                if(timestamp == cursor){
                    offset++;
                }else{
                    cursor=timestamp;
                    offset=1;
                }
            }
        }
        //还需要解决查询的数据依旧为空的时候
        if(postList.isEmpty()){
            //缓存空值
            redisTemplate.opsForValue().set(nullKey,"空值",10L, TimeUnit.SECONDS);
        }
        scrollResult.setOffset(offset);
        scrollResult.setRecords(postList);
        scrollResult.setCursor(cursor);
        return scrollResult;
    }

    @Override
    public PostVO getById(Long postId) {
        Post post = getByIdWithCache(postId);
        if(post==null) throw new PostException("没有此动态");
        //判断当前用户有没有点赞或者收藏这个动态
        Long userId = BaseContext.getCurrentId();
        boolean hasThumb = thumbService.hasThumb(2, userId, postId);
        boolean hasFavorite = favoriteService.hasFavorite(userId, postId);
        //转化成postVO
        PostVO postVO = BeanUtil.copyProperties(post,PostVO.class);
        postVO.setHasFavorite(hasFavorite);
        postVO.setHasThumb(hasThumb);
        return postVO;
    }

    //todo 我感觉正确的点赞并不是给点赞单独搞一个模块，因为这样需要区分点赞的类型
    // 还有就是点赞模块点了之后，对应的目标模块依旧需要进行点赞数的增加
    // 所以应该是每个模块对应的数据库都有一个点赞表来记录当前模块的点赞

    @Override
    @PostCacheEvict
    public void incrCommentNumber(Long postId) {
        lambdaUpdate().setSql("comment_number=comment_number+1").eq(Post::getId,postId).update();
    }

    @Override
    @PostCacheEvict
    public void decrCommentNumber(Long postId) {
        lambdaUpdate().setSql("comment_number=comment_number-1")
                .eq(Post::getId,postId).ge(Post::getCommentNumber,0).update();
    }

    @Override
    @PostCacheEvict
    public void incrThumbNumber(Long postId) {
        lambdaUpdate().setSql("thumb_number=thumb_number+1").eq(Post::getId,postId).update();
    }

    @Override
    @PostCacheEvict
    public void decrThumbNumber(Long postId) {
        lambdaUpdate().setSql("thumb_number=thumb_number-1")
                .eq(Post::getId,postId).ge(Post::getThumbNumber,0).update();
    }

    @Override
    @PostCacheEvict
    public void incrFavoriteNumber(Long postId) {
        lambdaUpdate().setSql("favorite_number=favorite_number-1")
                .eq(Post::getId,postId).update();
    }

    @Override
    @PostCacheEvict
    public void decrFavoriteNumber(Long postId) {
        lambdaUpdate().setSql("favorite_number=favorite_number-1")
                .eq(Post::getId,postId).ge(Post::getFavoriteNumber,0).update();
    }

    /**
     * 使用缓存来查询单个post
     */
    private Post getByIdWithCache(Long postId){
        String key=RedisConstant.POST_KEY+postId;
        String nullKey=RedisConstant.NULL_RESULT_KEY+postId;
        //先处理缓存穿透
        if(redisTemplate.opsForValue().get(nullKey)!=null) return null;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Post post=BeanUtil.mapToBean(entries,Post.class,true, CopyOptions.create());
        if(post!=null) return post;
        //缓存中没有进行查询数据库
        Post selectById = lambdaQuery().eq(Post::getId,postId).one();
        //缓存数据库中的值
        Map<String, Object> map = BeanUtil.beanToMap(selectById);
        if(selectById!=null) {
            redisTemplate.opsForHash().putAll(key,map);
            redisTemplate.expire(key,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
        }
        else {
            redisTemplate.opsForValue().set(nullKey,"空值",10L,TimeUnit.SECONDS);
        }
        return selectById;
    }

    /**
     * 检查前端传递的值
     */
    private void checkFormat(PostDTO postDTO){
        //todo 还有就是说，别人是怎么来检验前端传递的值的
        // 不然每次都需要来判断一下用户是否存在有点蠢啊
        //获取字段
        Long publisherId = postDTO.getPublisherId();
        Integer publisherType = postDTO.getPublisherType();
        Long currentUserId = BaseContext.getCurrentId();
        Integer type = postDTO.getType();
        if(!(type==1||type==0)) throw new PostException("没有此类型的动态");
        //判断发布者是否存在
        if(publisherType.equals(0)){
            //用户
            User user = userService.lambdaQuery().eq(User::getId, publisherId).one();
            if(user==null){
                throw new PostException("不存在id为"+ publisherId +"的用户");
            }
            if(!Objects.equals(user.getId(), currentUserId))
                throw new PostException("用户状态异常,无法发布动态");
        }else if(publisherType.equals(1)){
            //商铺
            ShopVO shopVO = shopService.getByIdWithCache(publisherId);
            if(shopVO==null){
                throw new PostException("不存在id为"+ publisherId +"的商铺");
            }
            if(!Objects.equals(shopVO.getOwnerId(), currentUserId))
                throw new PostException("用户状态异常,无法为商铺发布动态");
        }else{
            throw new PostException("发布者类型有误");
        }
    }
}
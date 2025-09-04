package com.wenjia.common.constant;

import java.util.concurrent.TimeUnit;

public final class RedisConstant {
    private RedisConstant(){}
    public static final String LOCK_SHOP_KEY="shopLock:";
    public static final String LOCK_THUMB_KEY="thumbLock:";
    public static final String LOCK_FAVORITE_KEY="favoriteLock:";
    public static final String LOCK_FOLLOW_KEY="followLock:";
    public static final String SHOP_KEY="shop:";
    public static final String COUPON_KEY="coupon:";
    public static final String POST_KEY="post:";
    public static final String INBOX_KEY="inbox:";
    public static final String OUTBOX_KEY="outbox:";
    public static final String THUMB_KEY="thumb:";
    public static final String FAVORITE_KEY="favorite:";
    public static final String THUMB_COUNT_KEY="thumbCount:";
    public static final String COUPON_STOCK_KEY="stock:";
    public static final String SECKILL_KEY="seckill:";
    public static final String UPDATE_STOCK_KEY="update:stock:";
    public static final String ORDER_KEY="order:";
    public static final Long EXPIRE_TIME=30L;
    public static final TimeUnit TIME_UNIT=TimeUnit.MINUTES;
    public static final Long LOCK_EXPIRATION_TIME=100L;
    public static final String NULL_RESULT_KEY="nullResult:";
    public static final String FOLLOW_LIMIT_KEY="followLimit:";
    public static final String LOGIN_LIMIT_KEY="loginLimit:";
}

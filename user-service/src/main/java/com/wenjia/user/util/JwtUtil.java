package com.wenjia.user.util;




import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * jwt令牌工具类，封装创建令牌和解析令牌
 */
public class JwtUtil {
    /**
     * 创建jwt令牌
     */
    public static String createJwt(String secretKey, long duration, Map<String, Object> claims) {
        //将字符串密钥转换为安全的SecretKey对象
        SecretKey key= Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        //设置jwt的截至时间
        long expiration = System.currentTimeMillis() + duration;
        Date exp = new Date(expiration);
        //生成jwt
        return Jwts.builder()
                //设置Claim
                .claims(claims)
                //设置签名算法和签名的秘钥
                .signWith(key, Jwts.SIG.HS256)
                //设置过期时间
                .expiration(exp)
                .compact();
    }

    /**
     * 解析jwt令牌
     */
    public static Claims parseJWT(String secretKey, String token) {
        //将字符串密钥转换为安全的SecretKey对象
        SecretKey key= Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                //设置验证密钥
                .verifyWith(key)
                .build()
                //解析令牌
                .parseSignedClaims(token)
                //获得负载
                .getPayload();
    }
}

package com.securtiy_with_redis.security_with_redis.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String,Object> redisTemplate;


    public void saveToken(String token, String username){
        redisTemplate.opsForValue().set(token,username, Duration.ofMinutes(5)); // token 5 dakika geçerli
    }

    public void refToken(String refToken, String username){
        redisTemplate.opsForValue().set(refToken,username, Duration.ofHours(24)); // refresh token 24 saat geçerli
    }

    public String getIdByToken(String token){
        return (String) redisTemplate.opsForValue().get(token);
    }

    public boolean isTokenValid(String refToken){
        return redisTemplate.opsForValue().get(refToken) != null;
    }

    public void invalidateToken(String token){
        redisTemplate.opsForValue().getAndDelete(token);
    }


}

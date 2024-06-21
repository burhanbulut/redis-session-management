package com.securtiy_with_redis.security_with_redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Token {
    private String token;
    private String username;
    private String refreshToken;


}

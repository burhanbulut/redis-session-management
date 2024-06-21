package com.securtiy_with_redis.security_with_redis.dto;

import lombok.Data;

@Data
public class UserCreateDto {
    private String username;
    private String password;
}

package com.securtiy_with_redis.security_with_redis.services;

import com.securtiy_with_redis.security_with_redis.dto.UserCreateDto;
import com.securtiy_with_redis.security_with_redis.dto.UserLoginDto;
import com.securtiy_with_redis.security_with_redis.model.Token;
import com.securtiy_with_redis.security_with_redis.model.User;
import com.securtiy_with_redis.security_with_redis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public User saveUser(UserCreateDto user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return null;
        }
        User newUser = new User();
        String hashedPassword = hashPassword(user.getPassword());
        newUser.setPassword(hashedPassword);
        newUser.setUsername(user.getUsername());
        return userRepository.save(newUser);
    }

    public Token loginUser(UserLoginDto user) {
        User userFromDb = (User) userRepository.findByUsername(user.getUsername()).orElse(null);
        if (userFromDb != null) {
            String hashedPassword = hashPassword(user.getPassword());
            if (userFromDb.getPassword().equals(hashedPassword)) {
                String token = generateToken();
                String refToken = generateToken();
                tokenService.refToken(refToken, userFromDb.getUsername());
                tokenService.saveToken(token, userFromDb.getUsername());
                return new Token(token, userFromDb.getUsername(), refToken);
            }
        }

        return null;
    }

    public Token refreshToken(Token oldToken){
        boolean isValid = tokenService.isTokenValid(oldToken.getRefreshToken());
        if(isValid){
            String token = generateToken();
            String newRefToken = generateToken();
            Token newToken = new Token(token, oldToken.getUsername(), newRefToken);
            newToken.setToken(token);
            newToken.setRefreshToken(newRefToken);
            tokenService.saveToken(newToken.getToken(), oldToken.getUsername());
            tokenService.refToken(newToken.getRefreshToken(), oldToken.getUsername());
            tokenService.invalidateToken(oldToken.getToken());
            tokenService.invalidateToken(oldToken.getRefreshToken());
            return newToken;
        }
        return null;
    }

    private String generateToken(){
        return UUID.randomUUID().toString();
    }

    private String hashPassword(String password){
        return DigestUtils.sha256Hex(password);
    }

    public boolean verifyToken(String token){
        return tokenService.getIdByToken(token) != null;
    }
}

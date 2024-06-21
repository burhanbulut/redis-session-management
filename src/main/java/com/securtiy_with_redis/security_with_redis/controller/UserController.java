package com.securtiy_with_redis.security_with_redis.controller;

import com.securtiy_with_redis.security_with_redis.dto.UserCreateDto;
import com.securtiy_with_redis.security_with_redis.dto.UserLoginDto;
import com.securtiy_with_redis.security_with_redis.model.Token;
import com.securtiy_with_redis.security_with_redis.model.User;
import com.securtiy_with_redis.security_with_redis.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.saveUser(userCreateDto));
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.ok(userService.loginUser(userLoginDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Token> refresh(@RequestBody Token token  ) {
        Token newToken = userService.refreshToken(token);
        return newToken != null ? ResponseEntity.ok(userService.refreshToken(newToken)) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken(@RequestParam String token){
        boolean isValid = userService.verifyToken(token);
        return isValid ? ResponseEntity.ok("Token is valid") : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not valid");
    }
}

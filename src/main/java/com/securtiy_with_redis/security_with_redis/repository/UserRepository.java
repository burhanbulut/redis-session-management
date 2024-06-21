package com.securtiy_with_redis.security_with_redis.repository;

import com.securtiy_with_redis.security_with_redis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<Object> findByUsername(String username);
}

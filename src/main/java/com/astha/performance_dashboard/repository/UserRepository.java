package com.astha.performance_dashboard.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.astha.performance_dashboard.model.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
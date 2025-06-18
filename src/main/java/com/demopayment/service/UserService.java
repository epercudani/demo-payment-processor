package com.demopayment.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import com.demopayment.client.UserClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// UserService handles user data management and caching
// Provides functionality to retrieve and store user information across different storage systems

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final UserClient userClient;
    
    private static final ObjectMapper jacksonMapper = new ObjectMapper();
    
    private static final int CACHE_TTL = 3600;
    
    public UserService() {
        this.userClient = new UserClient();
    }
    
    public Map<String, Object> getUserData(String userId) {
        logger.info("Getting user data for: {}", userId);
        
        // Check Redis cache first
        String cachedData = (String) redisTemplate.opsForValue().get("user:" + userId);
        if (cachedData != null) {
            try {
                return jacksonMapper.readValue(cachedData, Map.class);
            } catch (Exception e) {
                logger.error("Failed to deserialize cached user data", e);
            }
        }
        
        // Get user data from external service
        Map<String, Object> userData = userClient.getUserData(userId);
        
        // Update Redis cache
        try {
            redisTemplate.opsForValue().set("user:" + userId, jacksonMapper.writeValueAsString(userData), CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to cache user data", e);
        }
        
        return userData;
    }
} 
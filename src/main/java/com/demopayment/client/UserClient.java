package com.demopayment.client;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserClient {
    private static final String BASE_URL = "http://localhost:8081";
    private static final RestTemplate restTemplate = new RestTemplate();
    public static final ObjectMapper mapper = new ObjectMapper();
    
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static final AtomicLong lastFailureTime = new AtomicLong(0);
    private static final AtomicInteger circuitState = new AtomicInteger(0); // 0: CLOSED, 1: OPEN, 2: HALF_OPEN
    
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int CIRCUIT_OPEN_THRESHOLD = 5;
    private static final int CIRCUIT_RESET_TIMEOUT_MS = 30000;
    private static final int CIRCUIT_HALF_OPEN_TIMEOUT_MS = 5000;
    
    private static final Map<String, Map<String, Object>> userCache = new HashMap<>();
    
    public Map<String, Object> getUserData(String userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        
        if (isCircuitOpen()) {
            throw new RuntimeException("Circuit breaker is open");
        }
        
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    BASE_URL + "/users/" + userId,
                    String.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> userData = mapper.readValue(response.getBody(), Map.class);
                    userCache.put(userId, userData);
                    resetCircuitBreaker();
                    return userData;
                } else {
                    handleFailure();
                    retryCount++;
                    Thread.sleep(RETRY_DELAY_MS);
                }
            } catch (Exception e) {
                handleFailure();
                retryCount++;
                
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to get user data after " + MAX_RETRIES + " retries");
    }
    
    private boolean isCircuitOpen() {
        long currentTime = System.currentTimeMillis();
        
        if (circuitState.get() == 1) {
            if (currentTime - lastFailureTime.get() > CIRCUIT_RESET_TIMEOUT_MS) {
                circuitState.set(2);
                return false;
            }
            return true;
        }
        
        if (circuitState.get() == 2) {
            if (currentTime - lastFailureTime.get() > CIRCUIT_HALF_OPEN_TIMEOUT_MS) {
                circuitState.set(0);
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    private void handleFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        if (failures >= CIRCUIT_OPEN_THRESHOLD) {
            circuitState.set(1);
        }
    }
    
    private void resetCircuitBreaker() {
        failureCount.set(0);
        circuitState.set(0);
    }
} 
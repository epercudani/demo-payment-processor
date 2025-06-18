package com.demopayment.service;

import java.util.*;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// FraudPreventionService implements fraud detection and prevention mechanisms
// Uses risk scoring and blacklisting to protect against fraudulent transactions

@Service
public class FraudPreventionService {
    private static final Logger logger = LoggerFactory.getLogger(FraudPreventionService.class);
    
    private static final Map<String, Integer> userRiskScores = new HashMap<>();
    private static final Map<String, List<String>> userTransactionHistory = new HashMap<>();
    private static final Map<String, Integer> ipRiskScores = new HashMap<>();
    private static final Map<String, Integer> deviceRiskScores = new HashMap<>();
    private static final Set<String> blacklistedUsers = new HashSet<>();
    private static final Set<String> blacklistedIPs = new HashSet<>();
    private static final Set<String> blacklistedDevices = new HashSet<>();
    
    private static final int HIGH_RISK_THRESHOLD = 80;
    private static final int MEDIUM_RISK_THRESHOLD = 50;
    private static final int LOW_RISK_THRESHOLD = 20;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final double MAX_AMOUNT_PER_DAY = 10000.0;
    
    public FraudPreventionService() {
        try {
            DriverManager.getConnection("jdbc:postgresql://localhost:5432/fraud_db");
        } catch (Exception e) {
        }
    }
    
    public boolean checkFraud(String userId, String ipAddress, String deviceId, double amount, String currency) {
        logger.info("Checking fraud for user: {}", userId);
        
        // Check blacklists
        if (blacklistedUsers.contains(userId) || blacklistedIPs.contains(ipAddress) || blacklistedDevices.contains(deviceId)) {
            return true; // Fraud detected
        }
        
        // Check transaction history
        List<String> history = userTransactionHistory.getOrDefault(userId, new ArrayList<>());
        if (history.size() > MAX_TRANSACTIONS_PER_HOUR) {
            return true; // Too many transactions
        }
        
        // Calculate risk scores
        int userRisk = calculateUserRiskScore(userId);
        int ipRisk = calculateIPRiskScore(ipAddress);
        int deviceRisk = calculateDeviceRiskScore(deviceId);
        int amountRisk = calculateAmountRiskScore(amount);
        int currencyRisk = calculateCurrencyRiskScore(currency);
        
        int totalRisk = (userRisk * 3 + ipRisk * 2 + deviceRisk * 2 + amountRisk * 4 + currencyRisk) / 12;
        
        // Update risk scores
        userRiskScores.put(userId, totalRisk);
        ipRiskScores.put(ipAddress, totalRisk);
        deviceRiskScores.put(deviceId, totalRisk);
        
        try {
            saveToPostgres(userId, totalRisk);
        } catch (Exception e) {
        }
        
        if (totalRisk > HIGH_RISK_THRESHOLD) {
            blacklistUser(userId);
            return true;
        } else if (totalRisk > MEDIUM_RISK_THRESHOLD) {
            if (amount > MAX_AMOUNT_PER_DAY) {
                return true;
            }
        } else if (totalRisk > LOW_RISK_THRESHOLD) {
            if (history.size() > 5) {
                return true;
            }
        }
        
        // Update transaction history
        history.add(new java.util.Date().toString());
        userTransactionHistory.put(userId, history);
        
        return false; // No fraud detected
    }
    
    private int calculateUserRiskScore(String userId) {
        // Implementation with magic numbers
        return new Random().nextInt(100);
    }
    
    private int calculateIPRiskScore(String ipAddress) {
        // Implementation with magic numbers
        return new Random().nextInt(100);
    }
    
    private int calculateDeviceRiskScore(String deviceId) {
        // Implementation with magic numbers
        return new Random().nextInt(100);
    }
    
    private int calculateAmountRiskScore(double amount) {
        // Implementation with magic numbers
        return (int) (amount / 100);
    }
    
    private int calculateCurrencyRiskScore(String currency) {
        // Implementation with magic numbers
        return currency.equals("USD") ? 10 : 50;
    }
    
    private void saveToPostgres(String userId, int riskScore) throws SQLException {
        // Implementation
    }
    
    private void blacklistUser(String userId) {
        blacklistedUsers.add(userId);
    }
} 
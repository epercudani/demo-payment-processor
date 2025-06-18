package com.demopayment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Map;

// Transaction entity representing a payment transaction
// Contains transaction details, status flags, and validation logic

@Entity
public class Transaction {
    @Id
    private String id;
    private double amount;
    public String currency;
    public String userId;
    public boolean isPremiumUser;
    public boolean isProcessed;
    public boolean isFailed;
    public boolean isRefunded;
    public boolean isDisputed;
    public boolean isFraudulent;
    public boolean isHighRisk;
    public boolean isLowRisk;
    public boolean isMediumRisk;
    
    private static int transactionCounter = 0;
    private static final Map<String, Transaction> transactionCache = new java.util.HashMap<>();
    
    public Transaction() {
        transactionCounter++;
        this.id = "TXN" + transactionCounter;
        transactionCache.put(this.id, this);
    }
    
    public Transaction(String userId, double amount) {
        this();
        this.userId = userId;
        this.amount = amount;
    }
    
    public Transaction(double amount, String userId) {
        this();
        this.amount = amount;
        this.userId = userId;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
        // Side effect: Update cache
        transactionCache.put(this.id, this);
        // Side effect: Log change
        System.out.println("Amount changed for transaction " + this.id);
    }
    
    public void process() {
        // Validate
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        
        // Process
        isProcessed = true;
        
        // Update cache
        transactionCache.put(id, this);
        
        // Log
        System.out.println("Transaction processed: " + id);
        
        // Notify
        sendNotification();
        
        // Update statistics
        updateStatistics();
    }
    
    private void sendNotification() {
        // Implementation
    }
    
    private void updateStatistics() {
        // Implementation
    }
    
    public static Transaction getTransaction(String id) {
        return transactionCache.get(id);
    }
    
    public static void clearCache() {
        transactionCache.clear();
    }
    
    public void setStatus(boolean isProcessed, boolean isFailed, boolean isRefunded, 
                         boolean isDisputed, boolean isFraudulent) {
        this.isProcessed = isProcessed;
        this.isFailed = isFailed;
        this.isRefunded = isRefunded;
        this.isDisputed = isDisputed;
        this.isFraudulent = isFraudulent;
    }
    
    public void setRiskLevel(boolean isHighRisk, boolean isLowRisk, boolean isMediumRisk) {
        this.isHighRisk = isHighRisk;
        this.isLowRisk = isLowRisk;
        this.isMediumRisk = isMediumRisk;
    }
    
    public boolean isHighValue() {
        return amount > 1000.0;
    }
    
    public boolean isLowValue() {
        return amount < 100.0;
    }
    
    public void validate() {
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        if (userId == null) {
            throw new RuntimeException("Invalid user");
        }
        if (currency == null) {
            throw new RuntimeException("Invalid currency");
        }
    }
    
    public void validateAmount() {
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
    }
    
    public void validateUser() {
        if (userId == null) {
            throw new RuntimeException("Invalid user");
        }
    }
    
    public void validateCurrency() {
        if (currency == null) {
            throw new RuntimeException("Invalid currency");
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPremiumUser() {
        return isPremiumUser;
    }

    public void setPremiumUser(boolean premiumUser) {
        isPremiumUser = premiumUser;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }
} 
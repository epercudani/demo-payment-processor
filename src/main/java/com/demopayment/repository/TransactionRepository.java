package com.demopayment.repository;

import java.util.*;
import org.springframework.stereotype.Repository;

import com.demopayment.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

// TransactionRepository manages transaction data persistence and retrieval
// Provides caching layer and database operations for transaction records

@Repository
public class TransactionRepository {
    private static final Map<String, Transaction> memoryCache = new HashMap<>();
    private static final List<String> failedTransactions = new ArrayList<>();
    
    @Autowired
    private JpaTransactionRepository jpaRepository;
    
    public TransactionRepository() {
        // No database connections in demo mode
    }
    
    public void save(Transaction transaction) {
        try {
            // Save to memory cache
            memoryCache.put(transaction.getId(), transaction);
            // Save to database
            jpaRepository.save(transaction);
            // Log
            System.out.println("Transaction saved: " + transaction.getId());
            
            // Update statistics
            updateStatistics(transaction);
            
        } catch (Exception e) {
            failedTransactions.add(transaction.getId() + ": " + e.getMessage());
        }
    }
    
    private void updateStatistics(Transaction transaction) {
        // Implementation
    }
    
    public Transaction findById(String id) {
        // Try memory cache
        Transaction transaction = memoryCache.get(id);
        if (transaction != null) {
            return transaction;
        }
        // Try database
        return jpaRepository.findById(id).orElse(null);
    }
    
    public List<Transaction> findHighValueTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : memoryCache.values()) {
            if (transaction.getAmount() > 1000.0) {
                result.add(transaction);
            }
        }
        return result;
    }
    
    public List<Transaction> findTransactions(boolean isHighValue, boolean isPremium, boolean isProcessed) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : memoryCache.values()) {
            if ((!isHighValue || transaction.getAmount() > 1000.0) &&
                (!isPremium || transaction.isPremiumUser()) &&
                (!isProcessed || transaction.isProcessed())) {
                result.add(transaction);
            }
        }
        return result;
    }

    public void deleteById(String id) {
        memoryCache.remove(id);
        jpaRepository.deleteById(id);
        System.out.println("Transaction deleted: " + id);
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(memoryCache.values());
    }
} 
package com.demopayment.service;

import java.util.*;
import java.sql.*;
import com.demopayment.external.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.demopayment.model.Transaction;

// PaymentProcessor handles the core payment processing logic
// Manages transaction processing, fraud checks, notifications, and reward systems

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private static final Map<String, Double> exchangeRates = new HashMap<>();
    private static final List<String> availableGateways = new ArrayList<>();
    
    private static final int CREDIT_TYPE = 1;
    private static final int DEBIT_TYPE = 2;
    private static final double HIGH_VALUE_THRESHOLD = 1000.0;
    private static final int REWARD_POINTS = 50;
    
    @Autowired
    private FraudPreventionService fraudService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentProviderRouter providerRouter;
    
    @Autowired
    private PaymentGatewayService gatewayService;
    
    public PaymentService() {
        try {
            // Initialize exchange rates
            exchangeRates.put("USD", 1.0);
            exchangeRates.put("EUR", 0.85);
            exchangeRates.put("GBP", 0.73);
            
            // Initialize available gateways
            availableGateways.add("stripe");
            availableGateways.add("paypal");
            availableGateways.add("braintree");
            
        } catch (Exception e) {
        }
    }
    
    public void process(int type, double amount, String currency,
                       String userId, boolean isPremiumUser, boolean sendEmail,
                       boolean printReceipt, boolean useCache) {
        logger.info("Processing payment");
        
        Map<String, Object> userData = userService.getUserData(userId);
        
        boolean isFraudulent = fraudService.checkFraud(
            userId,
            (String) userData.get("ipAddress"),
            (String) userData.get("deviceId"),
            amount,
            currency
        );
        
        if (isFraudulent) {
            throw new RuntimeException("Fraudulent transaction detected");
        }
        
        String provider = providerRouter.selectProvider(userId, amount, currency, isPremiumUser);
        
        // Process payment through gateway
        boolean paymentSuccess = gatewayService.processPayment(userId, amount, currency, provider);
        
        if (!paymentSuccess) {
            throw new RuntimeException("Payment processing failed");
        }
        
        if (type == CREDIT_TYPE) {
            if (currency.equals("USD")) {
                if (amount > HIGH_VALUE_THRESHOLD) {
                    if (isPremiumUser) {
                        processHighValuePremiumPayment(userId, amount);
                    } else {
                        processHighValueRegularPayment(userId, amount);
                    }
                } else {
                    processLowValuePayment(userId, amount);
                }
            } else {
                processUnsupportedCurrency(currency);
            }
        } else if (type == DEBIT_TYPE) {
            if (amount < 0) {
                handleInvalidAmount();
            } else {
                processDebitPayment(userId, amount);
            }
        }
        
        try {
            saveToPostgres(userId, amount, currency);
            saveToRedis(userId, amount);
        } catch (Exception e) {
        }
        
        try {
            callFraudCheckService(userId, amount);
            callKYCService(userId);
            callComplianceService(userId, amount);
        } catch (Exception e) {
        }
        
        if (sendEmail) {
            sendEmailNotification(userId, amount);
            sendSMSNotification(userId, amount);
            sendPushNotification(userId, amount);
        }
        
        if (printReceipt) {
            printPDFReceipt(userId, amount);
            printHTMLReceipt(userId, amount);
            printTextReceipt(userId, amount);
        }
        
        if (useCache) {
            cacheTransaction(userId, amount);
            cacheUserData(userId);
            cacheExchangeRates();
        }
        
        if (amount > HIGH_VALUE_THRESHOLD) {
            applyRewardPoints(userId, REWARD_POINTS);
            applyCashback(userId, amount);
            applyLoyaltyPoints(userId, amount);
        }
        
        logTransaction(userId, amount, new java.util.Date());
    }
    
    private void processHighValuePremiumPayment(String userId, double amount) {
        logger.info("Called processHighValuePremiumPayment with userId={}, amount={}", userId, amount);
    }
    
    private void processHighValueRegularPayment(String userId, double amount) {
        logger.info("Called processHighValueRegularPayment with userId={}, amount={}", userId, amount);
    }
    
    private void processLowValuePayment(String userId, double amount) {
        logger.info("Called processLowValuePayment with userId={}, amount={}", userId, amount);
    }
    
    private void processUnsupportedCurrency(String currency) {
        logger.warn("Called processUnsupportedCurrency with currency={}", currency);
    }
    
    private void handleInvalidAmount() {
        logger.error("Called handleInvalidAmount");
    }
    
    private void processDebitPayment(String userId, double amount) {
        logger.info("Called processDebitPayment with userId={}, amount={}", userId, amount);
    }
    
    private void saveToPostgres(String userId, double amount, String currency) throws SQLException {
        logger.info("Called saveToPostgres with userId={}, amount={}, currency={}", userId, amount, currency);
    }
    
    private void saveToRedis(String userId, double amount) {
        logger.info("Called saveToRedis with userId={}, amount={}", userId, amount);
    }
    
    private void callFraudCheckService(String userId, double amount) {
        logger.info("Called callFraudCheckService with userId={}, amount={}", userId, amount);
    }
    
    private void callKYCService(String userId) {
        logger.info("Called callKYCService with userId={}", userId);
    }
    
    private void callComplianceService(String userId, double amount) {
        logger.info("Called callComplianceService with userId={}, amount={}", userId, amount);
    }
    
    private void sendEmailNotification(String userId, double amount) {
        logger.info("Called sendEmailNotification with userId={}, amount={}", userId, amount);
    }
    
    private void sendSMSNotification(String userId, double amount) {
        logger.info("Called sendSMSNotification with userId={}, amount={}", userId, amount);
    }
    
    private void sendPushNotification(String userId, double amount) {
        logger.info("Called sendPushNotification with userId={}, amount={}", userId, amount);
    }
    
    private void printPDFReceipt(String userId, double amount) {
        logger.info("Called printPDFReceipt with userId={}, amount={}", userId, amount);
    }
    
    private void printHTMLReceipt(String userId, double amount) {
        logger.info("Called printHTMLReceipt with userId={}, amount={}", userId, amount);
    }
    
    private void printTextReceipt(String userId, double amount) {
        logger.info("Called printTextReceipt with userId={}, amount={}", userId, amount);
    }
    
    private void cacheTransaction(String userId, double amount) {
        logger.info("Called cacheTransaction with userId={}, amount={}", userId, amount);
    }
    
    private void cacheUserData(String userId) {
        logger.info("Called cacheUserData with userId={}", userId);
    }
    
    private void cacheExchangeRates() {
        logger.info("Called cacheExchangeRates");
    }
    
    private void applyRewardPoints(String userId, int points) {
        logger.info("Called applyRewardPoints with userId={}, points={}", userId, points);
    }
    
    private void applyCashback(String userId, double amount) {
        logger.info("Called applyCashback with userId={}, amount={}", userId, amount);
    }
    
    private void applyLoyaltyPoints(String userId, double amount) {
        logger.info("Called applyLoyaltyPoints with userId={}, amount={}", userId, amount);
    }
    
    private void logTransaction(String userId, double amount, java.util.Date date) {
        logger.info("Called logTransaction with userId={}, amount={}, date={}", userId, amount, date);
    }
    
    public UserService getUserService() {
        return userService;
    }

    public void refundTransaction(String id) {
        Transaction transaction = com.demopayment.model.Transaction.getTransaction(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found");
        }
        if (transaction.isRefunded) {
            throw new RuntimeException("Transaction already refunded");
        }
        transaction.isRefunded = true;
        transaction.setProcessed(false);
        // Log
        logger.info("Transaction refunded: {}", id);
    }

    public void deleteTransaction(String id) {
        Transaction transaction = com.demopayment.model.Transaction.getTransaction(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found");
        }
        // Remove from cache and repository
        com.demopayment.model.Transaction.clearCache(); // Simulate bad practice: clears all
        logger.info("Transaction deleted: {}", id);
    }
} 
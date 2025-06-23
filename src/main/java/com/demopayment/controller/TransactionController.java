package com.demopayment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demopayment.external.PaymentGatewayService;
import com.demopayment.model.Transaction;
import com.demopayment.repository.TransactionRepository;
import com.demopayment.service.PaymentService;

import java.util.Random;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final PaymentService paymentService;
    private final TransactionRepository repository;
    private final PaymentGatewayService gatewayService;
    private final Random random = new Random();

    @Autowired
    public TransactionController(PaymentService paymentService,
                               TransactionRepository repository,
                               PaymentGatewayService gatewayService) {
        this.paymentService = paymentService;
        this.repository = repository;
        this.gatewayService = gatewayService;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody TransactionRequest request) {
        try {
            boolean isPremiumUser = paymentService.getUserService().isPremiumUser(request.getUserId());

            paymentService.process(
                1, // Credit payment
                request.getAmount(),
                request.getCurrency(),
                request.getUserId(),
                isPremiumUser,
                true, // Send email
                true, // Print receipt
                true  // Use cache
            );

            // Simulate external payment gateway call
            String[] gateways = {"stripe", "paypal", "braintree"};
            String gateway = gateways[random.nextInt(gateways.length)];
            boolean success = gatewayService.processPayment(
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                gateway
            );

            if (!success) {
                return ResponseEntity.badRequest().body("Payment processing failed");
            }

            // Save transaction
            Transaction transaction = new Transaction(request.getUserId(), request.getAmount());
            transaction.currency = request.getCurrency();
            transaction.isPremiumUser = isPremiumUser;
            repository.save(transaction);

            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable String id) {
        try {
            Transaction transaction = repository.findById(id);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving transaction: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundTransaction(@PathVariable String id) {
        try {
            paymentService.refundTransaction(id);
            Transaction transaction = repository.findById(id);
            repository.save(transaction); // Persist refund status
            return ResponseEntity.ok("Transaction refunded");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error refunding transaction: " + e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listTransactions() {
        try {
            return ResponseEntity.ok(repository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error listing transactions: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable String id) {
        try {
            repository.deleteById(id);
            paymentService.deleteTransaction(id);
            return ResponseEntity.ok("Transaction deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting transaction: " + e.getMessage());
        }
    }
}

class TransactionRequest {
    private String userId;
    private double amount;
    private String currency;

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
} 
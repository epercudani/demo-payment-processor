package com.demopayment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demopayment.model.Transaction;

public interface JpaTransactionRepository extends JpaRepository<Transaction, String> {
} 
package com.demopayment.service;

import com.demopayment.external.PaymentGatewayService;
import com.demopayment.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private FraudPreventionService fraudService;
    @Mock
    private UserService userService;
    @Mock
    private PaymentProviderRouter providerRouter;
    @Mock
    private PaymentGatewayService gatewayService;

    @InjectMocks
    private PaymentService paymentService;

    private final String userId = "user123";
    private final double amount = 1200.0;
    private final String currency = "USD";

    @BeforeEach
    void setup() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("ipAddress", "127.0.0.1");
        userData.put("deviceId", "device1");
        lenient().when(userService.getUserData(userId)).thenReturn(userData);
        lenient().when(fraudService.checkFraud(anyString(), anyString(), anyString(), anyDouble(), anyString())).thenReturn(false);
        lenient().when(providerRouter.selectProvider(anyString(), anyDouble(), anyString(), anyBoolean())).thenReturn("stripe");
        lenient().when(gatewayService.processPayment(anyString(), anyDouble(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    void testProcess_CreditType_Success() {
        assertDoesNotThrow(() -> paymentService.process(1, amount, currency, userId, true, false, false, false));
    }

    @Test
    void testProcess_DebitType_Success() {
        assertDoesNotThrow(() -> paymentService.process(2, 100, currency, userId, false, false, false, false));
    }

    @Test
    void testProcess_Fraudulent_ThrowsException() {
        when(fraudService.checkFraud(anyString(), anyString(), anyString(), anyDouble(), anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> paymentService.process(1, amount, currency, userId, true, false, false, false));
    }

    @Test
    void testRefundTransaction_Success() {
        Transaction tx = mock(Transaction.class);
        tx.isRefunded = false;
        try (MockedStatic<Transaction> mocked = mockStatic(Transaction.class)) {
            mocked.when(() -> Transaction.getTransaction("tx1")).thenReturn(tx);
            assertDoesNotThrow(() -> paymentService.refundTransaction("tx1"));
            verify(tx).setProcessed(false);
        }
    }

    @Test
    void testRefundTransaction_AlreadyRefunded_ThrowsException() {
        Transaction tx = mock(Transaction.class);
        tx.isRefunded = true;
        try (MockedStatic<Transaction> mocked = mockStatic(Transaction.class)) {
            mocked.when(() -> Transaction.getTransaction("tx1")).thenReturn(tx);
            assertThrows(RuntimeException.class, () -> paymentService.refundTransaction("tx1"));
        }
    }

    @Test
    void testRefundTransaction_NotFound_ThrowsException() {
        try (MockedStatic<Transaction> mocked = mockStatic(Transaction.class)) {
            mocked.when(() -> Transaction.getTransaction("tx1")).thenReturn(null);
            assertThrows(RuntimeException.class, () -> paymentService.refundTransaction("tx1"));
        }
    }

    @Test
    void testDeleteTransaction_Success() {
        Transaction tx = mock(Transaction.class);
        try (MockedStatic<Transaction> mocked = mockStatic(Transaction.class)) {
            mocked.when(() -> Transaction.getTransaction("tx1")).thenReturn(tx);
            assertDoesNotThrow(() -> paymentService.deleteTransaction("tx1"));
        }
    }

    @Test
    void testDeleteTransaction_NotFound_ThrowsException() {
        try (MockedStatic<Transaction> mocked = mockStatic(Transaction.class)) {
            mocked.when(() -> Transaction.getTransaction("tx1")).thenReturn(null);
            assertThrows(RuntimeException.class, () -> paymentService.deleteTransaction("tx1"));
        }
    }
} 
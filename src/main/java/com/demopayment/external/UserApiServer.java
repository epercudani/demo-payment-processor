package com.demopayment.external;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// UserApiServer provides a mock HTTP server for user data
// Simulates user service behavior with random delays and errors for testing

public class UserApiServer {
    private static final int PORT = 8081;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();
    private static HttpServer server;
    
    // Initialize some users
    private static final Map<String, Map<String, Object>> initializedUsers = new HashMap<>();
    
    static {
        // Merchant user
        Map<String, Object> merchant = new HashMap<>();
        merchant.put("id", "1001");
        merchant.put("name", "Tech Store Inc");
        merchant.put("email", "payments@techstore.com");
        merchant.put("phone", "+1-555-0123");
        merchant.put("type", "merchant");
        merchant.put("address", generateMockAddress("456 Business Ave", "San Francisco", "CA", "94105"));
        merchant.put("createdAt", "2023-01-15T10:00:00Z");
        merchant.put("isActive", true);
        merchant.put("isVerified", true);
        merchant.put("balance", 25000.00);
        merchant.put("currency", "USD");
        merchant.put("preferences", generateMockPreferences());
        initializedUsers.put("1001", merchant);
        
        // Payer user
        Map<String, Object> payer = new HashMap<>();
        payer.put("id", "2001");
        payer.put("name", "John Smith");
        payer.put("email", "john.smith@email.com");
        payer.put("phone", "+1-555-0124");
        payer.put("type", "payer");
        payer.put("address", generateMockAddress("789 Home St", "New York", "NY", "10001"));
        payer.put("createdAt", "2023-02-20T14:30:00Z");
        payer.put("isActive", true);
        payer.put("isVerified", true);
        payer.put("balance", 1500.00);
        payer.put("currency", "USD");
        payer.put("preferences", generateMockPreferences());
        initializedUsers.put("2001", payer);
        
        // Another merchant
        Map<String, Object> merchant2 = new HashMap<>();
        merchant2.put("id", "1002");
        merchant2.put("name", "Fashion Boutique");
        merchant2.put("email", "sales@fashionboutique.com");
        merchant2.put("phone", "+1-555-0125");
        merchant2.put("type", "merchant");
        merchant2.put("address", generateMockAddress("321 Style Blvd", "Los Angeles", "CA", "90001"));
        merchant2.put("createdAt", "2023-03-10T09:15:00Z");
        merchant2.put("isActive", true);
        merchant2.put("isVerified", true);
        merchant2.put("balance", 18000.00);
        merchant2.put("currency", "USD");
        merchant2.put("preferences", generateMockPreferences());
        initializedUsers.put("1002", merchant2);
    }
    
    private static class InternalUserService {
        private static final int MAX_RETRIES = 3;
        private static final int TIMEOUT_MS = 1000;
        
        public static Map<String, Object> getUserById(String userId) throws ServiceException {
            int retries = 0;
            while (retries < MAX_RETRIES) {
                try {
                    simulateNetworkLatency();
                    if (shouldSimulateError()) {
                        throw new ServiceException("Internal service error");
                    }
                    return initializedUsers.get(userId);
                } catch (ServiceException e) {
                    retries++;
                    if (retries == MAX_RETRIES) {
                        throw e;
                    }
                }
            }
            return null;
        }
        
        public static Map<String, Object> getUserByUuid(UUID uuid) throws ServiceException {
            int retries = 0;
            while (retries < MAX_RETRIES) {
                try {
                    simulateNetworkLatency();
                    if (shouldSimulateError()) {
                        throw new ServiceException("Internal service error");
                    }
                    boolean isOdd = (uuid.getLeastSignificantBits() & 1) == 1;
                    return initializedUsers.get(isOdd ? "2001" : "1001");
                } catch (ServiceException e) {
                    retries++;
                    if (retries == MAX_RETRIES) {
                        throw e;
                    }
                }
            }
            return null;
        }
        
        private static void simulateNetworkLatency() throws ServiceException {
            try {
                Thread.sleep(random.nextInt(TIMEOUT_MS));
            } catch (InterruptedException e) {
                throw new ServiceException("Request timeout");
            }
        }
        
        private static boolean shouldSimulateError() {
            return random.nextInt(100) < 10;
        }
    }
    
    private static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
    
    public static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Create context for GET /users/{id}
        server.createContext("/users/", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.close();
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);
            
            try {
                Map<String, Object> userData = InternalUserService.getUserById(userId);
                if (userData == null) {
                    exchange.sendResponseHeaders(404, 0); // Not Found
                    exchange.close();
                    return;
                }
                
                // Send response
                String response = mapper.writeValueAsString(userData);
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (ServiceException e) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });

        // Create context for GET /v2/users/{uuid}
        server.createContext("/v2/users/", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.close();
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String uuidStr = path.substring(path.lastIndexOf("/") + 1);
            
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Object> userData = InternalUserService.getUserByUuid(uuid);
                
                // Send response
                String response = mapper.writeValueAsString(userData);
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(400, 0); // Bad Request - Invalid UUID
                exchange.close();
            } catch (ServiceException e) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });
        
        server.setExecutor(null); // Use default executor
        server.start();
        System.out.println("User API Server started on port " + PORT);
    }
    
    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("User API Server stopped");
        }
    }
    
    private static Map<String, Object> generateMockAddress(String street, String city, String state, String zip) {
        Map<String, Object> address = new HashMap<>();
        address.put("street", street);
        address.put("city", city);
        address.put("state", state);
        address.put("zip", zip);
        address.put("country", "USA");
        return address;
    }
    
    private static Map<String, Object> generateMockPreferences() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "en");
        preferences.put("timezone", "UTC");
        preferences.put("notifications", true);
        preferences.put("currency", "USD");
        return preferences;
    }
    
    public static void main(String[] args) throws IOException {
        start();
    }
} 
package org.example.dynamic_bus_schedule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dynamic_bus_schedule.model.AuthResponse;
import org.example.dynamic_bus_schedule.model.User;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AuthService {
    private static final String BASE_URL = "http://localhost:3000/api";
    private static AuthService instance;
    private String currentToken;
    private User currentUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Private constructor for singleton
    private AuthService() {
        System.out.println("AuthService initialized with base URL: " + BASE_URL);
    }

    // Singleton instance getter
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // Reset instance (for logout)
    public static void resetInstance() {
        instance = null;
    }

    public AuthResponse login(String email, String password) {
        try {
            String response = makeApiCall("POST", "/auth/login",
                    String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password));

            AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

            if (authResponse.isSuccess()) {
                currentToken = authResponse.getToken();
                currentUser = authResponse.getUser();
                System.out.println("Token stored: " + (currentToken != null ? "***" : "null"));
            }

            return authResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Login failed: " + e.getMessage(), null, null);
        }
    }

    public AuthResponse register(String name, String email, String password, String phone, String role) {
        try {
            String requestBody = String.format(
                    "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
                    name, email, password, phone, role
            );

            String response = makeApiCall("POST", "/auth/register", requestBody);
            AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

            if (authResponse.isSuccess()) {
                currentToken = authResponse.getToken();
                currentUser = authResponse.getUser();
                System.out.println("Token stored after registration: " + (currentToken != null ? "***" : "null"));
            }

            return authResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Registration failed: " + e.getMessage(), null, null);
        }
    }

    // Add role checking methods
    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isOperator() {
        return currentUser != null && "driver".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isClient() {
        return currentUser != null && "client".equalsIgnoreCase(currentUser.getRole());
    }

    public String makeApiCall(String method, String endpoint) throws Exception {
        return makeApiCall(method, endpoint, null);
    }

    public String makeApiCall(String method, String endpoint, String requestBody) throws Exception {
        System.out.println("Making API call: " + method + " " + BASE_URL + endpoint);
        System.out.println("Current token: " + (currentToken != null ? "***" : "null"));

        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Add authorization header if token exists
            if (currentToken != null && !currentToken.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + currentToken);
                System.out.println("Authorization header added");
            } else {
                System.out.println("No token available for authorization");
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // For POST/PUT requests with body
            if (requestBody != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String responseString = response.toString();
            System.out.println("Response: " + responseString);

            if (responseCode >= 200 && responseCode < 300) {
                return responseString;
            } else {
                throw new Exception("API call failed with code " + responseCode + ": " + responseString);
            }

        } finally {
            connection.disconnect();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public void logout() {
        currentToken = null;
        currentUser = null;
        System.out.println("User logged out - token cleared");
    }

    // Method to get dashboard stats
    public String getDashboardStats() throws Exception {
        return makeApiCall("GET", "/admin/dashboard/stats");
    }

    // Method to get recent activities
    public String getRecentActivities() throws Exception {
        return makeApiCall("GET", "/admin/dashboard/activities");
    }
}
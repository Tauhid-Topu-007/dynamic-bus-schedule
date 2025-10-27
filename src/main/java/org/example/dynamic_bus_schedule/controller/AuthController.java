package org.example.dynamic_bus_schedule.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dynamic_bus_schedule.service.AuthService;
import org.example.dynamic_bus_schedule.model.AuthResponse;
import org.example.dynamic_bus_schedule.util.AlertUtil;

import java.io.IOException;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private AuthService authService;

    public AuthController() {
        this.authService = AuthService.getInstance(); // Use singleton
    }

    @FXML
    public void initialize() {
        // Set enter key listener
        passwordField.setOnAction(event -> handleLogin());

        // Set button action
        loginButton.setOnAction(event -> handleLogin());

        System.out.println("AuthController initialized");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showErrorAlert("Login Error", "Please enter both username and password.");
            return;
        }

        // Show loading state
        loginButton.setText("Signing In...");
        loginButton.setDisable(true);

        // Perform login in background thread
        new Thread(() -> {
            try {
                AuthResponse response = authService.login(username, password);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    loginButton.setText("Login");
                    loginButton.setDisable(false);

                    if (response.isSuccess()) {
                        try {
                            redirectToDashboard(response);
                        } catch (IOException e) {
                            AlertUtil.showErrorAlert("Navigation Error", "Unable to load dashboard: " + e.getMessage());
                        }
                    } else {
                        AlertUtil.showErrorAlert("Login Failed", response.getMessage());
                    }
                });
            } catch (Exception e) {
                // Handle any unexpected errors
                javafx.application.Platform.runLater(() -> {
                    loginButton.setText("Login");
                    loginButton.setDisable(false);
                    AlertUtil.showErrorAlert("Login Error", "An unexpected error occurred: " + e.getMessage());
                });
            }
        }).start();
    }

    private void redirectToDashboard(AuthResponse response) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root;

        // Determine dashboard based on user role
        if (authService.isAdmin()) {
            // Load admin dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dynamic_bus_schedule/fxml/admin-dashboard.fxml"));
            root = loader.load();

            // Pass user data to admin dashboard controller
            AdminDashboardController adminController = loader.getController();
            if (adminController != null && response.getUser() != null) {
                System.out.println("Admin dashboard loaded for user: " + response.getUser().getName());
            }

            stage.setTitle("Admin Dashboard - Bus Schedule System");
        } else if (authService.isOperator()) {
            // Load driver dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dynamic_bus_schedule/fxml/driver-dashboard.fxml"));
            root = loader.load();
            stage.setTitle("Driver Dashboard - Bus Schedule System");
        } else {
            // Load client dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/dynamic_bus_schedule/fxml/client-dashboard.fxml"));
            root = loader.load();
            stage.setTitle("Client Dashboard - Bus Schedule System");
        }

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.centerOnScreen();

        System.out.println("Redirected to dashboard successfully");
    }

    // Optional: Method to clear form
    public void clearForm() {
        usernameField.clear();
        passwordField.clear();
        usernameField.requestFocus();
    }
}
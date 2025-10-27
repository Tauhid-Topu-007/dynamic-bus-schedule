package org.example.dynamic_bus_schedule.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.service.AuthService;
import org.example.dynamic_bus_schedule.model.AuthResponse;

public class LoginController {

    // Login form elements
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Hyperlink showRegisterLink;
    @FXML private Label loginErrorLabel;

    // Register form elements
    @FXML private TextField registerName;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private TextField registerPhone;
    @FXML private ComboBox<String> registerRole;
    @FXML private Button registerButton;
    @FXML private Hyperlink showLoginLink;
    @FXML private Label registerErrorLabel;

    // Containers
    @FXML private StackPane loginContainer;
    @FXML private StackPane registerContainer;
    @FXML private ScrollPane mainScrollPane;

    private Main mainApp;
    private AuthService authService;

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized - Starting setup...");
        authService = AuthService.getInstance(); // Use singleton

        // Setup role combobox
        if (registerRole != null) {
            registerRole.getItems().addAll("Admin", "Client", "Driver");
            registerRole.setValue("Client");
            System.out.println("Role combobox initialized");
        }

        // Setup navigation between login and register forms
        if (showRegisterLink != null) {
            showRegisterLink.setOnAction(e -> showRegisterForm());
        }

        if (showLoginLink != null) {
            showLoginLink.setOnAction(e -> showLoginForm());
        }

        // Setup button actions
        if (loginButton != null) {
            loginButton.setOnAction(e -> handleLogin());
        }

        if (registerButton != null) {
            registerButton.setOnAction(e -> handleRegister());
        }

        // Setup enter key listeners
        setupEnterKeyListeners();

        // Setup scroll pane
        setupScrollPane();

        // Make sure login form is visible by default
        if (loginContainer != null && registerContainer != null) {
            loginContainer.setVisible(true);
            registerContainer.setVisible(false);
        }

        System.out.println("LoginController setup completed");
    }

    private void setupEnterKeyListeners() {
        // Login form enter key
        if (loginPassword != null) {
            loginPassword.setOnAction(e -> handleLogin());
        }

        // Register form enter key
        if (registerPassword != null) {
            registerPassword.setOnAction(e -> handleRegister());
        }
    }

    private void setupScrollPane() {
        if (mainScrollPane != null) {
            // Configure scroll pane for better user experience
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setFitToHeight(true);
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            mainScrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        System.out.println("MainApp set in controller");
    }

    private void showRegisterForm() {
        System.out.println("Showing register form");
        if (loginContainer != null && registerContainer != null) {
            loginContainer.setVisible(false);
            registerContainer.setVisible(true);
            clearErrors();
            scrollToTop();
        }
    }

    private void showLoginForm() {
        System.out.println("Showing login form");
        if (loginContainer != null && registerContainer != null) {
            registerContainer.setVisible(false);
            loginContainer.setVisible(true);
            clearErrors();
            scrollToTop();
        }
    }

    private void scrollToTop() {
        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(0);
        }
    }

    private void handleLogin() {
        System.out.println("Login attempted");
        String email = loginEmail != null ? loginEmail.getText().trim() : "";
        String password = loginPassword != null ? loginPassword.getText() : "";

        if (!validateLoginForm(email, password)) {
            return;
        }

        // Show loading state
        loginButton.setText("Signing In...");
        loginButton.setDisable(true);
        clearErrors();

        // Call the backend API
        new Thread(() -> {
            try {
                AuthResponse response = authService.login(email, password);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    loginButton.setText("Sign In");
                    loginButton.setDisable(false);

                    if (response.isSuccess()) {
                        showLoginSuccess("Login successful! Welcome " + response.getUser().getName());
                        System.out.println("Login successful for user: " + response.getUser().getName());
                        System.out.println("User role: " + response.getUser().getRole());
                        System.out.println("Token: " + (response.getToken() != null ? "***" : "null"));

                        // Redirect based on role
                        redirectAfterLogin(response.getUser().getRole());
                    } else {
                        showLoginError("Login failed: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loginButton.setText("Sign In");
                    loginButton.setDisable(false);
                    showLoginError("Login error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleRegister() {
        System.out.println("Register attempted");
        String name = registerName != null ? registerName.getText().trim() : "";
        String email = registerEmail != null ? registerEmail.getText().trim() : "";
        String password = registerPassword != null ? registerPassword.getText() : "";
        String phone = registerPhone != null ? registerPhone.getText().trim() : "";
        String role = registerRole != null ? registerRole.getValue() : "";

        if (!validateRegisterForm(name, email, password, phone)) {
            return;
        }

        // Show loading state
        registerButton.setText("Creating Account...");
        registerButton.setDisable(true);
        clearErrors();

        // Call the backend API
        new Thread(() -> {
            try {
                AuthResponse response = authService.register(name, email, password, phone, role.toLowerCase());

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    registerButton.setText("Create Account");
                    registerButton.setDisable(false);

                    if (response.isSuccess()) {
                        showRegisterSuccess("Registration successful! Please login with your new account.");
                        // Clear form and switch to login
                        clearRegisterForm();
                        // Auto-switch to login after delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                javafx.application.Platform.runLater(() -> {
                                    showLoginForm();
                                    scrollToTop();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        showRegisterError("Registration failed: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    registerButton.setText("Create Account");
                    registerButton.setDisable(false);
                    showRegisterError("Registration error: " + e.getMessage());
                });
            }
        }).start();
    }

    private boolean validateLoginForm(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter both email and password");
            return false;
        }

        if (!isValidEmail(email)) {
            showLoginError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private boolean validateRegisterForm(String name, String email, String password, String phone) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all required fields");
            return false;
        }

        if (!isValidEmail(email)) {
            showRegisterError("Please enter a valid email address");
            return false;
        }

        if (password.length() < 6) {
            showRegisterError("Password must be at least 6 characters long");
            return false;
        }

        if (!phone.isEmpty() && !isValidPhone(phone)) {
            showRegisterError("Please enter a valid phone number (10-15 digits)");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^[+]?[0-9]{10,15}$";
        return phone.matches(phoneRegex);
    }

    private void redirectAfterLogin(String role) {
        // Add a small delay before redirecting to show success message
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 second delay

                javafx.application.Platform.runLater(() -> {
                    if ("admin".equalsIgnoreCase(role)) {
                        Main.showAdminDashboard();
                    } else if ("driver".equalsIgnoreCase(role)) {
                        Main.showDriverDashboard();
                    } else {
                        Main.showClientDashboard();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clearRegisterForm() {
        if (registerName != null) registerName.clear();
        if (registerEmail != null) registerEmail.clear();
        if (registerPassword != null) registerPassword.clear();
        if (registerPhone != null) registerPhone.clear();
        if (registerRole != null) registerRole.setValue("Client");
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            loginErrorLabel.setVisible(true);
            scrollToTop();
        }
    }

    private void showLoginSuccess(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
            loginErrorLabel.setVisible(true);
            scrollToTop();
        }
    }

    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            registerErrorLabel.setVisible(true);
            scrollToTop();
        }
    }

    private void showRegisterSuccess(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
            registerErrorLabel.setVisible(true);
            scrollToTop();
        }
    }

    private void clearErrors() {
        System.out.println("Clearing errors");
        if (loginErrorLabel != null) {
            loginErrorLabel.setVisible(false);
        }
        if (registerErrorLabel != null) {
            registerErrorLabel.setVisible(false);
        }
    }
}
package org.example.dynamic_bus_schedule.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.layout.GridPane;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.service.AuthService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserManagementController {

    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label driverCountLabel;
    @FXML private Label clientCountLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> dateColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button refreshButton;

    private AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    // User data class
    public static class User {
        private final int id;
        private final String name;
        private final String email;
        private final String phone;
        private final String role;
        private final String createdAt;

        public User(int id, String name, String email, String phone, String role, String createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public String getCreatedAt() { return createdAt; }
    }

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        setupTableColumns();
        loadUserProfile();
        setupFilters();
        loadUsersData();

        System.out.println("UserManagementController initialized successfully");
    }

    private void setupTableColumns() {
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getName()));
        }
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getEmail()));
        }
        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getPhone()));
        }
        if (roleColumn != null) {
            roleColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getRole()));
        }
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getCreatedAt()));
        }
    }

    private void setupFilters() {
        if (roleFilter != null) {
            roleFilter.getItems().addAll("All", "Admin", "Driver", "Client");
            roleFilter.setValue("All");

            // Add listener for filter changes
            roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void loadUserProfile() {
        try {
            var currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                welcomeLabel.setText("User Management - " + currentUser.getName());
                userRoleLabel.setText("Administrator");
            } else {
                welcomeLabel.setText("User Management");
                userRoleLabel.setText("Admin Panel");
            }
        } catch (Exception e) {
            e.printStackTrace();
            welcomeLabel.setText("User Management");
            userRoleLabel.setText("Admin Panel");
        }
    }

    private void loadUsersData() {
        new Thread(() -> {
            try {
                System.out.println("Loading users data from API...");

                // Make API call to get all users
                String response = authService.makeApiCall("GET", "/users");
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    JsonNode usersData = jsonResponse.get("data");
                    ObservableList<User> users = FXCollections.observableArrayList();

                    for (JsonNode userNode : usersData) {
                        try {
                            int id = userNode.get("id").asInt();
                            String name = userNode.get("name").asText();
                            String email = userNode.get("email").asText();
                            String phone = userNode.has("phone") ? userNode.get("phone").asText() : "N/A";
                            String role = userNode.get("role").asText();
                            String createdAt = formatDate(userNode.get("created_at").asText());

                            users.add(new User(id, name, email, phone, role, createdAt));
                        } catch (Exception e) {
                            System.err.println("Error parsing user data: " + e.getMessage());
                        }
                    }

                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        allUsers = users;
                        usersTable.setItems(allUsers);
                        updateUserStats();
                        applyFilters(); // Apply any active filters
                    });

                    System.out.println("Successfully loaded " + users.size() + " users from API");

                } else {
                    throw new Exception("API returned error: " + jsonResponse.get("message").asText());
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to mock data if API fails
                javafx.application.Platform.runLater(() -> {
                    loadMockUsersData();
                    showErrorAlert("Failed to load users data: " + e.getMessage());
                });
            }
        }).start();
    }

    private String formatDate(String dateString) {
        try {
            if (dateString.contains("T")) {
                // ISO format: 2024-01-15T10:30:00.000Z
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = isoFormat.parse(dateString);
                return dateFormat.format(date);
            } else {
                // Simple format
                return dateString;
            }
        } catch (Exception e) {
            return dateString;
        }
    }

    private void loadMockUsersData() {
        ObservableList<User> users = FXCollections.observableArrayList(
                new User(1, "System Admin", "admin@bus.com", "+1234567890", "Admin", "Jan 01, 2024"),
                new User(2, "John Driver", "driver.john@bus.com", "+8801712345678", "Driver", "Jan 15, 2024"),
                new User(3, "Sarah Client", "sarah.client@bus.com", "+8801812345678", "Client", "Jan 16, 2024"),
                new User(4, "Mike Operator", "mike.driver@bus.com", "+8801912345678", "Driver", "Jan 17, 2024"),
                new User(5, "Emma Traveler", "emma.client@bus.com", "+8801612345678", "Client", "Jan 18, 2024")
        );

        allUsers = users;
        usersTable.setItems(allUsers);
        updateUserStats();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String roleFilterValue = roleFilter.getValue();

        ObservableList<User> filteredUsers = FXCollections.observableArrayList();

        for (User user : allUsers) {
            boolean matchesSearch = searchText.isEmpty() ||
                    user.getName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);

            boolean matchesRole = roleFilterValue.equals("All") ||
                    user.getRole().equalsIgnoreCase(roleFilterValue);

            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }

        usersTable.setItems(filteredUsers);
        updateUserStats();
    }

    private void updateUserStats() {
        int totalUsers = usersTable.getItems().size();
        int adminCount = 0;
        int driverCount = 0;
        int clientCount = 0;

        for (User user : usersTable.getItems()) {
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    adminCount++;
                    break;
                case "driver":
                    driverCount++;
                    break;
                case "client":
                    clientCount++;
                    break;
            }
        }

        // Update labels if they exist
        if (totalUsersLabel != null) {
            totalUsersLabel.setText(totalUsers + " Users");
        }
        if (adminCountLabel != null) {
            adminCountLabel.setText(adminCount + "");
        }
        if (driverCountLabel != null) {
            driverCountLabel.setText(driverCount + "");
        }
        if (clientCountLabel != null) {
            clientCountLabel.setText(clientCount + "");
        }
    }

    @FXML
    private void handleAddUser() {
        System.out.println("Add user clicked");

        // Create a dialog for adding new user
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "Driver", "Client");
        roleComboBox.setValue("Client");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a user when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showErrorAlert("Please fill in all required fields");
                    return null;
                }
                return new User(0, nameField.getText(), emailField.getText(),
                        phoneField.getText(), roleComboBox.getValue(), "Just now");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            // Here you would make an API call to create the user
            createNewUser(newUser);
        });
    }

    private void createNewUser(User newUser) {
        new Thread(() -> {
            try {
                String requestBody = String.format(
                        "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
                        newUser.getName(), newUser.getEmail(), "password123", // In real app, use actual password
                        newUser.getPhone(), newUser.getRole().toLowerCase()
                );

                String response = authService.makeApiCall("POST", "/auth/register", requestBody);
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    javafx.application.Platform.runLater(() -> {
                        showInfoAlert("Success", "User created successfully!");
                        loadUsersData(); // Refresh the user list
                    });
                } else {
                    throw new Exception("API error: " + jsonResponse.get("message").asText());
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Failed to create user: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            System.out.println("Edit user: " + selectedUser.getName());

            // Create edit dialog
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Edit User");
            dialog.setHeaderText("Edit user: " + selectedUser.getName());

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nameField = new TextField(selectedUser.getName());
            TextField emailField = new TextField(selectedUser.getEmail());
            TextField phoneField = new TextField(selectedUser.getPhone());
            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("Admin", "Driver", "Client");
            roleComboBox.setValue(selectedUser.getRole());

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(emailField, 1, 1);
            grid.add(new Label("Phone:"), 0, 2);
            grid.add(phoneField, 1, 2);
            grid.add(new Label("Role:"), 0, 3);
            grid.add(roleComboBox, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return new User(selectedUser.getId(), nameField.getText(), emailField.getText(),
                            phoneField.getText(), roleComboBox.getValue(), selectedUser.getCreatedAt());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedUser -> {
                updateUser(selectedUser, updatedUser);
            });

        } else {
            showErrorAlert("Please select a user to edit.");
        }
    }

    private void updateUser(User oldUser, User updatedUser) {
        new Thread(() -> {
            try {
                String requestBody = String.format(
                        "{\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"role\":\"%s\"}",
                        updatedUser.getName(), updatedUser.getEmail(),
                        updatedUser.getPhone(), updatedUser.getRole().toLowerCase()
                );

                String response = authService.makeApiCall("PUT", "/users/" + oldUser.getId(), requestBody);
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    javafx.application.Platform.runLater(() -> {
                        showInfoAlert("Success", "User updated successfully!");
                        loadUsersData(); // Refresh the user list
                    });
                } else {
                    throw new Exception("API error: " + jsonResponse.get("message").asText());
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Failed to update user: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Show confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete User");
            confirmation.setHeaderText("Delete User Confirmation");
            confirmation.setContentText("Are you sure you want to delete user:\n" +
                    selectedUser.getName() + " (" + selectedUser.getEmail() + ")?\n\nThis action cannot be undone.");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteUser(selectedUser);
                }
            });
        } else {
            showErrorAlert("Please select a user to delete.");
        }
    }

    private void deleteUser(User user) {
        new Thread(() -> {
            try {
                String response = authService.makeApiCall("DELETE", "/users/" + user.getId());
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    javafx.application.Platform.runLater(() -> {
                        showInfoAlert("Success", "User deleted successfully!");
                        loadUsersData(); // Refresh the user list
                    });
                } else {
                    throw new Exception("API error: " + jsonResponse.get("message").asText());
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Failed to delete user: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing user data...");
        loadUsersData();
    }

    @FXML
    private void handleBackToDashboard() {
        System.out.println("Returning to Admin Dashboard");
        Main.showAdminDashboard();
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        try {
            authService.logout();
            AuthService.resetInstance();
            Main.showLoginScene();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Logout failed: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Operation Failed");
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Could not show error alert: " + e.getMessage());
            }
        });
    }

    private void showInfoAlert(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Could not show info alert: " + e.getMessage());
            }
        });
    }
}
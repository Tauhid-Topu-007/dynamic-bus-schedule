package org.example.dynamic_bus_schedule.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.service.AuthService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserManagementController {

    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
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
                // For now, use mock data. You can replace this with API calls later
                ObservableList<User> users = FXCollections.observableArrayList(
                        new User(1, "System Admin", "admin@bus.com", "+1234567890", "Admin", "Jan 01, 2024"),
                        new User(2, "John Driver", "driver.john@bus.com", "+8801712345678", "Driver", "Jan 15, 2024"),
                        new User(3, "Sarah Client", "sarah.client@bus.com", "+8801812345678", "Client", "Jan 16, 2024"),
                        new User(4, "Mike Operator", "mike.driver@bus.com", "+8801912345678", "Driver", "Jan 17, 2024"),
                        new User(5, "Emma Traveler", "emma.client@bus.com", "+8801612345678", "Client", "Jan 18, 2024"),
                        new User(6, "David Manager", "david.admin@bus.com", "+8801512345678", "Admin", "Jan 19, 2024"),
                        new User(7, "Lisa Passenger", "lisa.client@bus.com", "+8801412345678", "Client", "Jan 20, 2024"),
                        new User(8, "Robert Chauffeur", "robert.driver@bus.com", "+8801312345678", "Driver", "Jan 21, 2024")
                );

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    if (usersTable != null) {
                        usersTable.setItems(users);
                        updateUserStats(users.size());
                    }
                });

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

    private void loadMockUsersData() {
        ObservableList<User> users = FXCollections.observableArrayList(
                new User(1, "System Admin", "admin@bus.com", "+1234567890", "Admin", "Jan 01, 2024"),
                new User(2, "John Driver", "driver.john@bus.com", "+8801712345678", "Driver", "Jan 15, 2024"),
                new User(3, "Sarah Client", "sarah.client@bus.com", "+8801812345678", "Client", "Jan 16, 2024"),
                new User(4, "Mike Operator", "mike.driver@bus.com", "+8801912345678", "Driver", "Jan 17, 2024"),
                new User(5, "Emma Traveler", "emma.client@bus.com", "+8801612345678", "Client", "Jan 18, 2024")
        );

        if (usersTable != null) {
            usersTable.setItems(users);
            updateUserStats(users.size());
        }
    }

    private void applyFilters() {
        // This would filter the table based on search and role filters
        // For now, we'll just log the filter values
        String searchText = searchField.getText();
        String roleFilterValue = roleFilter.getValue();

        System.out.println("Applying filters - Search: " + searchText + ", Role: " + roleFilterValue);

        // In a real implementation, you would filter the table data here
    }

    private void updateUserStats(int totalUsers) {
        // You can update statistics labels here if you add them to the UI
        System.out.println("Total users: " + totalUsers);
    }

    @FXML
    private void handleAddUser() {
        System.out.println("Add user clicked");
        showInfoAlert("Add User", "Add new user functionality coming soon!\n\nThis will allow you to:\n• Create new user accounts\n• Assign roles and permissions\n• Set up user profiles");
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            System.out.println("Edit user: " + selectedUser.getName());
            showInfoAlert("Edit User", "Editing user: " + selectedUser.getName() +
                    "\n\nEmail: " + selectedUser.getEmail() +
                    "\nRole: " + selectedUser.getRole() +
                    "\n\nEdit functionality coming soon!");
        } else {
            showErrorAlert("Please select a user to edit.");
        }
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
                    System.out.println("Delete user: " + selectedUser.getName());
                    showInfoAlert("User Deleted", "User " + selectedUser.getName() + " has been deleted successfully.\n\nNote: This is a demo. In production, this would remove the user from the database.");
                }
            });
        } else {
            showErrorAlert("Please select a user to delete.");
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing user data...");
        loadUsersData();
        showInfoAlert("Refresh", "User data refreshed successfully!");
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
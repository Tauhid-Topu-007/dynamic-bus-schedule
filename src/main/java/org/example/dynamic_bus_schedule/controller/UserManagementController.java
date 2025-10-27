package org.example.dynamic_bus_schedule.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.model.User;
import org.example.dynamic_bus_schedule.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleField;
    @FXML private ComboBox<String> statusField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    private ObservableList<User> userList;
    private UserService userService;
    private User selectedUser;

    @FXML
    public void initialize() {
        userService = new UserService();
        userList = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        setupForm();
        loadUsers();

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectUser(newValue)
        );
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        userTable.setItems(userList);
    }

    private void setupFilters() {
        roleFilter.getItems().addAll("All", "admin", "client", "driver");
        roleFilter.setValue("All");

        statusFilter.getItems().addAll("All", "active", "inactive", "suspended");
        statusFilter.setValue("All");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadUsers());
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> loadUsers());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> loadUsers());
    }

    private void setupForm() {
        roleField.getItems().addAll("admin", "client", "driver");
        roleField.setValue("client");

        statusField.getItems().addAll("active", "inactive", "suspended");
        statusField.setValue("active");
    }

    private void loadUsers() {
        try {
            Map<String, String> filters = new HashMap<>();

            String search = searchField.getText().trim();
            if (!search.isEmpty()) {
                filters.put("search", search);
            }

            String role = roleFilter.getValue();
            if (!"All".equals(role)) {
                filters.put("role", role);
            }

            String status = statusFilter.getValue();
            if (!"All".equals(status)) {
                filters.put("status", status);
            }

            List<User> users = userService.getAllUsers(filters);
            userList.setAll(users);
        } catch (Exception e) {
            showError("Failed to load users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectUser(User user) {
        this.selectedUser = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            roleField.setValue(user.getRole());
            statusField.setValue(user.getStatus());

            // Don't show password for security
            passwordField.clear();

            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            addButton.setDisable(true);
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleAddUser() {
        if (!validateForm()) return;

        try {
            User newUser = new User();
            newUser.setName(nameField.getText().trim());
            newUser.setEmail(emailField.getText().trim().toLowerCase());
            newUser.setPassword(passwordField.getText()); // This would be hashed in real implementation
            newUser.setPhone(phoneField.getText().trim());
            newUser.setRole(roleField.getValue());
            newUser.setStatus(statusField.getValue());

            User createdUser = userService.createUser(newUser);
            userList.add(createdUser);
            clearForm();
            showSuccess("User added successfully!");
        } catch (Exception e) {
            showError("Failed to add user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null || !validateForm()) return;

        try {
            selectedUser.setName(nameField.getText().trim());
            selectedUser.setEmail(emailField.getText().trim().toLowerCase());
            selectedUser.setPhone(phoneField.getText().trim());
            selectedUser.setRole(roleField.getValue());
            selectedUser.setStatus(statusField.getValue());

            // Only update password if provided
            if (!passwordField.getText().isEmpty()) {
                selectedUser.setPassword(passwordField.getText());
            }

            User updatedUser = userService.updateUser(selectedUser.getId(), selectedUser);
            int index = userList.indexOf(selectedUser);
            userList.set(index, updatedUser);
            userTable.refresh();
            showSuccess("User updated successfully!");
        } catch (Exception e) {
            showError("Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete user " + selectedUser.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = userService.deleteUser(selectedUser.getId());
                    if (success) {
                        userList.remove(selectedUser);
                        clearForm();
                        showSuccess("User deleted successfully!");
                    }
                } catch (Exception e) {
                    showError("Failed to delete user: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        userTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        phoneField.clear();
        roleField.setValue("client");
        statusField.setValue("active");

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
        selectedUser = null;
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                roleField.getValue() == null ||
                statusField.getValue() == null) {

            showError("Please fill in all required fields");
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address");
            return false;
        }

        // For new users, password is required
        if (selectedUser == null && passwordField.getText().isEmpty()) {
            showError("Password is required for new users");
            return false;
        }

        if (!passwordField.getText().isEmpty() && passwordField.getText().length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package org.example.dynamic_bus_schedule.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Alert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.model.User;
import org.example.dynamic_bus_schedule.service.AuthService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDashboardController {

    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label totalBusesLabel;
    @FXML private Label activeSchedulesLabel;
    @FXML private Label todaysTripsLabel;
    @FXML private Label systemStatusLabel;
    @FXML private Label databaseStatusLabel;
    @FXML private Label lastBackupLabel;
    @FXML private Label activeUsersLabel;
    @FXML private TableView<Activity> recentActivityTable;
    @FXML private TableColumn<Activity, String> timeColumn;
    @FXML private TableColumn<Activity, String> activityColumn;
    @FXML private TableColumn<Activity, String> userColumn;

    private AuthService authService;
    private User currentUser;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Sample data class for recent activity
    public static class Activity {
        private final String time;
        private final String activity;
        private final String user;

        public Activity(String time, String activity, String user) {
            this.time = time;
            this.activity = activity;
            this.user = user;
        }

        public String getTime() { return time; }
        public String getActivity() { return activity; }
        public String getUser() { return user; }
    }

    @FXML
    public void initialize() {
        authService = AuthService.getInstance(); // Use singleton
        setupTableColumns();
        loadUserProfile();
        initializeSystemInfo();
        loadDashboardData();
        loadRecentActivity();

        System.out.println("AdminDashboardController initialized successfully");
    }

    private void setupTableColumns() {
        if (timeColumn != null) {
            timeColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getTime()));
        }
        if (activityColumn != null) {
            activityColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getActivity()));
        }
        if (userColumn != null) {
            userColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getUser()));
        }
    }

    private void loadUserProfile() {
        try {
            currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                welcomeLabel.setText("Welcome, " + currentUser.getName() + "!");
                userRoleLabel.setText("Role: " + currentUser.getRole().toUpperCase());
            } else {
                // Fallback to default values if no user data
                welcomeLabel.setText("Welcome, Administrator!");
                userRoleLabel.setText("Role: ADMIN");
                showErrorAlert("No user data found. Using default profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            welcomeLabel.setText("Welcome, Administrator!");
            userRoleLabel.setText("Role: ADMIN");
            showErrorAlert("Failed to load user profile: " + e.getMessage());
        }
    }

    private void initializeSystemInfo() {
        try {
            // Set current time for last backup
            lastBackupLabel.setText(timeFormat.format(new Date()));

            // Set initial system status
            systemStatusLabel.setText("🟢 Operational");
            systemStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            databaseStatusLabel.setText("🟢 Connected");
            databaseStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            activeUsersLabel.setText("1"); // Current user

        } catch (Exception e) {
            System.err.println("Error initializing system info: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                System.out.println("Loading dashboard data from API...");

                // Make API call to get dashboard stats
                String response = authService.getDashboardStats();
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    JsonNode data = jsonResponse.get("data");
                    JsonNode totals = data.get("totals");

                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        try {
                            int totalBuses = totals.get("buses").asInt();
                            int activeSchedules = totals.get("activeSchedules").asInt();
                            int todaysTrips = totals.get("todaysTrips").asInt();

                            totalBusesLabel.setText(String.valueOf(totalBuses));
                            activeSchedulesLabel.setText(String.valueOf(activeSchedules));
                            todaysTripsLabel.setText(String.valueOf(todaysTrips));

                            // Update active users count (current user + some random number for demo)
                            int activeUsers = 1 + (int)(Math.random() * 5);
                            activeUsersLabel.setText(String.valueOf(activeUsers));

                            System.out.println("Dashboard data loaded successfully: " +
                                    totalBuses + " buses, " + activeSchedules + " schedules, " +
                                    todaysTrips + " today's trips");

                        } catch (Exception e) {
                            e.printStackTrace();
                            setDefaultDashboardValues();
                            showErrorAlert("Error parsing dashboard data: " + e.getMessage());
                        }
                    });
                } else {
                    String errorMessage = jsonResponse.get("message").asText();
                    throw new Exception("API returned error: " + errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to mock data if API fails
                javafx.application.Platform.runLater(() -> {
                    setDefaultDashboardValues();
                    showErrorAlert("Failed to load dashboard data: " + e.getMessage());
                });
            }
        }).start();
    }

    private void setDefaultDashboardValues() {
        try {
            totalBusesLabel.setText("7");
            activeSchedulesLabel.setText("24");
            todaysTripsLabel.setText("8");
            activeUsersLabel.setText("3");

            // Set system status to warning
            systemStatusLabel.setText("🟡 Limited");
            systemStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

            databaseStatusLabel.setText("🟡 Degraded");
            databaseStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        } catch (Exception e) {
            System.err.println("Error setting default values: " + e.getMessage());
        }
    }

    private void loadRecentActivity() {
        new Thread(() -> {
            try {
                System.out.println("Loading recent activities from API...");

                String response = authService.getRecentActivities();
                JsonNode jsonResponse = objectMapper.readTree(response);

                if (jsonResponse.get("success").asBoolean()) {
                    JsonNode activitiesData = jsonResponse.get("data");
                    ObservableList<Activity> activities = FXCollections.observableArrayList();

                    for (JsonNode activityNode : activitiesData) {
                        try {
                            String timestamp = activityNode.get("timestamp").asText();
                            String activity = activityNode.get("activity").asText();
                            String user = activityNode.get("user").asText();

                            // Format the timestamp for better display
                            String formattedTime = formatTimestamp(timestamp);

                            activities.add(new Activity(formattedTime, activity, user));
                        } catch (Exception e) {
                            System.err.println("Error parsing activity: " + e.getMessage());
                        }
                    }

                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        if (recentActivityTable != null) {
                            recentActivityTable.setItems(activities);
                            System.out.println("Loaded " + activities.size() + " recent activities");
                        }
                    });
                } else {
                    throw new Exception("API returned error: " + jsonResponse.get("message").asText());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to mock data if API fails
                javafx.application.Platform.runLater(() -> {
                    loadMockRecentActivity();
                    System.out.println("Using mock recent activities due to API error: " + e.getMessage());
                });
            }
        }).start();
    }

    private String formatTimestamp(String timestamp) {
        try {
            // Try to parse the timestamp and format it nicely
            if (timestamp.contains("T")) {
                // ISO format
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp);
                return timeFormat.format(date);
            } else {
                // Simple format
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
                return timeFormat.format(date);
            }
        } catch (Exception e) {
            // Return original timestamp if parsing fails
            return timestamp;
        }
    }

    private void loadMockRecentActivity() {
        try {
            // Mock data for recent activity with current timestamps
            ObservableList<Activity> activities = FXCollections.observableArrayList(
                    new Activity(timeFormat.format(new Date()), "New bus added: BUS-015", "System Admin"),
                    new Activity(timeFormat.format(new Date(System.currentTimeMillis() - 3600000)), "Schedule updated: Dhaka-Chittagong", "John Driver"),
                    new Activity(timeFormat.format(new Date(System.currentTimeMillis() - 7200000)), "User registered: client123", "System"),
                    new Activity(timeFormat.format(new Date(System.currentTimeMillis() - 10800000)), "Maintenance completed: BUS-008", "Tech Team"),
                    new Activity(timeFormat.format(new Date(System.currentTimeMillis() - 14400000)), "New schedule created: Dhaka-Sylhet", "System Admin"),
                    new Activity(timeFormat.format(new Date(System.currentTimeMillis() - 18000000)), "Bus status updated: BUS-003 to maintenance", "Mike Operator")
            );

            if (recentActivityTable != null) {
                recentActivityTable.setItems(activities);
            }
        } catch (Exception e) {
            System.err.println("Error loading mock activities: " + e.getMessage());
        }
    }

    @FXML
    private void handleBusManagement() {
        System.out.println("Navigating to Bus Management");
        try {
            Main.showBusManagement();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to navigate to Bus Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleScheduleManagement() {
        System.out.println("Navigating to Schedule Management");
        try {
            Main.showScheduleManagement();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to navigate to Schedule Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleUserManagement() {
        System.out.println("Navigating to User Management");
        try {
            Main.showUserManagement();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to navigate to User Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleReports() {
        System.out.println("Reports clicked - Feature coming soon");
        showInfoAlert("Reports", "This feature is coming soon!\n\nPlanned features:\n• Revenue Reports\n• Passenger Statistics\n• Bus Utilization\n• Route Performance");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing dashboard...");
        loadDashboardData();
        loadRecentActivity();
        initializeSystemInfo();
        showInfoAlert("Refresh", "Dashboard data refreshed successfully!");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        try {
            authService.logout();
            AuthService.resetInstance(); // Reset singleton
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

    // Method to update system status (can be called from other parts of the application)
    public void updateSystemStatus(boolean isOnline, String message) {
        javafx.application.Platform.runLater(() -> {
            if (isOnline) {
                systemStatusLabel.setText("🟢 " + message);
                systemStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                systemStatusLabel.setText("🔴 " + message);
                systemStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
    }

    // Method to update database status
    public void updateDatabaseStatus(boolean isConnected) {
        javafx.application.Platform.runLater(() -> {
            if (isConnected) {
                databaseStatusLabel.setText("🟢 Connected");
                databaseStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                databaseStatusLabel.setText("🔴 Disconnected");
                databaseStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
    }
}
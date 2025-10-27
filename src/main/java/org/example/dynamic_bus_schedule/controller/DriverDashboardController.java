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

public class DriverDashboardController {

    @FXML private BorderPane mainContainer;
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label assignedBusLabel;
    @FXML private Label todaysTripsLabel;
    @FXML private Label nextTripLabel;
    @FXML private TableView<Schedule> todaysScheduleTable;
    @FXML private TableColumn<Schedule, String> routeColumn;
    @FXML private TableColumn<Schedule, String> departureColumn;
    @FXML private TableColumn<Schedule, String> arrivalColumn;
    @FXML private TableColumn<Schedule, String> timeColumn;
    @FXML private TableColumn<Schedule, String> statusColumn;

    private AuthService authService;
    private User currentUser;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // Schedule data class
    public static class Schedule {
        private final String route;
        private final String departure;
        private final String arrival;
        private final String time;
        private final String status;

        public Schedule(String route, String departure, String arrival, String time, String status) {
            this.route = route;
            this.departure = departure;
            this.arrival = arrival;
            this.time = time;
            this.status = status;
        }

        public String getRoute() { return route; }
        public String getDeparture() { return departure; }
        public String getArrival() { return arrival; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
    }

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        setupTableColumns();
        loadUserProfile();
        loadDriverData();
        loadTodaysSchedule();

        System.out.println("DriverDashboardController initialized successfully");
    }

    private void setupTableColumns() {
        if (routeColumn != null) {
            routeColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getRoute()));
        }
        if (departureColumn != null) {
            departureColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getDeparture()));
        }
        if (arrivalColumn != null) {
            arrivalColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getArrival()));
        }
        if (timeColumn != null) {
            timeColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getTime()));
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getStatus()));
        }
    }

    private void loadUserProfile() {
        try {
            currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                welcomeLabel.setText("Welcome, " + currentUser.getName() + "!");
                userRoleLabel.setText("Professional Driver");
            } else {
                welcomeLabel.setText("Welcome, Driver!");
                userRoleLabel.setText("Driver");
                showErrorAlert("No user data found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            welcomeLabel.setText("Welcome, Driver!");
            userRoleLabel.setText("Driver");
            showErrorAlert("Failed to load user profile: " + e.getMessage());
        }
    }

    private void loadDriverData() {
        // For now, use mock data. You can replace this with API calls later
        assignedBusLabel.setText("BUS-002 (Scania K-series)");
        todaysTripsLabel.setText("3");
        nextTripLabel.setText("Dhaka to Sylhet - 14:00");
    }

    private void loadTodaysSchedule() {
        new Thread(() -> {
            try {
                // Mock data for driver's schedule
                ObservableList<Schedule> schedules = FXCollections.observableArrayList(
                        new Schedule("Dhaka to Chittagong", "Dhaka", "Chittagong", "08:00 - 14:00", "🟢 Completed"),
                        new Schedule("Chittagong to Dhaka", "Chittagong", "Dhaka", "15:00 - 21:00", "🟡 In Progress"),
                        new Schedule("Dhaka to Sylhet", "Dhaka", "Sylhet", "14:00 - 19:00", "🔵 Upcoming"),
                        new Schedule("Sylhet to Dhaka", "Sylhet", "Dhaka", "20:00 - 01:00", "⚪ Scheduled")
                );

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    if (todaysScheduleTable != null) {
                        todaysScheduleTable.setItems(schedules);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to mock data if API fails
                javafx.application.Platform.runLater(() -> {
                    loadMockSchedule();
                    showErrorAlert("Failed to load schedule: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadMockSchedule() {
        ObservableList<Schedule> schedules = FXCollections.observableArrayList(
                new Schedule("Dhaka to Chittagong", "Dhaka", "Chittagong", "08:00 - 14:00", "🟢 Completed"),
                new Schedule("Chittagong to Dhaka", "Chittagong", "Dhaka", "15:00 - 21:00", "🟡 In Progress"),
                new Schedule("Dhaka to Sylhet", "Dhaka", "Sylhet", "14:00 - 19:00", "🔵 Upcoming"),
                new Schedule("Sylhet to Dhaka", "Sylhet", "Dhaka", "20:00 - 01:00", "⚪ Scheduled")
        );

        if (todaysScheduleTable != null) {
            todaysScheduleTable.setItems(schedules);
        }
    }

    @FXML
    private void handleStartTrip() {
        System.out.println("Starting trip...");
        showInfoAlert("Trip Started", "Your trip has been marked as started. Safe driving!");
    }

    @FXML
    private void handleCompleteTrip() {
        System.out.println("Completing trip...");
        showInfoAlert("Trip Completed", "Trip marked as completed. Thank you for your service!");
    }

    @FXML
    private void handleReportIssue() {
        System.out.println("Reporting issue...");
        showInfoAlert("Report Issue", "Please contact maintenance team for any bus issues.\n\nEmergency: +880-XXXX-XXXX");
    }

    @FXML
    private void handleViewSchedule() {
        System.out.println("Viewing full schedule...");
        showInfoAlert("Full Schedule", "Your weekly schedule is available in the schedule management section.");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing dashboard...");
        loadDriverData();
        loadTodaysSchedule();
        showInfoAlert("Refresh", "Dashboard data refreshed successfully!");
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
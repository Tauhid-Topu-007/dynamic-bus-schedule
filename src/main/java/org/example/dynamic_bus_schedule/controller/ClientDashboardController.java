package org.example.dynamic_bus_schedule.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.model.Schedule;
import org.example.dynamic_bus_schedule.service.ScheduleService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientDashboardController {

    @FXML private Label welcomeLabel;

    @FXML private TextField searchFromField;
    @FXML private TextField searchToField;
    @FXML private DatePicker searchDateField;
    @FXML private Button searchButton;

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> routeColumn;
    @FXML private TableColumn<Schedule, String> busColumn;
    @FXML private TableColumn<Schedule, String> departureColumn;
    @FXML private TableColumn<Schedule, String> arrivalColumn;
    @FXML private TableColumn<Schedule, Double> priceColumn;
    @FXML private TableColumn<Schedule, Integer> seatsColumn;
    @FXML private TableColumn<Schedule, String> statusColumn;

    @FXML private Button bookButton;
    @FXML private Button logoutButton;

    private ObservableList<Schedule> scheduleList;
    private ScheduleService scheduleService;
    private Schedule selectedSchedule;

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        scheduleList = FXCollections.observableArrayList();

        setupTable();
        loadRecentSchedules();
    }

    private void setupTable() {
        routeColumn.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    schedule.getRoute().getFrom() + " → " + schedule.getRoute().getTo()
            );
        });

        busColumn.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    schedule.getBus() != null ? schedule.getBus().getBusNumber() : "N/A"
            );
        });

        departureColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDepartureTime())
        );

        arrivalColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedArrivalTime())
        );

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        scheduleTable.setItems(scheduleList);

        scheduleTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedSchedule = newValue;
                    bookButton.setDisable(newValue == null);
                }
        );
    }

    private void loadRecentSchedules() {
        try {
            // Load schedules for today
            String today = java.time.LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            List<Schedule> schedules = scheduleService.searchSchedules("", "", today);
            scheduleList.setAll(schedules);
        } catch (Exception e) {
            showError("Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String from = searchFromField.getText().trim();
        String to = searchToField.getText().trim();

        if (from.isEmpty() || to.isEmpty() || searchDateField.getValue() == null) {
            showError("Please fill in all search fields");
            return;
        }

        try {
            String date = searchDateField.getValue().format(DateTimeFormatter.ISO_DATE);
            List<Schedule> schedules = scheduleService.searchSchedules(from, to, date);
            scheduleList.setAll(schedules);

            if (schedules.isEmpty()) {
                showInfo("No schedules found for your search criteria");
            }
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBook() {
        if (selectedSchedule == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Booking");
        alert.setHeaderText("Book Schedule");
        alert.setContentText(
                "Confirm booking for:\n" +
                        "Route: " + selectedSchedule.getRoute().getFrom() + " → " + selectedSchedule.getRoute().getTo() + "\n" +
                        "Departure: " + selectedSchedule.getFormattedDepartureTime() + "\n" +
                        "Price: $" + selectedSchedule.getPrice()
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showSuccess("Booking confirmed! Your seat has been reserved.");
                // In a real application, you would call a booking API here
            }
        });
    }

    @FXML
    private void handleLogout() {
        Main.showLoginScene();
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
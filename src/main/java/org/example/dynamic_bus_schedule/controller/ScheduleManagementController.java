package org.example.dynamic_bus_schedule.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.model.Bus;
import org.example.dynamic_bus_schedule.model.Schedule;
import org.example.dynamic_bus_schedule.model.User;
import org.example.dynamic_bus_schedule.service.BusService;
import org.example.dynamic_bus_schedule.service.ScheduleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleManagementController {

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> routeColumn;
    @FXML private TableColumn<Schedule, String> busColumn;
    @FXML private TableColumn<Schedule, String> departureColumn;
    @FXML private TableColumn<Schedule, String> arrivalColumn;
    @FXML private TableColumn<Schedule, Double> priceColumn;
    @FXML private TableColumn<Schedule, Integer> seatsColumn;
    @FXML private TableColumn<Schedule, String> statusColumn;

    @FXML private TextField searchFromField;
    @FXML private TextField searchToField;
    @FXML private DatePicker searchDateField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private ComboBox<Bus> busComboBox;
    @FXML private ComboBox<User> driverComboBox;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private DatePicker departureDateField;
    @FXML private TextField departureTimeField;
    @FXML private DatePicker arrivalDateField;
    @FXML private TextField arrivalTimeField;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private TextField priceField;
    @FXML private TextField availableSeatsField;

    @FXML private Button addButton;
    @FXML private Button updateStatusButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button searchButton;

    private ObservableList<Schedule> scheduleList;
    private ObservableList<Bus> busList;
    private ObservableList<User> driverList;
    private ScheduleService scheduleService;
    private BusService busService;
    private Schedule selectedSchedule;

    @FXML
    public void initialize() {
        scheduleService = new ScheduleService();
        busService = new BusService();

        scheduleList = FXCollections.observableArrayList();
        busList = FXCollections.observableArrayList();
        driverList = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        setupForm();
        loadSchedules();
        loadBuses();
        loadDrivers();

        scheduleTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectSchedule(newValue)
        );
    }

    private void setupTable() {
        routeColumn.setCellValueFactory(cellData -> {
            Schedule schedule = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(schedule.getRouteDisplay());
        });

        busColumn.setCellValueFactory(cellData -> {
            Bus bus = cellData.getValue().getBus();
            return new javafx.beans.property.SimpleStringProperty(
                    bus != null ? bus.getBusNumber() : "N/A"
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
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "scheduled", "departed", "arrived", "cancelled", "delayed");
        statusFilter.setValue("All");
    }

    private void setupForm() {
        frequencyComboBox.getItems().addAll("once", "daily", "weekly", "monthly");
        frequencyComboBox.setValue("once");

        busComboBox.setItems(busList);
        driverComboBox.setItems(driverList);

        // Set up time field formatting
        departureTimeField.setPromptText("HH:mm");
        arrivalTimeField.setPromptText("HH:mm");

        // Only allow numbers in price and seats fields
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });

        availableSeatsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                availableSeatsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadSchedules() {
        try {
            Map<String, String> filters = new HashMap<>();

            String status = statusFilter.getValue();
            if (!"All".equals(status)) {
                filters.put("status", status);
            }

            List<Schedule> schedules = scheduleService.getAllSchedules(filters);
            scheduleList.setAll(schedules);
        } catch (Exception e) {
            showError("Failed to load schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBuses() {
        try {
            List<Bus> buses = busService.getAllBuses(new HashMap<>());
            busList.setAll(buses);
        } catch (Exception e) {
            showError("Failed to load buses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDrivers() {
        // This would typically load users with driver role from the API
        // For now, we'll create some dummy drivers with proper constructor parameters
        // Assuming User constructor is: (long id, String name, String email, String phone, String role, String status)
        User driver1 = new User(1L, "John Driver", "john@example.com", "+1234567890", "driver", "active");
        User driver2 = new User(2L, "Jane Operator", "jane@example.com", "+1234567891", "driver", "active");

        driverList.addAll(driver1, driver2);
    }

    private void selectSchedule(Schedule schedule) {
        this.selectedSchedule = schedule;
        if (schedule != null) {
            // Populate form with schedule data
            fromField.setText(schedule.getRoute().getFrom());
            toField.setText(schedule.getRoute().getTo());

            // Set bus and driver
            busComboBox.getSelectionModel().select(schedule.getBus());
            driverComboBox.getSelectionModel().select(schedule.getDriver());

            // Parse and set date/time
            try {
                LocalDateTime departure = LocalDateTime.parse(schedule.getDepartureTime());
                departureDateField.setValue(departure.toLocalDate());
                departureTimeField.setText(departure.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

                LocalDateTime arrival = LocalDateTime.parse(schedule.getArrivalTime());
                arrivalDateField.setValue(arrival.toLocalDate());
                arrivalTimeField.setText(arrival.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            } catch (Exception e) {
                e.printStackTrace();
            }

            frequencyComboBox.setValue(schedule.getFrequency());
            priceField.setText(String.valueOf(schedule.getPrice()));
            availableSeatsField.setText(String.valueOf(schedule.getAvailableSeats()));

            updateStatusButton.setDisable(false);
            deleteButton.setDisable(false);
            addButton.setDisable(true);
        } else {
            clearForm();
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
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddSchedule() {
        if (!validateForm()) return;

        try {
            Schedule newSchedule = new Schedule();
            newSchedule.setBus(busComboBox.getValue());
            newSchedule.setDriver(driverComboBox.getValue());

            Schedule.Route route = new Schedule.Route();
            route.setFrom(fromField.getText().trim());
            route.setTo(toField.getText().trim());
            newSchedule.setRoute(route);

            // Combine date and time
            LocalDateTime departure = LocalDateTime.of(
                    departureDateField.getValue(),
                    java.time.LocalTime.parse(departureTimeField.getText())
            );
            LocalDateTime arrival = LocalDateTime.of(
                    arrivalDateField.getValue(),
                    java.time.LocalTime.parse(arrivalTimeField.getText())
            );

            newSchedule.setDepartureTime(departure.format(DateTimeFormatter.ISO_DATE_TIME));
            newSchedule.setArrivalTime(arrival.format(DateTimeFormatter.ISO_DATE_TIME));
            newSchedule.setFrequency(frequencyComboBox.getValue());
            newSchedule.setPrice(Double.parseDouble(priceField.getText()));
            newSchedule.setAvailableSeats(Integer.parseInt(availableSeatsField.getText()));
            newSchedule.setStatus("scheduled");

            Schedule createdSchedule = scheduleService.createSchedule(newSchedule);
            scheduleList.add(createdSchedule);
            clearForm();
            showSuccess("Schedule added successfully!");
        } catch (Exception e) {
            showError("Failed to add schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedSchedule == null) return;

        // Create a dialog for status update
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Update Schedule Status");
        dialog.setHeaderText("Update status for schedule: " + selectedSchedule.getRouteDisplay());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("scheduled", "departed", "arrived", "cancelled", "delayed");
        statusCombo.setValue(selectedSchedule.getStatus());

        TextField delayReasonField = new TextField();
        delayReasonField.setPromptText("Delay reason");
        TextField delayDurationField = new TextField();
        delayDurationField.setPromptText("Delay duration (minutes)");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Status:"),
                statusCombo,
                new Label("Delay Reason (if delayed):"),
                delayReasonField,
                new Label("Delay Duration (minutes):"),
                delayDurationField
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("status", statusCombo.getValue());
                result.put("reason", delayReasonField.getText());
                result.put("duration", delayDurationField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                String status = result.get("status");
                String reason = result.get("reason");
                int duration = 0;

                if (!result.get("duration").isEmpty()) {
                    duration = Integer.parseInt(result.get("duration"));
                }

                Schedule updatedSchedule = scheduleService.updateScheduleStatus(
                        selectedSchedule.getId(), status, reason, duration
                );

                int index = scheduleList.indexOf(selectedSchedule);
                scheduleList.set(index, updatedSchedule);
                scheduleTable.refresh();
                showSuccess("Schedule status updated successfully!");
            } catch (Exception e) {
                showError("Failed to update schedule status: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDeleteSchedule() {
        if (selectedSchedule == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Schedule");
        alert.setContentText("Are you sure you want to delete this schedule?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = scheduleService.deleteSchedule(selectedSchedule.getId());
                    if (success) {
                        scheduleList.remove(selectedSchedule);
                        clearForm();
                        showSuccess("Schedule deleted successfully!");
                    }
                } catch (Exception e) {
                    showError("Failed to delete schedule: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        scheduleTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }

    private void clearForm() {
        fromField.clear();
        toField.clear();
        busComboBox.getSelectionModel().clearSelection();
        driverComboBox.getSelectionModel().clearSelection();
        departureDateField.setValue(null);
        departureTimeField.clear();
        arrivalDateField.setValue(null);
        arrivalTimeField.clear();
        frequencyComboBox.setValue("once");
        priceField.clear();
        availableSeatsField.clear();

        updateStatusButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
        selectedSchedule = null;
    }

    private boolean validateForm() {
        if (fromField.getText().trim().isEmpty() ||
                toField.getText().trim().isEmpty() ||
                busComboBox.getValue() == null ||
                driverComboBox.getValue() == null ||
                departureDateField.getValue() == null ||
                departureTimeField.getText().isEmpty() ||
                arrivalDateField.getValue() == null ||
                arrivalTimeField.getText().isEmpty() ||
                priceField.getText().isEmpty() ||
                availableSeatsField.getText().isEmpty()) {

            showError("Please fill in all required fields");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showError("Price cannot be negative");
                return false;
            }

            int seats = Integer.parseInt(availableSeatsField.getText());
            if (seats < 0) {
                showError("Available seats cannot be negative");
                return false;
            }

            // Validate time format
            java.time.LocalTime.parse(departureTimeField.getText());
            java.time.LocalTime.parse(arrivalTimeField.getText());

        } catch (Exception e) {
            showError("Please check your input values");
            return false;
        }

        return true;
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
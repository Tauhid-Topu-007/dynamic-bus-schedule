package org.example.dynamic_bus_schedule.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dynamic_bus_schedule.Main;
import org.example.dynamic_bus_schedule.model.Bus;
import org.example.dynamic_bus_schedule.service.BusService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class BusManagementController {

    @FXML private TableView<Bus> busTable;
    @FXML private TableColumn<Bus, String> busNumberColumn;
    @FXML private TableColumn<Bus, String> licensePlateColumn;
    @FXML private TableColumn<Bus, String> modelColumn;
    @FXML private TableColumn<Bus, Integer> capacityColumn;
    @FXML private TableColumn<Bus, String> statusColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TextField busNumberField;
    @FXML private TextField licensePlateField;
    @FXML private TextField modelField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> fuelTypeField;
    @FXML private TextField yearField;
    @FXML private TextField amenitiesField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private ObservableList<Bus> busList;
    private BusService busService;
    private Bus selectedBus;

    @FXML
    public void initialize() {
        busService = new BusService();
        busList = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        setupForm();
        loadBuses();

        busTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectBus(newValue)
        );
    }

    private void setupTable() {
        busNumberColumn.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        licensePlateColumn.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        busTable.setItems(busList);
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "active", "maintenance", "inactive");
        statusFilter.setValue("All");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadBuses());
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> loadBuses());
    }

    private void setupForm() {
        fuelTypeField.getItems().addAll("diesel", "petrol", "electric", "hybrid");
        fuelTypeField.setValue("diesel");

        // Only allow numbers in capacity and year fields
        capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                capacityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadBuses() {
        try {
            Map<String, String> filters = new HashMap<>();

            String search = searchField.getText().trim();
            if (!search.isEmpty()) {
                filters.put("search", search);
            }

            String status = statusFilter.getValue();
            if (!"All".equals(status)) {
                filters.put("status", status);
            }

            List<Bus> buses = busService.getAllBuses(filters);
            busList.setAll(buses);
        } catch (Exception e) {
            showError("Failed to load buses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectBus(Bus bus) {
        this.selectedBus = bus;
        if (bus != null) {
            busNumberField.setText(bus.getBusNumber());
            licensePlateField.setText(bus.getLicensePlate());
            modelField.setText(bus.getModel());
            capacityField.setText(String.valueOf(bus.getCapacity()));

            // Set fuel type if available, otherwise use default
            if (bus.getFuelType() != null && !bus.getFuelType().isEmpty()) {
                fuelTypeField.setValue(bus.getFuelType());
            } else {
                fuelTypeField.setValue("diesel");
            }

            yearField.setText(bus.getYear() > 0 ? String.valueOf(bus.getYear()) : "");
            amenitiesField.setText(bus.getAmenitiesString());

            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            addButton.setDisable(true);
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleAddBus() {
        if (!validateForm()) return;

        try {
            Bus newBus = new Bus();
            newBus.setBusNumber(busNumberField.getText().trim().toUpperCase());
            newBus.setLicensePlate(licensePlateField.getText().trim().toUpperCase());
            newBus.setModel(modelField.getText().trim());
            newBus.setCapacity(Integer.parseInt(capacityField.getText()));
            newBus.setFuelType(fuelTypeField.getValue());
            newBus.setType("Standard"); // Default type
            newBus.setStatus("active"); // Default status

            if (!yearField.getText().isEmpty()) {
                newBus.setYear(Integer.parseInt(yearField.getText()));
            }

            if (!amenitiesField.getText().isEmpty()) {
                newBus.setAmenitiesFromString(amenitiesField.getText());
            }

            Bus createdBus = busService.createBus(newBus);
            busList.add(createdBus);
            clearForm();
            showSuccess("Bus added successfully!");
        } catch (Exception e) {
            showError("Failed to add bus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateBus() {
        if (selectedBus == null || !validateForm()) return;

        try {
            selectedBus.setBusNumber(busNumberField.getText().trim().toUpperCase());
            selectedBus.setLicensePlate(licensePlateField.getText().trim().toUpperCase());
            selectedBus.setModel(modelField.getText().trim());
            selectedBus.setCapacity(Integer.parseInt(capacityField.getText()));
            selectedBus.setFuelType(fuelTypeField.getValue());

            if (!yearField.getText().isEmpty()) {
                selectedBus.setYear(Integer.parseInt(yearField.getText()));
            } else {
                selectedBus.setYear(0);
            }

            if (!amenitiesField.getText().isEmpty()) {
                selectedBus.setAmenitiesFromString(amenitiesField.getText());
            } else {
                selectedBus.setAmenities(null);
            }

            Bus updatedBus = busService.updateBus(selectedBus.getId(), selectedBus);
            int index = busList.indexOf(selectedBus);
            busList.set(index, updatedBus);
            busTable.refresh();
            showSuccess("Bus updated successfully!");
        } catch (Exception e) {
            showError("Failed to update bus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBus() {
        if (selectedBus == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Bus");
        alert.setContentText("Are you sure you want to delete bus " + selectedBus.getBusNumber() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = busService.deleteBus(selectedBus.getId());
                    if (success) {
                        busList.remove(selectedBus);
                        clearForm();
                        showSuccess("Bus deleted successfully!");
                    }
                } catch (Exception e) {
                    showError("Failed to delete bus: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        busTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        Main.showAdminDashboard();
    }

    private void clearForm() {
        busNumberField.clear();
        licensePlateField.clear();
        modelField.clear();
        capacityField.clear();
        fuelTypeField.setValue("diesel");
        yearField.clear();
        amenitiesField.clear();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
        selectedBus = null;
    }

    private boolean validateForm() {
        if (busNumberField.getText().trim().isEmpty() ||
                licensePlateField.getText().trim().isEmpty() ||
                modelField.getText().trim().isEmpty() ||
                capacityField.getText().isEmpty()) {

            showError("Please fill in all required fields");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText());
            if (capacity <= 0 || capacity > 100) {
                showError("Capacity must be between 1 and 100");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Capacity must be a valid number");
            return false;
        }

        // Validate year if provided
        if (!yearField.getText().isEmpty()) {
            try {
                int year = Integer.parseInt(yearField.getText());
                if (year < 1990 || year > 2030) {
                    showError("Year must be between 1990 and 2030");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Year must be a valid number");
                return false;
            }
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
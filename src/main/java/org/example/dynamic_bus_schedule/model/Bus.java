package org.example.dynamic_bus_schedule.model;

import java.util.List;
import java.util.Arrays;

public class Bus {
    private long id;  // Changed from int to long
    private String busNumber;
    private String licensePlate;
    private String model;
    private int capacity;
    private String type;
    private String fuelType;
    private int year;
    private String status;
    private List<String> amenities;
    private String driverName;

    // Constructors
    public Bus() {
        this.status = "active";
        this.type = "Standard";
    }

    public Bus(long id, String busNumber, String licensePlate, String model, int capacity,  // Changed to long
               String type, String fuelType, int year, String status, List<String> amenities, String driverName) {
        this.id = id;
        this.busNumber = busNumber;
        this.licensePlate = licensePlate;
        this.model = model;
        this.capacity = capacity;
        this.type = type;
        this.fuelType = fuelType;
        this.year = year;
        this.status = status;
        this.amenities = amenities;
        this.driverName = driverName;
    }

    // Getters and setters
    public long getId() { return id; }  // Changed to long
    public void setId(long id) { this.id = id; }  // Changed to long

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    @Override
    public String toString() {
        return busNumber + " (" + model + ", " + licensePlate + ")";
    }

    // Helper method to get amenities as comma-separated string
    public String getAmenitiesString() {
        if (amenities == null || amenities.isEmpty()) {
            return "";
        }
        return String.join(", ", amenities);
    }

    // Helper method to set amenities from comma-separated string
    public void setAmenitiesFromString(String amenitiesString) {
        if (amenitiesString == null || amenitiesString.trim().isEmpty()) {
            this.amenities = null;
        } else {
            this.amenities = Arrays.asList(amenitiesString.split("\\s*,\\s*"));
        }
    }
}
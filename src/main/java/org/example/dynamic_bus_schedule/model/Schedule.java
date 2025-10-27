package org.example.dynamic_bus_schedule.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Schedule {
    private long id;  // Changed from int to long
    private Bus bus;
    private User driver;
    private Route route;
    private String departureTime;
    private String arrivalTime;
    private String frequency;
    private double price;
    private int availableSeats;
    private String status;

    // Inner class for Route
    public static class Route {
        private String from;
        private String to;

        public Route() {}

        public Route(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
    }

    // Constructors
    public Schedule() {
        this.route = new Route();
    }

    public Schedule(long id, Bus bus, User driver, Route route, String departureTime,  // Changed to long
                    String arrivalTime, String frequency, double price, int availableSeats, String status) {
        this.id = id;
        this.bus = bus;
        this.driver = driver;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.frequency = frequency;
        this.price = price;
        this.availableSeats = availableSeats;
        this.status = status;
    }

    // Getters and setters
    public long getId() { return id; }  // Changed to long
    public void setId(long id) { this.id = id; }  // Changed to long

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper methods for formatted display
    public String getFormattedDepartureTime() {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(departureTime);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return departureTime;
        }
    }

    public String getFormattedArrivalTime() {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(arrivalTime);
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return arrivalTime;
        }
    }

    public String getRouteDisplay() {
        return route.getFrom() + " → " + route.getTo();
    }
}
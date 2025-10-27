package org.example.dynamic_bus_schedule.service;

import org.example.dynamic_bus_schedule.model.Bus;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class BusService {

    public List<Bus> getAllBuses(Map<String, String> filters) {
        // Mock implementation - replace with actual API call
        List<Bus> buses = new ArrayList<>();

        // Add mock buses with all properties - using long IDs
        buses.add(new Bus(1L, "BUS001", "ABC123", "Mercedes Tourismo", 50,
                "Luxury", "diesel", 2022, "active",
                Arrays.asList("wifi", "ac", "charging_ports", "toilet"), "John Doe"));

        buses.add(new Bus(2L, "BUS002", "DEF456", "Volvo 9700", 45,
                "Standard", "diesel", 2021, "active",
                Arrays.asList("ac", "charging_ports"), "Jane Smith"));

        buses.add(new Bus(3L, "BUS003", "GHI789", "Scania Interlink", 40,
                "Standard", "hybrid", 2023, "maintenance",
                Arrays.asList("wifi", "ac"), "Mike Johnson"));

        // Apply filters
        String statusFilter = filters.get("status");
        String searchFilter = filters.get("search");

        if (statusFilter != null && !statusFilter.equals("All")) {
            buses.removeIf(bus -> !bus.getStatus().equals(statusFilter));
        }

        if (searchFilter != null && !searchFilter.isEmpty()) {
            String searchLower = searchFilter.toLowerCase();
            buses.removeIf(bus ->
                    !bus.getBusNumber().toLowerCase().contains(searchLower) &&
                            !bus.getLicensePlate().toLowerCase().contains(searchLower) &&
                            !bus.getModel().toLowerCase().contains(searchLower)
            );
        }

        return buses;
    }

    public Bus createBus(Bus bus) {
        // Mock implementation - replace with actual API call
        bus.setId((long) (Math.random() * 1000) + 100L); // Generate random long ID
        return bus;
    }

    public Bus updateBus(long busId, Bus bus) { // Changed to long
        // Mock implementation - replace with actual API call
        bus.setId(busId);
        return bus;
    }

    public boolean deleteBus(long busId) { // Changed to long
        // Mock implementation - replace with actual API call
        return true;
    }

    public Bus getBusById(long busId) { // Changed to long
        // Mock implementation
        return new Bus(busId, "BUS" + busId, "PLATE" + busId, "Model " + busId,
                50, "Standard", "diesel", 2022, "active",
                Arrays.asList("ac"), "Driver " + busId);
    }
}
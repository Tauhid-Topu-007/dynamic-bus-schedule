package org.example.dynamic_bus_schedule.service;

import org.example.dynamic_bus_schedule.model.User;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class UserService {

    public List<User> getAllUsers(Map<String, String> filters) {
        // Mock implementation - replace with actual API call
        List<User> users = new ArrayList<>();

        // Add mock users
        users.add(new User(1L, "Admin User", "admin@example.com", "+1234567890", "admin", "active"));
        users.add(new User(2L, "Client User", "client@example.com", "+1234567891", "client", "active"));
        users.add(new User(3L, "Driver User", "driver@example.com", "+1234567892", "driver", "active"));

        // Apply filters
        String roleFilter = filters.get("role");
        String statusFilter = filters.get("status");
        String searchFilter = filters.get("search");

        if (roleFilter != null && !roleFilter.equals("All")) {
            users.removeIf(user -> !user.getRole().equals(roleFilter));
        }

        if (statusFilter != null && !statusFilter.equals("All")) {
            users.removeIf(user -> !user.getStatus().equals(statusFilter));
        }

        if (searchFilter != null && !searchFilter.isEmpty()) {
            String searchLower = searchFilter.toLowerCase();
            users.removeIf(user ->
                    !user.getName().toLowerCase().contains(searchLower) &&
                            !user.getEmail().toLowerCase().contains(searchLower)
            );
        }

        return users;
    }

    public User createUser(User user) {
        // Mock implementation - replace with actual API call
        user.setId((long) (Math.random() * 1000) + 100L);
        return user;
    }

    public User updateUser(long userId, User user) {
        // Mock implementation - replace with actual API call
        user.setId(userId);
        return user;
    }

    public boolean deleteUser(long userId) {
        // Mock implementation - replace with actual API call
        return true;
    }

    public User getUserById(long userId) {
        // Mock implementation
        return new User(userId, "User " + userId, "user" + userId + "@example.com",
                "+1234567890", "client", "active");
    }
}
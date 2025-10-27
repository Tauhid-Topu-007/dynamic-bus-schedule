package org.example.dynamic_bus_schedule.service;

import org.example.dynamic_bus_schedule.model.Schedule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ScheduleService {
    private static final String BASE_URL = "http://localhost:5000/api";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Schedule> getAllSchedules(Map<String, String> filters) {
        // Mock implementation - replace with actual API call
        List<Schedule> schedules = new ArrayList<>();

        // Add some mock schedules for testing - using long IDs
        Schedule.Route route1 = new Schedule.Route("City A", "City B");
        Schedule schedule1 = new Schedule(1L, null, null, route1,
                "2024-12-20T08:00:00", "2024-12-20T10:00:00", "daily", 25.50, 50, "scheduled");
        schedules.add(schedule1);

        Schedule.Route route2 = new Schedule.Route("City B", "City C");
        Schedule schedule2 = new Schedule(2L, null, null, route2,
                "2024-12-20T09:00:00", "2024-12-20T11:30:00", "daily", 20.00, 40, "scheduled");
        schedules.add(schedule2);

        return schedules;
    }

    public List<Schedule> searchSchedules(String from, String to, String date) {
        // Mock implementation
        return getAllSchedules(new HashMap<>());
    }

    public Schedule createSchedule(Schedule schedule) {
        // Mock implementation
        schedule.setId((long) (Math.random() * 1000)); // Changed to long
        return schedule;
    }

    public Schedule updateScheduleStatus(long scheduleId, String status, String reason, int duration) { // Changed to long
        // Mock implementation
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        schedule.setStatus(status);
        return schedule;
    }

    public boolean deleteSchedule(long scheduleId) { // Changed to long
        // Mock implementation
        return true;
    }
}
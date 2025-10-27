module org.example.dynamic_bus_schedule {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;
    requires javafx.web;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens org.example.dynamic_bus_schedule to javafx.fxml;
    opens org.example.dynamic_bus_schedule.controller to javafx.fxml;
    opens org.example.dynamic_bus_schedule.model to javafx.fxml, com.fasterxml.jackson.databind;
    opens org.example.dynamic_bus_schedule.service to javafx.fxml, com.fasterxml.jackson.databind; // ADDED com.fasterxml.jackson.databind here
    opens org.example.dynamic_bus_schedule.util to javafx.fxml;

    exports org.example.dynamic_bus_schedule;
    exports org.example.dynamic_bus_schedule.controller;
    exports org.example.dynamic_bus_schedule.model;
    exports org.example.dynamic_bus_schedule.service; // ADDED this line
    exports org.example.dynamic_bus_schedule.util;
}
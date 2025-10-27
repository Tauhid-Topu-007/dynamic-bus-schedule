package org.example.dynamic_bus_schedule;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.dynamic_bus_schedule.controller.LoginController;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Set stage properties first
        primaryStage.setTitle("Dynamic Bus Schedule System");
        primaryStage.setResizable(true);

        // Try to load icon, but don't crash if not found
        try {
            Image icon = new Image(getClass().getResourceAsStream("/org/example/dynamic_bus_schedule/images/bus-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, continuing without it...");
        }

        showLoginScene();
        primaryStage.show();
    }

    public static void showLoginScene() {
        try {
            System.out.println("=== Loading Login Scene ===");

            URL fxmlUrl = Main.class.getResource("/org/example/dynamic_bus_schedule/fxml/login.fxml");
            System.out.println("FXML URL: " + fxmlUrl);

            if (fxmlUrl == null) {
                System.err.println("ERROR: FXML file not found!");
                showErrorAlert("Login screen not available. Please check installation.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            // Set controller
            LoginController controller = loader.getController();
            if (controller != null) {
                // Don't create new Main instance, use the existing application context
                controller.setMainApp(getInstance());
                System.out.println("Controller initialized successfully");
            }

            Scene scene = new Scene(root, 1000, 700);

            // Load CSS if available
            URL cssUrl = Main.class.getResource("/org/example/dynamic_bus_schedule/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS loaded successfully");
            } else {
                System.out.println("CSS file not found, using default styles");
            }

            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            System.out.println("Login scene displayed successfully");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error loading login screen: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Unexpected error: " + e.getMessage());
        }
    }

    public static void showAdminDashboard() {
        System.out.println("Redirecting to Admin Dashboard");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/admin-dashboard.fxml", "Admin Dashboard");
    }

    public static void showClientDashboard() {
        System.out.println("Redirecting to Client Dashboard");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/client-dashboard.fxml", "Client Dashboard");
    }

    public static void showDriverDashboard() {
        System.out.println("Redirecting to Driver Dashboard");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/driver-dashboard.fxml", "Driver Dashboard");
    }

    public static void showBusManagement() {
        System.out.println("Loading Bus Management");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/bus-management.fxml", "Bus Management");
    }

    public static void showScheduleManagement() {
        System.out.println("Loading Schedule Management");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/schedule-management.fxml", "Schedule Management");
    }

    public static void showUserManagement() {
        System.out.println("Loading User Management");
        loadFXML("/org/example/dynamic_bus_schedule/fxml/user-management.fxml", "User Management");
    }

    public static void showLoginScreen() {
        System.out.println("Returning to Login Screen");
        showLoginScene();
    }

    private static void loadFXML(String fxmlPath, String title) {
        try {
            System.out.println("Loading FXML: " + fxmlPath);

            URL fxmlUrl = Main.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML not found: " + fxmlPath);

                // If dashboard FXML is not available, show a simple placeholder
                if (fxmlPath.contains("dashboard") || fxmlPath.contains("management")) {
                    showPlaceholderScreen(title);
                    return;
                }

                showErrorAlert("Screen not available: " + title);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Load CSS if available
            URL cssUrl = Main.class.getResource("/org/example/dynamic_bus_schedule/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();

            System.out.println(title + " loaded successfully");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading " + title + ": " + e.getMessage());

            // Show placeholder if FXML fails to load
            if (fxmlPath.contains("dashboard") || fxmlPath.contains("management")) {
                showPlaceholderScreen(title);
            } else {
                showErrorAlert("Error loading " + title + ": " + e.getMessage());
            }
        }
    }

    private static void showPlaceholderScreen(String title) {
        try {
            // Create a simple placeholder screen
            javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox(20);
            placeholder.setAlignment(javafx.geometry.Pos.CENTER);
            placeholder.setStyle("-fx-padding: 40; -fx-background-color: #f5f5f5;");

            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

            javafx.scene.control.Label messageLabel = new javafx.scene.control.Label("This screen is under development");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

            javafx.scene.control.Button backButton = new javafx.scene.control.Button("Back to Login");
            backButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            backButton.setOnAction(e -> showLoginScreen());

            placeholder.getChildren().addAll(titleLabel, messageLabel, backButton);

            Scene scene = new Scene(placeholder, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();

            System.out.println("Placeholder screen shown for: " + title);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to load placeholder screen: " + e.getMessage());
        }
    }

    private static void showErrorAlert(String message) {
        System.err.println("ERROR: " + message);

        javafx.application.Platform.runLater(() -> {
            try {
                // Check if we're on the JavaFX Application Thread and stage is showing
                if (primaryStage != null && primaryStage.isShowing()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.initOwner(primaryStage);
                    alert.showAndWait();
                } else {
                    // Fallback: print to console
                    System.err.println("Error Alert: " + message);
                }
            } catch (Exception e) {
                System.err.println("Could not show error dialog: " + e.getMessage());
                System.err.println("Original error: " + message);
            }
        });
    }

    public static void showSuccessAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                if (primaryStage != null && primaryStage.isShowing()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.initOwner(primaryStage);
                    alert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("Could not show success dialog: " + e.getMessage());
            }
        });
    }

    // Singleton instance reference
    private static Main instance;

    @Override
    public void init() throws Exception {
        super.init();
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        System.out.println("Starting Dynamic Bus Schedule Application...");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JavaFX Version: " + System.getProperty("javafx.version"));

        // Debug resource locations
        debugResourceLocation();

        launch(args);
    }

    private static void debugResourceLocation() {
        try {
            System.out.println("=== Resource Debug Information ===");

            String[] pathsToCheck = {
                    "/org/example/dynamic_bus_schedule/fxml/login.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/admin-dashboard.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/client-dashboard.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/driver-dashboard.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/bus-management.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/schedule-management.fxml",
                    "/org/example/dynamic_bus_schedule/fxml/user-management.fxml",
                    "/org/example/dynamic_bus_schedule/css/styles.css",
                    "/org/example/dynamic_bus_schedule/images/bus-icon.png"
            };

            for (String path : pathsToCheck) {
                URL url = Main.class.getResource(path);
                System.out.println(path + " -> " + (url != null ? "✅ FOUND" : "❌ NOT FOUND"));
            }
            System.out.println("=== End Debug ===");

        } catch (Exception e) {
            System.err.println("Debug error: " + e.getMessage());
        }
    }
}
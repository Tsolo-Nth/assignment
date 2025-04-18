package com.example.vehicle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.DatePicker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehicleRentalSystem extends Application {
    private Stage primaryStage;
    private Scene mainScene;
    private VBox contentLayout;
    private String currentUserRole = "Admin";
    private String currentUsername = "";
    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            primaryStage.setTitle("Vehicle Rental System");

            // Initialize database
            initializeDatabase();

            showLoginScreen();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Application Error", "Failed to start application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void initializeDatabase() {
        try {
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite driver loaded successfully!");

            // Connect to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:vehicle_rental.db");

            // Create tables if they don't exist
            Statement statement = connection.createStatement();

            // Users table
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "username TEXT UNIQUE NOT NULL, " +
                                      "password TEXT NOT NULL, " +
                                      "role TEXT NOT NULL)");

            // Vehicles table
            statement.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "brand TEXT NOT NULL, " +
                                      "model TEXT NOT NULL, " +
                                      "category TEXT NOT NULL, " +
                                      "year INTEGER NOT NULL, " +
                                      "price_per_day REAL NOT NULL, " +
                                      "available BOOLEAN NOT NULL DEFAULT 1)");

            // Customers table
            statement.execute("CREATE TABLE IF NOT EXISTS customers (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "name TEXT NOT NULL, " +
                                      "email TEXT, " +
                                      "phone TEXT, " +
                                      "address TEXT, " +
                                      "license_number TEXT)");

            // Bookings table
            statement.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "customer_id INTEGER NOT NULL, " +
                                      "vehicle_id INTEGER NOT NULL, " +
                                      "start_date TEXT NOT NULL, " +
                                      "end_date TEXT NOT NULL, " +
                                      "total_amount REAL NOT NULL, " +
                                      "status TEXT NOT NULL DEFAULT 'Pending', " +
                                      "FOREIGN KEY(customer_id) REFERENCES customers(id), " +
                                      "FOREIGN KEY(vehicle_id) REFERENCES vehicles(id))");

            // Payments table
            statement.execute("CREATE TABLE IF NOT EXISTS payments (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "booking_id INTEGER NOT NULL, " +
                                      "amount REAL NOT NULL, " +
                                      "payment_method TEXT NOT NULL, " +
                                      "payment_date TEXT NOT NULL, " +
                                      "processed_by TEXT NOT NULL, " +
                                      "FOREIGN KEY(booking_id) REFERENCES bookings(id))");

            // Insert default admin user if not exists
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            if (rs.getInt(1) == 0) {
                statement.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')");
                statement.execute("INSERT INTO users (username, password, role) VALUES ('employee', 'employee123', 'Employee')");
            }

            // Insert sample vehicles if none exist
            rs = statement.executeQuery("SELECT COUNT(*) FROM vehicles");
            if (rs.getInt(1) == 0) {
                statement.execute("INSERT INTO vehicles (brand, model, category, year, price_per_day, available) VALUES " +
                                          "('Toyota', 'Camry', 'Car', 2022, 90.0, 1), " +
                                          "('Honda', 'CR-V', 'SUV', 2021, 120.0, 1), " +
                                          "('Ford', 'F-150', 'Truck', 2020, 150.0, 1)");
            }

            // Insert sample customers if none exist
            rs = statement.executeQuery("SELECT COUNT(*) FROM customers");
            if (rs.getInt(1) == 0) {
                statement.execute("INSERT INTO customers (name, email, phone, address, license_number) VALUES " +
                                          "('Nthatuoa Ts'olo', 'nthatuoa.tsolo@example.com', '555-1234', '123 Main St', 'DL-12345678'), " +
                                          "('Lebo Chane', 'Lebo.Chane@example.com', '555-5678', '456 Oak Ave', 'DL-87654321')");
            }

        } catch (ClassNotFoundException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to load database driver: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Close database connection when application stops
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showLoginScreen() {
        try {
            StackPane root = new StackPane();

            // Background with gradient
            Rectangle bg = new Rectangle(800, 600);
            LinearGradient gradient = new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("violet")),
                    new Stop(0.5, Color.web("blue")),
                    new Stop(1, Color.web("violet"))
            );
            bg.setFill(gradient);

            // Pattern overlay
            Rectangle patternOverlay = new Rectangle(800, 600);
            patternOverlay.setFill(Color.rgb(0, 0, 0, 0.05));
            patternOverlay.setStyle("-fx-fill: url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"40\" height=\"40\" viewBox=\"0 0 40 40\"><circle cx=\"20\" cy=\"20\" r=\"1\" fill=\"white\" opacity=\"0.2\"/></svg>');");

            // Form container
            VBox formContainer = new VBox(20);
            formContainer.setAlignment(Pos.CENTER);
            formContainer.setPadding(new Insets(40));
            formContainer.setMaxWidth(400);

            // Decorative element with animation
            Rectangle decor = new Rectangle(100, 5, Color.web("#4CAF50"));
            decor.setArcHeight(10);
            decor.setArcWidth(10);
            decor.setEffect(new DropShadow(10, Color.web("#4CAF50")));

            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(2), decor);
            scaleTransition.setFromX(0.5);
            scaleTransition.setToX(1.5);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);

            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), decor);
            rotateTransition.setFromAngle(-5);
            rotateTransition.setToAngle(5);
            rotateTransition.setAutoReverse(true);
            rotateTransition.setCycleCount(RotateTransition.INDEFINITE);

            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, rotateTransition);
            parallelTransition.play();

            // Title with animation
            Text title = new Text("VEHICLE RENTAL SYSTEM");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            title.setFill(Color.WHITE);

            FadeTransition fadeTitle = new FadeTransition(Duration.seconds(2), title);
            fadeTitle.setFromValue(0.5);
            fadeTitle.setToValue(1);
            fadeTitle.setCycleCount(FadeTransition.INDEFINITE);
            fadeTitle.setAutoReverse(true);
            fadeTitle.play();

            // Login form
            GridPane loginGrid = new GridPane();
            loginGrid.setAlignment(Pos.CENTER);
            loginGrid.setHgap(15);
            loginGrid.setVgap(15);
            loginGrid.setPadding(new Insets(30, 30, 30, 30));
            loginGrid.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 20;");

            // Form elements
            Label usernameLabel = createAnimatedLabel("Username:");
            TextField usernameField = createAnimatedTextField("Enter username");

            Label passwordLabel = createAnimatedLabel("Password:");
            PasswordField passwordField = createAnimatedPasswordField("Enter password");

            Label roleLabel = createAnimatedLabel("Role:");
            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("Admin", "Employee");
            roleComboBox.setValue("Admin");
            roleComboBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-font-size: 14px;");

            FadeTransition comboFade = new FadeTransition(Duration.seconds(1), roleComboBox);
            comboFade.setFromValue(0);
            comboFade.setToValue(1);
            comboFade.play();

            // Login button with effects
            Button loginBtn = new Button("LOGIN");
            loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5;");
            loginBtn.setPrefWidth(200);
            loginBtn.setPrefHeight(40);

            loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);"));
            loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-effect: null;"));

            ScaleTransition btnScale = new ScaleTransition(Duration.seconds(1.5), loginBtn);
            btnScale.setFromX(1);
            btnScale.setToX(1.05);
            btnScale.setFromY(1);
            btnScale.setToY(1.05);
            btnScale.setAutoReverse(true);
            btnScale.setCycleCount(ScaleTransition.INDEFINITE);
            btnScale.play();

            // Login action
            loginBtn.setOnAction(e -> {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String role = roleComboBox.getValue();

                if (!username.isEmpty() && !password.isEmpty()) {
                    try {
                        PreparedStatement stmt = connection.prepareStatement(
                                "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?");
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        stmt.setString(3, role);

                        ResultSet rs = stmt.executeQuery();

                        if (rs.next()) {
                            currentUserRole = role;
                            currentUsername = username;

                            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), root);
                            fadeOut.setFromValue(1);
                            fadeOut.setToValue(0);
                            fadeOut.setOnFinished(event -> initializeMainApplication());
                            fadeOut.play();
                        } else {
                            showLoginError(loginGrid, "Invalid username, password, or role combination");
                        }
                    } catch (SQLException ex) {
                        showAlert(AlertType.ERROR, "Database Error", "Failed to verify login: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    showLoginError(loginGrid, "Please enter both username and password");
                }
            });

            // Add elements to grid
            loginGrid.add(usernameLabel, 0, 0);
            loginGrid.add(usernameField, 1, 0);
            loginGrid.add(passwordLabel, 0, 1);
            loginGrid.add(passwordField, 1, 1);
            loginGrid.add(roleLabel, 0, 2);
            loginGrid.add(roleComboBox, 1, 2);
            loginGrid.add(loginBtn, 0, 3, 2, 1);

            // Build the scene
            formContainer.getChildren().addAll(title, decor, loginGrid);
            root.getChildren().addAll(bg, patternOverlay, formContainer);
            Scene loginScene = new Scene(root, 800, 600);

            // Fade in animation
            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            primaryStage.setScene(loginScene);
            primaryStage.show();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to initialize login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginError(GridPane loginGrid, String message) {
        FadeTransition shakeFade = new FadeTransition(Duration.millis(100), loginGrid);
        shakeFade.setFromValue(0.7);
        shakeFade.setToValue(1);

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginGrid);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        ParallelTransition shakeTransition = new ParallelTransition(shake, shakeFade);
        shakeTransition.play();

        showAlert(AlertType.ERROR, "Login Failed", message);
    }

    private Label createAnimatedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        FadeTransition fade = new FadeTransition(Duration.seconds(1), label);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        return label;
    }

    private TextField createAnimatedTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-font-size: 14px; -fx-background-radius: 5;");

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), field);
        scale.setFromX(0.9);
        scale.setToX(1);
        scale.play();

        return field;
    }

    private PasswordField createAnimatedPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-font-size: 14px; -fx-background-radius: 5;");

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), field);
        scale.setFromX(0.9);
        scale.setToX(1);
        scale.play();

        return field;
    }

    private void initializeMainApplication() {
        try {
            // Main menu
            HBox mainMenuLayout = new HBox(20);
            mainMenuLayout.setPadding(new Insets(20));
            mainMenuLayout.setAlignment(Pos.CENTER);

            // Menu buttons
            Button vehicleManagementBtn = createMenuButton("Manage Vehicles", "#4CAF50");
            Button customerManagementBtn = createMenuButton("Manage Customers", "#FFA500");
            Button bookingManagementBtn = createMenuButton("Manage Bookings", "#1E90FF");
            Button paymentBtn = createMenuButton("Payments", "#555555");
            Button reportBtn = createMenuButton("View Reports", "#8A2BE2");
            Button logoutBtn = createMenuButton("Logout", "#FF6347");

            if ("Employee".equals(currentUserRole)) {
                vehicleManagementBtn.setVisible(false);
                customerManagementBtn.setVisible(false);
                reportBtn.setVisible(false);
            }

            mainMenuLayout.getChildren().addAll(
                    vehicleManagementBtn, customerManagementBtn, bookingManagementBtn,
                    paymentBtn, reportBtn, logoutBtn
            );

            // Welcome message
            Label welcomeMessage = new Label("Welcome, " + currentUsername + " (" + currentUserRole + ")");
            welcomeMessage.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            // Content area
            contentLayout = new VBox(10);
            contentLayout.setPadding(new Insets(20));

            // Main layout
            VBox mainLayout = new VBox(20);
            mainLayout.setPadding(new Insets(20));
            mainLayout.setAlignment(Pos.CENTER);
            mainLayout.getChildren().addAll(welcomeMessage, mainMenuLayout, contentLayout);
            mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e4e8f0); " +
                                        "-fx-background-image: url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\"><circle cx=\"50\" cy=\"50\" r=\"1\" fill=\"%23d9d9d9\" opacity=\"0.2\"/></svg>');");

            // Main scene
            mainScene = new Scene(mainLayout, 1000, 600);
            primaryStage.setScene(mainScene);

            // Button actions
            vehicleManagementBtn.setOnAction(event -> showVehicleManagement());
            customerManagementBtn.setOnAction(event -> showCustomerManagement());
            bookingManagementBtn.setOnAction(event -> showBookingManagement());
            paymentBtn.setOnAction(event -> showPaymentManagement());
            reportBtn.setOnAction(event -> showReports());
            logoutBtn.setOnAction(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), mainLayout);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> showLoginScreen());
                fadeOut.play();
            });
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Application Error", "Failed to initialize main application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Button createMenuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; " +
                             "-fx-text-fill: white; " +
                             "-fx-font-weight: bold; " +
                             "-fx-font-size: 14px; " +
                             "-fx-background-radius: 5; " +
                             "-fx-padding: 10 20;");
        btn.setPrefWidth(180);
        btn.setPrefHeight(40);

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + color + ", -10%); " +
                                                        "-fx-text-fill: white; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-font-size: 14px; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-padding: 10 20; " +
                                                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"));

        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; " +
                                                       "-fx-text-fill: white; " +
                                                       "-fx-font-weight: bold; " +
                                                       "-fx-font-size: 14px; " +
                                                       "-fx-background-radius: 5; " +
                                                       "-fx-padding: 10 20; " +
                                                       "-fx-effect: null;"));

        btn.setOnMousePressed(e -> btn.setStyle("-fx-background-color: derive(" + color + ", -20%); " +
                                                        "-fx-text-fill: white; " +
                                                        "-fx-font-weight: bold; " +
                                                        "-fx-font-size: 14px; " +
                                                        "-fx-background-radius: 5; " +
                                                        "-fx-padding: 10 20;"));

        btn.setOnMouseReleased(e -> btn.setStyle("-fx-background-color: derive(" + color + ", -10%); " +
                                                         "-fx-text-fill: white; " +
                                                         "-fx-font-weight: bold; " +
                                                         "-fx-font-size: 14px; " +
                                                         "-fx-background-radius: 5; " +
                                                         "-fx-padding: 10 20; " +
                                                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"));

        return btn;
    }

    private void showVehicleManagement() {
        try {
            contentLayout.getChildren().clear();

            GridPane gridPane = new GridPane();
            gridPane.setHgap(15);
            gridPane.setVgap(15);
            gridPane.setPadding(new Insets(20));
            gridPane.setAlignment(Pos.CENTER);

            addFormField(gridPane, "Brand:", new TextField(), 0);
            addFormField(gridPane, "Model:", new TextField(), 1);

            ComboBox<String> categoryComboBox = new ComboBox<>();
            categoryComboBox.getItems().addAll("Car", "SUV", "Truck", "Van", "Motorcycle");
            addFormField(gridPane, "Category:", categoryComboBox, 2);

            addFormField(gridPane, "Year:", new TextField(), 3);
            addFormField(gridPane, "Rental Price/Day:", new TextField(), 4);

            CheckBox availabilityCheckBox = new CheckBox("Available");
            availabilityCheckBox.setStyle("-fx-font-size: 14px;");
            gridPane.add(new Label("Availability:"), 0, 5);
            gridPane.add(availabilityCheckBox, 1, 5);

            Button addBtn = createActionButton("Add Vehicle", "#4CAF50");
            Button updateBtn = createActionButton("Update Vehicle", "#FFA500");
            Button deleteBtn = createActionButton("Delete Vehicle", "#F44336");

            HBox buttonBox = new HBox(15, addBtn, updateBtn, deleteBtn);
            buttonBox.setAlignment(Pos.CENTER);

            contentLayout.getChildren().addAll(gridPane, buttonBox);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to load vehicle management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCustomerManagement() {
        try {
            contentLayout.getChildren().clear();

            GridPane gridPane = new GridPane();
            gridPane.setHgap(15);
            gridPane.setVgap(15);
            gridPane.setPadding(new Insets(20));
            gridPane.setAlignment(Pos.CENTER);

            addFormField(gridPane, "Customer Name:", new TextField(), 0);
            addFormField(gridPane, "Email:", new TextField(), 1);
            addFormField(gridPane, "Phone:", new TextField(), 2);
            addFormField(gridPane, "Address:", new TextField(), 3);
            addFormField(gridPane, "License Number:", new TextField(), 4);

            Button registerBtn = createActionButton("Register Customer", "#4CAF50");
            Button updateBtn = createActionButton("Update Details", "#FFA500");

            HBox buttonBox = new HBox(15, registerBtn, updateBtn);
            buttonBox.setAlignment(Pos.CENTER);

            contentLayout.getChildren().addAll(gridPane, buttonBox);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to load customer management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showBookingManagement() {
        try {
            contentLayout.getChildren().clear();

            GridPane gridPane = new GridPane();
            gridPane.setHgap(15);
            gridPane.setVgap(15);
            gridPane.setPadding(new Insets(20));
            gridPane.setAlignment(Pos.CENTER);

            ComboBox<String> customerCombo = new ComboBox<>();
            customerCombo.getItems().addAll("Nthatuoa Ts'olo (NT001)", "Lebo Chane (LC002)");
            addFormField(gridPane, "Customer:", customerCombo, 0);

            ComboBox<String> vehicleCombo = new ComboBox<>();
            vehicleCombo.getItems().addAll("Toyota Camry (TC-1001)", "Honda CR-V (HC-2002)");
            addFormField(gridPane, "Vehicle:", vehicleCombo, 1);

            addFormField(gridPane, "Start Date:", new DatePicker(), 2);
            addFormField(gridPane, "End Date:", new DatePicker(), 3);

            TextField totalField = new TextField();
            totalField.setEditable(false);
            addFormField(gridPane, "Total Amount:", totalField, 4);

            Button createBtn = createActionButton("Create Booking", "#4CAF50");
            Button modifyBtn = createActionButton("Modify Booking", "#FFA500");
            Button cancelBtn = createActionButton("Cancel Booking", "#F44336");

            HBox buttonBox = new HBox(15, createBtn, modifyBtn, cancelBtn);
            buttonBox.setAlignment(Pos.CENTER);

            contentLayout.getChildren().addAll(gridPane, buttonBox);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to load booking management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPaymentManagement() {
        try {
            contentLayout.getChildren().clear();

            GridPane gridPane = new GridPane();
            gridPane.setHgap(15);
            gridPane.setVgap(15);
            gridPane.setPadding(new Insets(20));
            gridPane.setAlignment(Pos.CENTER);

            ComboBox<String> bookingCombo = new ComboBox<>();
            bookingCombo.getItems().addAll("B-001 (Nthatuoa - Toyota Camry)", "B-002 (Lebo Chane - Honda CR-V)");
            addFormField(gridPane, "Booking:", bookingCombo, 0);

            TextField amountField = new TextField();
            amountField.setEditable(false);
            addFormField(gridPane, "Base Amount:", amountField, 1);

            TextField extrasField = new TextField();
            extrasField.setText("0.00");
            addFormField(gridPane, "Additional Charges:", extrasField, 2);

            TextField totalField = new TextField();
            totalField.setEditable(false);
            addFormField(gridPane, "Total Amount:", totalField, 3);

            ComboBox<String> paymentCombo = new ComboBox<>();
            paymentCombo.getItems().addAll("Cash", "Credit Card", "Debit Card", "Bank Transfer", "Online Payment");
            addFormField(gridPane, "Payment Method:", paymentCombo, 4);

            Button calculateBtn = createActionButton("Calculate", "#1E90FF");
            calculateBtn.setOnAction(e -> {
                amountField.setText("M450.00");
                totalField.setText("M" + (450.00 + Double.parseDouble(extrasField.getText())));
            });

            Button paymentBtn = createActionButton("Process Payment", "#4CAF50");
            paymentBtn.setOnAction(e -> showAlert(AlertType.INFORMATION, "Payment Successful",
                                                  "Payment of " + totalField.getText() + " processed via " + paymentCombo.getValue()));

            Button invoiceBtn = createActionButton("Print Invoice", "#8A2BE2");
            invoiceBtn.setOnAction(e -> printInvoice());

            HBox buttonBox = new HBox(15, calculateBtn, paymentBtn, invoiceBtn);
            buttonBox.setAlignment(Pos.CENTER);

            contentLayout.getChildren().addAll(gridPane, buttonBox);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to load payment management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printInvoice() {
        try {
            Text invoiceText = new Text();
            invoiceText.setFont(Font.font("Arial", 12));
            invoiceText.setText(
                    "================================\n" +
                            "       VEHICLE RENTAL INVOICE\n" +
                            "================================\n\n" +
                            "Invoice #: INV-2023-001\n" +
                            "Date: " + LocalDate.now() + "\n\n" +
                            "Customer: Nthatuoa Ts'olo\n" +
                            "License: DL-12345678\n" +
                            "Contact: nthatuoa.tsolo@example.com\n\n" +
                            "Vehicle: Toyota Camry (2022)\n" +
                            "Plate: TC-1001\n" +
                            "Category: Sedan\n\n" +
                            "Rental Period: 5 days\n" +
                            "Daily Rate: M90.00\n" +
                            "Subtotal: M450.00\n" +
                            "Additional Charges: M0.00\n" +
                            "Total Amount: M450.00\n\n" +
                            "Payment Method: Credit Card\n" +
                            "Processed By: " + currentUsername + "\n\n" +
                            "Thank you for your business!\n" +
                            "================================\n"
            );

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(primaryStage)) {
                boolean success = job.printPage(invoiceText);
                if (success) {
                    job.endJob();
                    showAlert(AlertType.INFORMATION, "Print Successful", "Invoice printed successfully");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Print Error", "Failed to print invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showReports() {
        try {
            contentLayout.getChildren().clear();

            TabPane tabPane = new TabPane();

            Tab financialTab = new Tab("Financial Summary");
            PieChart financialChart = new PieChart();
            financialChart.getData().addAll(
                    new PieChart.Data("Completed Payments", 65),
                    new PieChart.Data("Pending Payments", 15),
                    new PieChart.Data("Cancelled Bookings", 20)
            );
            financialTab.setContent(financialChart);

            Tab vehicleTab = new Tab("Vehicle Status");
            PieChart vehicleChart = new PieChart();
            vehicleChart.getData().addAll(
                    new PieChart.Data("Available", 45),
                    new PieChart.Data("Rented", 35),
                    new PieChart.Data("Maintenance", 20)
            );
            vehicleTab.setContent(vehicleChart);

            tabPane.getTabs().addAll(financialTab, vehicleTab);

            Button exportBtn = createActionButton("Export Report", "#4CAF50");
            exportBtn.setOnAction(e -> showAlert(AlertType.INFORMATION, "Export", "Report exported successfully"));

            contentLayout.getChildren().addAll(tabPane, exportBtn);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "UI Error", "Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addFormField(GridPane grid, String labelText, Control field, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        grid.add(label, 0, row);

        field.setPrefWidth(250);
        if (field instanceof TextInputControl) {
            ((TextInputControl)field).setPromptText("Enter " + labelText.toLowerCase().replace(":", ""));
        }
        grid.add(field, 1, row);
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btn.setPrefWidth(150);
        return btn;
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

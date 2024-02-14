package com.example.grade_management;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

import java.sql.*;

public class HelloController {

    @FXML
    private TextField StudentName_addStudents, StudentID_addStudents, StudentID_viewGrades, course1_Grade, course2_Grade, course3_Grade, course4_Grade, course5_Grade, StudentID_viewGrade, studentID_gradeCalc;
    @FXML
    private Button addStudents_Button, calculateGrade, viewReport, generateID;
    @FXML
    private ComboBox<String> comboBox_calculator, comboBox_viewGrade, comboBox_courses;
    @FXML
    private Label grade1, grade2, grade3, grade4, grade5, GPA, viewGrade_generate;
    @FXML
    private TableView<String> tableView;
    @FXML
    private AnchorPane reportView;


    private boolean isNumeric(String str) { // isNumeric method returns true if the input string can be successfully converted to a double, and false otherwise. It can be used to check whether a given string represents a numeric value or not.
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void initialize() {
        // Create an ObservableList with the items
        ObservableList<String> items = FXCollections.observableArrayList("exams", "project", "assignment");
        ObservableList<String> courses = FXCollections.observableArrayList("course1", "course2", "course3", "course4", "course5");

        // Set the items to the comboBox_calculator && comboBox_viewGrades
        comboBox_calculator.setItems(items);
        comboBox_viewGrade.setItems(items);
        comboBox_courses.setItems(courses);

    }

    public void calclGrade() {
        // Validate and get values from text fields
        int grade1Value = getAndValidateGrade(course1_Grade, "Course 1");
        int grade2Value = getAndValidateGrade(course2_Grade, "Course 2");
        int grade3Value = getAndValidateGrade(course3_Grade, "Course 3");
        int grade4Value = getAndValidateGrade(course4_Grade, "Course 4");
        int grade5Value = getAndValidateGrade(course5_Grade, "Course 5");

        // Assign grades based on the grading scale
        String grade1Letter = assignGrade(grade1Value);
        String grade2Letter = assignGrade(grade2Value);
        String grade3Letter = assignGrade(grade3Value);
        String grade4Letter = assignGrade(grade4Value);
        String grade5Letter = assignGrade(grade5Value);

        // Display grades in corresponding labels
        grade1.setText(grade1Letter);
        grade2.setText(grade2Letter);
        grade3.setText(grade3Letter);
        grade4.setText(grade4Letter);
        grade5.setText(grade5Letter);

        // Calculate GPA for each course
        double GPA1 = getGPAValue(grade1Letter);
        double GPA2 = getGPAValue(grade2Letter);
        double GPA3 = getGPAValue(grade3Letter);
        double GPA4 = getGPAValue(grade4Letter);
        double GPA5 = getGPAValue(grade5Letter);

        // Calculate average GPA
        double averageGPA = (GPA1 + GPA2 + GPA3 + GPA4 + GPA5) / 5.0;

        // Display average GPA in the corresponding label
        GPA.setText(String.format("%.2f", averageGPA));

        // Update grades in the database
        updateDatabaseGrades(studentID_gradeCalc.getText(), comboBox_calculator.getValue(),
                grade1Letter, grade2Letter, grade3Letter, grade4Letter, grade5Letter);

        // Update average GPA in the "general" table
        updateAverageGPA(studentID_gradeCalc.getText(), averageGPA);
    }

    private int getAndValidateGrade(TextField textField, String courseName) {
        try {
            int gradeValue = Integer.parseInt(textField.getText().trim());

            // Check if the grade is within the valid range
            if (gradeValue < 0 || gradeValue > 100) {
                // Show a pop-up message for invalid input
                showAlert(AlertType.ERROR, "Invalid Input", "Please enter a valid grade (0 - 100) for " + courseName + ".");
                return -1; // Return a sentinel value to indicate an error
            }

            return gradeValue;
        } catch (NumberFormatException e) {
            // Show a pop-up message for non-numeric input
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter a valid numeric grade for " + courseName + ".");
            return -1; // Return a sentinel value to indicate an error
        }
    }

    private String assignGrade(int gradeValue) {
        // Assign grades based on the grading scale
        if (gradeValue >= 80 && gradeValue <= 100) return "A";
        else if (gradeValue >= 70 && gradeValue <= 79) return "B+";
        else if (gradeValue >= 60 && gradeValue <= 69) return "B-";
        else if (gradeValue >= 55 && gradeValue <= 59) return "C+";
        else if (gradeValue >= 50 && gradeValue <= 54) return "C";
        else if (gradeValue >= 45 && gradeValue <= 49) return "D+";
        else if (gradeValue >= 40 && gradeValue <= 44) return "D";
        else return "F";
    }

    private double getGPAValue(String letterGrade) {
        switch (letterGrade) {
            case "A":
                return 4.0;
            case "B+":
                return 3.5;
            case "B-":
                return 3.0;
            case "C+":
                return 2.5;
            case "C":
                return 2.0;
            case "D+":
                return 1.5;
            case "D":
                return 1.0;
            default:
                return 0.0; // For grade "F"
        }
    }

    private void updateDatabaseGrades(String studentID, String tableName, String grade1, String grade2,
                                      String grade3, String grade4, String grade5) {
        // JDBC variables
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Load JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:Grade_Management.db");

            // Check if the student ID exists in the specified table
            String checkStudentQuery = "SELECT * FROM " + tableName + " WHERE Student_ID = ?";
            preparedStatement = connection.prepareStatement(checkStudentQuery);
            preparedStatement.setString(1, studentID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Student ID found, update existing row with new grades
                String updateGradesQuery = "UPDATE " + tableName +
                        " SET course1 = ?, course2 = ?, course3 = ?, course4 = ?, course5 = ?" +
                        " WHERE Student_ID = ?";
                preparedStatement = connection.prepareStatement(updateGradesQuery);
                preparedStatement.setString(1, grade1);
                preparedStatement.setString(2, grade2);
                preparedStatement.setString(3, grade3);
                preparedStatement.setString(4, grade4);
                preparedStatement.setString(5, grade5);
                preparedStatement.setString(6, studentID);
                preparedStatement.executeUpdate();

                // Show success message
                showAlert(AlertType.INFORMATION, "Grades Update", "Grades successfully updated for student with ID: " + studentID);
            } else {
                // Student ID not found, insert a new row with grades
                String insertGradesQuery = "INSERT INTO " + tableName +
                        " (Student_ID, course1, course2, course3, course4, course5)" +
                        " VALUES (?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(insertGradesQuery);
                preparedStatement.setString(1, studentID);
                preparedStatement.setString(2, grade1);
                preparedStatement.setString(3, grade2);
                preparedStatement.setString(4, grade3);
                preparedStatement.setString(5, grade4);
                preparedStatement.setString(6, grade5);
                preparedStatement.executeUpdate();

                // Show success message
                showAlert(AlertType.INFORMATION, "Grades Insert", "Grades successfully inserted for student with ID: " + studentID);
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error connecting to the database.");
        } finally {
            // Close JDBC resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAverageGPA(String studentID, double averageGPA) {
        // JDBC variables
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Load JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:Grade_Management.db");

            // Check if the student ID exists in the "general" table
            String checkStudentQuery = "SELECT * FROM general WHERE Student_ID = ?";
            preparedStatement = connection.prepareStatement(checkStudentQuery);
            preparedStatement.setString(1, studentID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Student ID found, update existing row with new average GPA
                double existingAverageGPA = resultSet.getDouble("averageGPA");
                double updatedAverageGPA = (existingAverageGPA + averageGPA) / 2.0;

                String updateAverageGPAQuery = "UPDATE general SET averageGPA = ? WHERE Student_ID = ?";
                preparedStatement = connection.prepareStatement(updateAverageGPAQuery);
                preparedStatement.setDouble(1, updatedAverageGPA);
                preparedStatement.setString(2, studentID);
                preparedStatement.executeUpdate();

                // Show success message
                showAlert(AlertType.INFORMATION, "Average GPA Update", "Average GPA successfully updated for student with ID: " + studentID);
            } else {
                // Student ID not found, insert a new row with average GPA
                String insertAverageGPAQuery = "INSERT INTO general (Student_ID, averageGPA) VALUES (?, ?)";
                preparedStatement = connection.prepareStatement(insertAverageGPAQuery);
                preparedStatement.setString(1, studentID);
                preparedStatement.setDouble(2, averageGPA);
                preparedStatement.executeUpdate();

                // Show success message
                showAlert(AlertType.INFORMATION, "Average GPA Insert", "Average GPA successfully inserted for student with ID: " + studentID);
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error connecting to the database.");
        } finally {
            // Close JDBC resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }







    public void viewReport() {
        // JDBC variables
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:Grade_Management.db");
             Statement statement = connection.createStatement()) {

            // Query to retrieve data from the 'general' table in descending order of GPA
            String query = "SELECT * FROM general ORDER BY averageGPA DESC";
            ResultSet resultSet = statement.executeQuery(query);

            // Get column metadata to determine the number of columns in the result set
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numColumns = metaData.getColumnCount();

            // Create a TableView to display the data
            TableView<ObservableList<String>> tableView = new TableView<>();

            // Create columns dynamically based on the number of columns in the result set
            for (int i = 1; i <= numColumns; i++) {
                final int columnIndex = i - 1;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(metaData.getColumnName(i));

                // Define a cell value factory to populate the table cells
                column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(columnIndex)));

                // Add the column to the table
                tableView.getColumns().add(column);
            }

            // Populate the table data
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= numColumns; i++) {
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }
            tableView.setItems(data);

            // Set the table's size to fit the anchor pane
            tableView.prefHeightProperty().bind(reportView.heightProperty());
            tableView.prefWidthProperty().bind(reportView.widthProperty());

            // Add the table to the anchor pane
            reportView.getChildren().clear();
            reportView.getChildren().add(tableView);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }




    public void addStudents() {
        // Get values from text fields
        String studentID = StudentID_addStudents.getText().trim();
        String studentName = StudentName_addStudents.getText().trim();

        // Check if studentID is a number
        if (!isNumeric(studentID)) {
            // Show a pop-up message for invalid input
            showAlert(AlertType.ERROR, "Invalid Input", "Please enter a valid numeric Student ID.");
            return; // Stop further execution
        }

        // Check if studentID is already present in the database
        if (isStudentIDAlreadyExists(studentID)) {
            // Show a pop-up message
            showAlert(AlertType.ERROR, "Duplicate Student ID", "Student ID already exists. Please try again.");
        } else {
            // Insert new student into the database
            insertStudentIntoDatabase(studentID, studentName);

            // Show a pop-up message indicating successful addition
            showAlert(AlertType.INFORMATION, "Success", "Student added successfully!");
        }
    }

    private boolean isStudentIDAlreadyExists(String studentID) {
        // JDBC connection parameters
        String url = "jdbc:sqlite:Grade_Management.db";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM general WHERE Student_ID = ?")) {

            preparedStatement.setString(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            // If resultSet has any rows, then studentID already exists
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertStudentIntoDatabase(String studentID, String studentName) {
        // JDBC connection parameters
        String url = "jdbc:sqlite:Grade_Management.db";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO general (Student_ID, Student_Name) VALUES (?, ?)")) {

            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, studentName);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void generateID() {
        // Get selected table name from comboBox_viewGrade
        String selectedTableName = comboBox_viewGrade.getValue();

        // Get selected course name from comboBox_courses
        String selectedCourseName = comboBox_courses.getValue();

        // Get student ID from textField StudentID_viewGrades
        String studentID = StudentID_viewGrades.getText();

        if (selectedTableName == null || selectedCourseName == null || studentID.isEmpty()) {
            showAlert(AlertType.ERROR, "Incomplete Selection", "Please select a table, a course, and enter a student ID.");
            return;
        }

        // JDBC variables
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:Grade_Management.db");
             Statement statement = connection.createStatement()) {

            // Query to retrieve data for the selected course and student ID
            String query = "SELECT " + selectedCourseName +
                    " FROM " + selectedTableName +
                    " WHERE Student_ID = '" + studentID + "'";

            ResultSet resultSet = ((Statement) statement).executeQuery(query);

            if (resultSet.next()) {
                // Retrieve and display the data on label viewGrade_generate
                String courseGrade = resultSet.getString(selectedCourseName);
                viewGrade_generate.setText(courseGrade);
            } else {
                // No data found for the specified student and course
                showAlert(AlertType.ERROR, "Data Not Found", "No data found for student ID: " + studentID +
                        " and course: " + selectedCourseName);
                viewGrade_generate.setText(""); // Clear the label
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error connecting to the database.");
        }
    }






}




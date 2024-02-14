module com.example.grade_management {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.grade_management to javafx.fxml;
    exports com.example.grade_management;
}
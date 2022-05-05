module com.example.clientchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clientchat to javafx.fxml;
    exports com.example.clientchat;
    exports com.example.clientchat.controllers;
    opens com.example.clientchat.controllers to javafx.fxml;
}
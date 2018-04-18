package com.zhangqianli.tbminer.nestedcontrollers2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NestedJavaFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/tbminer/nestedcontrollers2/f1.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/tbminer/nestedcontrollers2/sample.css");
        primaryStage.setTitle("Nested JavaFX Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

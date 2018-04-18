package com.zhangqianli.tbminer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TimeSlices extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/tbminer/TimeSlices.fxml"));
		primaryStage.setTitle("单日施工片段分析");
		Scene timeSlices = new Scene(root);
		timeSlices.getStylesheets().add("/tbminer/Blank.css");
		primaryStage.setScene(timeSlices);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

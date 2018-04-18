package com.zhangqianli.tbminer;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TBMiner extends Application {
	private static Stage baseStage;//用静态变量来引用这个primaryStage
	public TBMiner() {
		baseStage = new Stage();
	}
	@Override
	public void start(Stage primaryStage) throws IOException {
			baseStage = primaryStage;
			Parent root = FXMLLoader.load(getClass().getResource("/tbminer/TBMiner.fxml"));
			primaryStage.setTitle("TBMiner");
			Scene tbminer = new Scene(root);
//			tbminer.getStylesheets().add("/tbminer/TBMiner.css");
			tbminer.getStylesheets().add("/tbminer/Blank.css");
			primaryStage.setScene(tbminer);
//			primaryStage.setScene(new Scene(root));
			primaryStage.show();
//			要先创建stage，再创建Pane，然后再创建scene
		
	}
	public static void main(String[] args) {
		launch(args);
	}
	public static void switchScene(Scene newScene)
	{
		baseStage.setScene(newScene);
	}
}

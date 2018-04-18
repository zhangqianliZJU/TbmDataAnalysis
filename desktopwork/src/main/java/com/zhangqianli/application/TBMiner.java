package com.zhangqianli.application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 
 * @author zhangqianli
 * 这是整个TBMiner程序的入口
 *
 */
public class TBMiner extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/MainWindow.fxml"));
			primaryStage.setTitle("TBMiner");
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
//			要先创建stage，再创建Pane，然后再创建scene
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

package com.zhangqianli.tbminer.dynamic.chart;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 这个是可以运行的程序，下一步将其改成可以按照施工单个施工片段动态更新的模样
 */
public class DynamicBirch2 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/tbminer/TBMiner2.fxml"));
            primaryStage.setTitle("TBMiner");
            Scene tbminer = new Scene(root);
//            tbminer.getStylesheets().add("/tbminer/Blank.css");
            primaryStage.setScene(tbminer);
            primaryStage.show();
        } catch (IOException e) {
        }
    }
}

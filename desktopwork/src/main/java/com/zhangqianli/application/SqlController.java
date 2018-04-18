package com.zhangqianli.application;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SqlController {
	@FXML
	private AnchorPane firstPane;
	@FXML
	private Menu sqlMenu;
	@FXML
	private MenuItem importMenuItem;
	@FXML
	private MenuItem statisticMenuItem;
	@FXML
	private MenuItem singleDayQuery;

	public void importTxtToSql(ActionEvent event) {
		System.out.println("SQL菜单下的菜单项-\"导入TXT格式数据\"被选中");
		Stage primaryStage = new Stage();
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/application/ImportTxtWindow.fxml"));
			primaryStage.setTitle("ImportTxt");
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void SqlTableStatistics(ActionEvent event) {

		System.out.println("SQL菜单下的菜单项-\"数据库统计信息\"被选中");
		Stage primaryStage = new Stage();
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/application/StatisticsWindow.fxml"));
			primaryStage.setTitle("StaticsWindow");
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void singleDayQuery(ActionEvent event) {//应该是在这里初始化comboBox呢，还是在SingleDayQueryController里面
		System.out.println("SQL菜单下的菜单项-\"单日施工数据分析\"被选中");
		Stage primaryStage = new Stage();
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/application/SingleDayQuery.fxml"));
			//怎样在这里获取combox呢
			primaryStage.setTitle("单日施工数据分析");
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

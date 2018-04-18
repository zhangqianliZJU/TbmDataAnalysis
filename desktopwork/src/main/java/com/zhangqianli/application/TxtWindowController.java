package com.zhangqianli.application;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import com.zhangqianli.jdbc.ImportMultiTxtToMysqlCleaned;
import com.zhangqianli.jdbc.SqlConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class TxtWindowController {
	private String sqlurl;
	private String username;
	private String password;
	List<File> files;//存储txt文件列表
	ObservableList<String> listViewContent = FXCollections.observableArrayList();
	@FXML
	private AnchorPane importTxtWindow;
	@FXML
	private PasswordField passWord;
	@FXML
	private TextField sqlURL;
	@FXML
	private TextField userName;
	@FXML
	private Button submitButton;
	@FXML 
	private Button resetButton;
	@FXML
	private Button openTxtButton;
	@FXML
	private Button importButton;
	@FXML
	private GridPane gridPane;	
	@FXML
	private ListView<String> listView;
	
	public void onSubmitClick(ActionEvent event) {
		sqlurl = sqlURL.getText();
		username = userName.getText();
		password = passWord.getText();
		System.out.println("数据库信息如下：");
		System.out.println("SqlURL: " + sqlurl);
		System.out.println("username: " + username);
		System.out.println("password: " + password);
	}
	public void onResetButtonClick(ActionEvent event) {
		sqlURL.clear();
		userName.clear();
		passWord.clear();
	}
	public void onOpenTxtButtonClick() {
		Stage stage = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("选择Txt文件");
		fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Text Files", "*.txt"),
		         new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
		         new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
		         new ExtensionFilter("All Files", "*.*"));
		files = fileChooser.showOpenMultipleDialog(stage);
		
		
		listView.setItems(listViewContent);
		listView.setOrientation(Orientation.VERTICAL);
		File temp;		
		String name;
		if (files.size() > 0) {
			for(int i =0;i<files.size();i++)
			{
				temp = files.get(i);
				name = temp.getName();
				listViewContent.add(name);
				System.out.println(name);
			}
		}
		else {
			System.out.println("并没有选中文件，请再次选择！");
		}
	}
	public void importTxt() throws ReflectiveOperationException, Exception {
		Connection connect = SqlConnection.getConnection(sqlurl, username, password);
		ImportMultiTxtToMysqlCleaned.importTxts(connect, files);
	}
}

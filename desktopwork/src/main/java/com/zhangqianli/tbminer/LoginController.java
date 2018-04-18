package com.zhangqianli.tbminer;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class LoginController {
	private static final String USERNAME = "zhangqianli";
	private static final String PASSWORD = "tian123kong";
	private String userName;
	private String passWord;
	@FXML
	private TextField account;
	@FXML
	private PasswordField password;
	@FXML
	private Button reset;
	@FXML
	private Button submit;
	@FXML
	private Label notice;
	public void onReset() {
		account.clear();
		password.clear();
	}
	public void onSubmit() throws IOException
	{
		userName = account.getText();
		passWord = password.getText();
		if(userName.equals(USERNAME) && passWord.equals(PASSWORD))
		{
			notice.setText("用户信息正确");
			notice.setTextFill(Color.BLUE);
			Parent root = FXMLLoader.load(getClass().getResource("/tbminer/TBMiner.fxml"));
			Scene tbminer = new Scene(root);
			tbminer.getStylesheets().add("/tbminer/TBMiner.css");
			TBMiner.switchScene(tbminer);
//			Scene scene = submit.getScene();
//			primaryStage.
			
		}
		else {
			notice.setText("用户名或密码错误，请重新输入");
			notice.setTextFill(Color.RED);
			onReset();
		}
	}
}

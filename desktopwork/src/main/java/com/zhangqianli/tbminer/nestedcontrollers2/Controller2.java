package com.zhangqianli.tbminer.nestedcontrollers2;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class Controller2 {
    @FXML
    Button btn4;
    @FXML
    Button btn5;
    @FXML
    Button btn6;

    @FXML TextArea tx4;
    @FXML TextArea tx5;
    @FXML TextArea tx6;


    public void onBtn4() {
        tx4.appendText("Clicked!!!\n");
        //tx4.setText("Button4 is clicked!");
    }
    public void onBtn5() {
        tx5.appendText("clicked!!!\n");
    }
    public void onBtn6() {
        tx6.appendText("clicked!!!\n");
    }
}

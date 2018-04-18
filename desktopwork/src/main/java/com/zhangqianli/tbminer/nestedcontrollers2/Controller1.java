package com.zhangqianli.tbminer.nestedcontrollers2;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller1 implements Initializable {
    @FXML
    private Button btn1;

    /**
     * 这种获取嵌套controller的方式不靠谱
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
/*        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("/tbminer/nestedcontrollers/f3.fxml").openStream());
            ct2 = (Controller2) loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

   /*     try {
            URL location1 = getClass().getResource("/tbminer/nestedcontrollers/f3.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location1);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent root = fxmlLoader.load(location1.openStream());
            ct2=fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @FXML
    private Button btn2;
    @FXML
    private Button btn3;

    @FXML
    private TextArea tx1;
    @FXML
    private TextArea tx2;
    @FXML
    private TextArea tx3;

    @FXML
    GridPane gridPane2;
    @FXML//注意这个fx:include包含进来的节点和他的控制器的命名规则
    //终于解决了嵌套控制器的问题，原来是控制器的命名出现了错误
    private Controller2 gridPane2Controller;

    public void onBtn1() {
        // tx1.setText("Button1 is clicked!");
        tx1.appendText("Button1 is clicked!\n");
        gridPane2Controller.onBtn4();
    }

    public void onBtn2() {
        tx2.appendText("Button2 is clicked!\n");
        gridPane2Controller.onBtn5();
    }

    public void onBtn3() {
        tx3.appendText("Button3 is clicked!\n");
        gridPane2Controller.onBtn6();
    }
}

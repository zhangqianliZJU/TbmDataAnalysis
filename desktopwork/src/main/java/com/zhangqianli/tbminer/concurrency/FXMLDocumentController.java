package com.zhangqianli.tbminer.concurrency;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {
    @FXML
    private ProgressBar progress1;
    @FXML
    private ProgressIndicator progress2;
    @FXML
    private Text progress3;
    @FXML
    private Button btn1;
    public void handleButtonAction(ActionEvent event){
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        for (int a = 1;a<=100;a++){
                            updateValue("process:"+a+"%");
                            updateProgress(a, 100);
                            Thread.sleep(1000);

                        }
                        return "Success";
                    }
                };
            }
        };
        progress1.progressProperty().bind(service.progressProperty());
        progress2.progressProperty().bind(service.progressProperty());
        progress3.textProperty().bind(service.valueProperty());
        service.setOnSucceeded((WorkerStateEvent e) -> {System.out.println("任务成功执行");} );
        service.start();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn1.setOnAction(e -> handleButtonAction(e));
    }
}

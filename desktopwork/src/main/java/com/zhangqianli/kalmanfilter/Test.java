package com.zhangqianli.kalmanfilter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            File file = new File("config/KalmanFilter1.txt");
            FileInputStream fStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fStream);
            BufferedReader bfReader = new BufferedReader(reader);
            ArrayList<Double> input = new ArrayList<>();
            String temp;
            while (null != (temp = bfReader.readLine())){
                input.add(Double.parseDouble(temp));
            }
            KalmanFilter kfFiler = new KalmanFilter(input);
            kfFiler.filter();
            ArrayList<Double> output = kfFiler.getOutput();

            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("时间序列编号");
            yAxis.setLabel("推进力曲线");
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            //需要设置一下scatterchart图标的性质，不然感觉javafx渲染不过来啊
            lineChart.setAnimated(false);

            lineChart.setTitle("Kalman滤波效果对比");
            XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
            XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
            for(int i=0;i<input.size();i++){

                series1.getData().add(new XYChart.Data<>(i, input.get(i)));
                series2.getData().add(new XYChart.Data<>(i, output.get(i)));
            }
            lineChart.getData().add(series1);
            lineChart.getData().add(series2);
            series1.setName("原始曲线");
            series2.setName("KalmanFilter结果");
            StackPane root = new StackPane();
            Scene promptScene = new Scene(root, 1000, 800);
            promptScene.getStylesheets().add("/tbminer/Chart2.css");
            root.getChildren().add(lineChart);
            primaryStage.setScene(promptScene);
            primaryStage.show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}

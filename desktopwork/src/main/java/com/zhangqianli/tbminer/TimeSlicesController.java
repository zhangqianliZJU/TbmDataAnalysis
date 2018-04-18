package com.zhangqianli.tbminer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.zhangqianli.dbcp.DBCPUtil;
import com.zhangqianli.jdbc.SqlConnection;
import com.zhangqianli.jdbc.TimeSlice;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

public class TimeSlicesController {
    String date;
    List<ArrayList<Time>> timeSlices;
    String slice;
    int index;
    String attr;
    @FXML
    private ComboBox<String> dateSelect;
    @FXML
    private Button submit1;
    @FXML
    private ComboBox<String> sliceSelect;
    @FXML
    private ComboBox<String> attrSelect;
    @FXML
    private Button submit2;
    @FXML
    private TextArea summary;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab result;

    public void onDateSelect() {
        date = dateSelect.getValue();
        summary.setPrefColumnCount(20);
        summary.setPrefRowCount(10);
        summary.setWrapText(true);
        print("选中的施工日期为：" + date);
    }

    public void onSubmit1() throws ReflectiveOperationException, Exception {
        System.out.println("选中日期为：" + date);
        timeSlices = TimeSlice.fetchTimeSlices(date);
        int count = timeSlices.size();
        for (int i = 0; i < count; i++) {
            sliceSelect.getItems().add("Slices" + (i + 1));
        }
        print("施工片段的数量为：" + count);
    }

    public void onSliceSelect() {
        slice = sliceSelect.getValue();
        System.out.println("选中的片段为" + slice);
        index = Integer.parseInt(slice.substring(6)) - 1;
        print("选中的施工片段为：" + slice);
    }

    public void onAttrSelect() {
        attr = attrSelect.getValue();
        System.out.println("选中属性为" + attr);
        print("选中的属性为：" + attr);

    }

    public void onSubmit2() throws Exception {
        ArrayList<Time> temp = timeSlices.get(index);

        String sqlURL = "jdbc:mysql://localhost:3306/tbm";
        String user = "root";
        String password = "tian123kong";
        Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
        PreparedStatement ps = sqlconnect
                .prepareStatement("SELECT * FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
        String start = date + " " + temp.get(0);
        String end = date + " " + temp.get(temp.size() - 1);
        // ps.setString(1, attr);
        ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
        ps.setTimestamp(2, Timestamp.valueOf(end));
        ResultSet rs = ps.executeQuery();

        rs.last();// 移动到返回结果的最后一行
        int rowCount = rs.getRow();// 获取行数

        rs.beforeFirst();// 重新回到结果的第一行
        System.out.format("返回结果总共有 %d 行 \n", rowCount);
        print("选中的施工片段包括的施工记录为：" + rowCount);
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("时间编号");
        LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setTitle("TBM施工");
        // defining a series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(attr + "单日工作曲线");
        int i = 0;
        // populating the series with data
        while (rs.next()) {
            series.getData().add(new XYChart.Data<Number, Number>(i, rs.getDouble(attr)));
            i++;
            // rs.getDouble(columnLabel)
        }
        lineChart.getData().add(series);
        Tab newTab = new Tab(attr);
        newTab.setContent(lineChart);
        tabPane.getTabs().add(newTab);
        sqlconnect.close();
    }

    /**
     * 考虑用多线程提高运算速度
     *
     * @throws Exception
     */
    public void onSubmit3() throws Exception {

        //先使用一下Platform.runLater(),可以啊，这个速度真的很快，牛逼啊
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Time> temp = timeSlices.get(index);
                    Connection sqlconnect = DBCPUtil.getConnection();
                    PreparedStatement ps = sqlconnect
                            .prepareStatement("SELECT * FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                    String start = date + " " + temp.get(0);
                    String end = date + " " + temp.get(temp.size() - 1);
                    // ps.setString(1, attr);
                    ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                    ps.setTimestamp(2, Timestamp.valueOf(end));
                    ResultSet rs = ps.executeQuery();
                    rs.last();// 移动到返回结果的最后一行
                    int rowCount = rs.getRow();// 获取行数

                    rs.beforeFirst();// 重新回到结果的第一行
                    //System.out.format("返回结果总共有 %d 行 \n", rowCount);
                    print("选中的施工片段包括的施工记录为：" + rowCount);
                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("时间轴");
                    LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
                    lineChart.setTitle(attr + "施工数据");
                    // defining a series
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(attr + "单日工作曲线");
                    int i = 0;
                    // populating the series with data
                    double[] noiseSignal = new double[rowCount];
                    while (rs.next()) {
                        series.getData().add(new XYChart.Data<>(i, rs.getDouble(attr)));
                        noiseSignal[i] = rs.getDouble(attr);
                        i++;
                        // rs.getDouble(columnLabel)
                    }

                    lineChart.getData().add(series);
                    Tab newTab = new Tab(attr);
                    newTab.setContent(lineChart);
                    tabPane.getTabs().add(newTab);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //多线程里面的Service使用方法
    public void onSubmit4() {

        Service<String> plot = new Service<String>() {
            @Override
            protected Task<String> createTask() {

                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        try {
                            ArrayList<Time> temp = timeSlices.get(index);
                            Connection sqlconnect = DBCPUtil.getConnection();
                            PreparedStatement ps = sqlconnect
                                    .prepareStatement("SELECT * FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                            String start = date + " " + temp.get(0);
                            String end = date + " " + temp.get(temp.size() - 1);
                            // ps.setString(1, attr);
                            ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                            ps.setTimestamp(2, Timestamp.valueOf(end));
                            ResultSet rs = ps.executeQuery();
                            rs.last();// 移动到返回结果的最后一行
                            int rowCount = rs.getRow();// 获取行数

                            rs.beforeFirst();// 重新回到结果的第一行
                            //System.out.format("返回结果总共有 %d 行 \n", rowCount);
                            print("选中的施工片段包括的施工记录为：" + rowCount);
                            final NumberAxis xAxis = new NumberAxis();
                            final NumberAxis yAxis = new NumberAxis();
                            xAxis.setLabel("时间轴");
                            LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
                            lineChart.setTitle(attr + "施工数据");
                            // defining a series
                            XYChart.Series<Number, Number> series = new XYChart.Series<>();
                            series.setName(attr + "单日工作曲线");
                            int i = 0;
                            // populating the series with data
                            double[] noiseSignal = new double[rowCount];
                            while (rs.next()) {
                                series.getData().add(new XYChart.Data<>(i, rs.getDouble(attr)));
                                noiseSignal[i] = rs.getDouble(attr);
                                i++;
                                // rs.getDouble(columnLabel)
                            }

                            lineChart.getData().add(series);
                            Tab newTab = new Tab(attr);
                            newTab.setContent(lineChart);
                            tabPane.getTabs().add(newTab);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return "Plot job is done";
                    }
                };
            }
        };
        plot.start();
    }
    public void onSubmit5() {

        Task<String> task =  new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    ArrayList<Time> temp = timeSlices.get(index);
                    Connection sqlconnect = DBCPUtil.getConnection();
                    PreparedStatement ps = sqlconnect
                            .prepareStatement("SELECT * FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                    String start = date + " " + temp.get(0);
                    String end = date + " " + temp.get(temp.size() - 1);
                    // ps.setString(1, attr);
                    ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                    ps.setTimestamp(2, Timestamp.valueOf(end));
                    ResultSet rs = ps.executeQuery();
                    rs.last();// 移动到返回结果的最后一行
                    int rowCount = rs.getRow();// 获取行数

                    rs.beforeFirst();// 重新回到结果的第一行
                    //System.out.format("返回结果总共有 %d 行 \n", rowCount);
                    print("选中的施工片段包括的施工记录为：" + rowCount);
                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("时间轴");
                    LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
                    lineChart.setTitle(attr + "施工数据");
                    // defining a series
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(attr + "单日工作曲线");
                    int i = 0;
                    // populating the series with data
                    double[] noiseSignal = new double[rowCount];
                    while (rs.next()) {
                        series.getData().add(new XYChart.Data<>(i, rs.getDouble(attr)));
                        noiseSignal[i] = rs.getDouble(attr);
                        i++;
                        // rs.getDouble(columnLabel)
                    }

                    lineChart.getData().add(series);
                    Tab newTab = new Tab(attr);
                    newTab.setContent(lineChart);
                    tabPane.getTabs().add(newTab);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return "Plot job is done";
            }
        };
        Platform.runLater(task);
    }
    public void print(String input) {
        //summary.setText(summary.getText() + "/n" + input);
        summary.appendText(input + "\n");//牛逼了，终于实现控制台功能。
    }
}

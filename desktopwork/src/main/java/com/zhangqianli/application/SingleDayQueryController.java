package com.zhangqianli.application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.zhangqianli.jdbc.SqlConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class SingleDayQueryController {
	private String date;
	private ResultSet rs;
	private String attr;
	private int rowCount;

	@FXML
	private Button submit;
	@FXML
	private Button submit1;
	@FXML
	private ComboBox<String> dateSelection;
	@FXML
	private ComboBox<String> attrSelection;
	// @FXML
	// private LineChart lineChart;
	@FXML
	LineChart<Number, Number> lineChart;

	public void onDateSelection() {
		date = dateSelection.getValue().toString();
		System.out.println("选中的日期为：" + date);
	}

	public void onAttrSelection() {
		attr = attrSelection.getValue().toString();
		System.out.println("被选中的属性为：" + attr);
	}

	public void onSubmit() throws ReflectiveOperationException, Exception {
		System.out.println("被选中的日期为：" + date);
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		PreparedStatement ps = sqlconnect
				.prepareStatement("select * from tbmrecordcleaned where attr3 >= ? and attr3 <= ?");
		String start = date + " 00:00:00";
		String end = date + " 23:59:59";
		ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
		ps.setTimestamp(2, Timestamp.valueOf(end));
		rs = ps.executeQuery();
		rs.last();// 移动到返回结果的最后一行
		rowCount = rs.getRow();// 获取行数

		rs.beforeFirst();// 重新回到结果的第一行
		System.out.format("返回结果总共有 %d 行 \n", rowCount);
		// sqlconnect.close();// 在这里关闭还不一定合适
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onSubmit1() throws SQLException {
		String attrIndex = attr.substring(4);
		Integer index = Integer.parseInt(attrIndex);
		System.out.println("被选中的属性所在的列为：" + attrIndex);

		System.out.println("我被点击了");
		lineChart.setTitle("TBM施工");
		// defining a series
		XYChart.Series series = new XYChart.Series();
		series.setName(attr + "单日工作曲线");
		int i = 0;
		// populating the series with data
		while (rs.next() && i <1000) {
			series.getData().add(new XYChart.Data(i, rs.getDouble(index)));
			i++;
		}
		// lineChart.getData().add(series);
		// Scene scene = new Scene(lineChart, 800, 600);
		lineChart.getData().add(series);
		// Stage stage = new Stage();
		// stage.setScene(scene);
		// stage.show();
	}

	public void onSubmit2() {
		System.out.println("我被点击了");
		lineChart.setTitle("Stock Monitoring, 2010");
		// defining a series
		XYChart.Series series = new XYChart.Series();
		series.setName("My portfolio");
		// populating the series with data
		series.getData().add(new XYChart.Data(1, 23));
		series.getData().add(new XYChart.Data(2, 14));
		series.getData().add(new XYChart.Data(3, 15));
		series.getData().add(new XYChart.Data(4, 24));
		series.getData().add(new XYChart.Data(5, 34));
		series.getData().add(new XYChart.Data(6, 36));
		series.getData().add(new XYChart.Data(7, 22));
		series.getData().add(new XYChart.Data(8, 45));
		series.getData().add(new XYChart.Data(9, 43));
		series.getData().add(new XYChart.Data(10, 17));
		series.getData().add(new XYChart.Data(11, 29));
		series.getData().add(new XYChart.Data(12, 25));
		// Scene scene = new Scene(lineChart, 800, 600);
		lineChart.getData().add(series);
		// Stage stage = new Stage();
		// stage.setScene(scene);
		// stage.show();
	}
}

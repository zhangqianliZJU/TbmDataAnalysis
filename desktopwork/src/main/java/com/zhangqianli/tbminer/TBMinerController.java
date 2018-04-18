package com.zhangqianli.tbminer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.zhangqianli.algorythm.GroupAttributes;
import com.zhangqianli.jdbc.ImportMultiTxtToMysqlCleaned;
import com.zhangqianli.jdbc.SqlConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class TBMinerController {
	private String sqlurl;
	private String username;
	private String password;
	List<File> files;// 存储txt文件列表
	ObservableList<String> listViewContent = FXCollections.observableArrayList();

	private String date;
	private ResultSet rs;
	private String attr;
	private int rowCount;

	@FXML
	private BorderPane borderPane;
	@FXML
	private MenuBar menuBar;
	@FXML
	private Menu fileMenu;
	@FXML
	private Menu editMenu;
	@FXML
	private Menu helpMenu;
	@FXML
	private ScrollPane scrollPaneLeft;
	@FXML
	private ComboBox<String> sqlURL;
	@FXML
	private TextField userName;
	@FXML
	private TextField passWord;
	@FXML
	private Button reset;
	@FXML
	private Button submit;
	@FXML
	private Button fileChoose;
	@FXML
	private Button fileImport;
	@FXML
	private ListView<String> listView;
	@FXML
	private ComboBox<String> dateSelection;
	@FXML
	private ComboBox<String> attrSelection;
	@FXML
	private ComboBox<String> groupAttributes;
	@FXML
	private Button dateSubmit;
	@FXML
	private Button attrSubmit;
	@FXML
	private Button groupSubmit;
	@FXML
	private TabPane centerTabPane;

	public void onReset() {
		userName.clear();
		passWord.clear();
		// sqlURL该执行什么方法
		sqlURL.hide();
		System.out.println("重置数据库连接信息");
	}

	public void onSubmit() {
		sqlurl = sqlURL.getValue();
		username = userName.getText();
		password = passWord.getText();
		System.out.println("数据库信息如下：");
		System.out.println("SqlURL: " + sqlurl);
		System.out.println("username: " + username);
		System.out.println("password: " + password);
	}

	// 怎样实现控制台功能，需要去拦截PrintStream信息。
	public void onFileChoose() {
		Stage stage = new Stage();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("选择Txt文件");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"), new ExtensionFilter("All Files", "*.*"));
		files = fileChooser.showOpenMultipleDialog(stage);

		listView.setItems(listViewContent);
		listView.setOrientation(Orientation.VERTICAL);
		File temp;
		String name;
		listViewContent.add("选中的txt数据文件为：");
		if (files.size() > 0) {
			for (int i = 0; i < files.size(); i++) {
				temp = files.get(i);
				name = temp.getName();
				listViewContent.add(name);
				System.out.println(name);
			}
		} else {
			System.out.println("并没有选中文件，请再次选择！");
		}
	}

	public void onFileImport() throws ReflectiveOperationException, Exception {
		Connection connect = SqlConnection.getConnection(sqlurl, username, password);
		ImportMultiTxtToMysqlCleaned.importTxts(connect, files);
		connect.close();
	}

	public void onDateSelection() {
		date = dateSelection.getValue().toString();
		System.out.println("选中的日期为：" + date);
	}

	public void onAttrSelection() {
		attr = attrSelection.getValue().toString();
		System.out.println("被选中的属性为：" + attr);
	}

	public void onDateSubmit() throws ReflectiveOperationException, Exception {
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
	public void onAttrSubmit() throws SQLException {
		rs.beforeFirst();
		String attrIndex = attr.substring(4);
		Integer index = Integer.parseInt(attrIndex);
		System.out.println("被选中的属性所在的列为：" + attrIndex);
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("时间编号");
		LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		System.out.println("我被点击了");
		lineChart.setTitle("TBM施工");
		// defining a series
		Series series = new Series();
		series.setName(attr + "单日工作曲线");
		int i = 0;
		// populating the series with data
		while (rs.next() && i < 1000) {
			series.getData().add(new XYChart.Data<Integer, Double>(i, rs.getDouble(index)));
			i++;
		}

		lineChart.getData().add(series);
		Tab newTab = new Tab(attr + "时间曲线");
		newTab.setContent(lineChart);
		centerTabPane.getTabs().add(newTab);
	}
	public void onGroupSelection() {
		String groupAttrName = groupAttributes.getValue();
		System.out.println("被选中的属性为：" + groupAttrName);
	}
	@SuppressWarnings("unchecked")
	public void onGroupAttributes() throws SQLException {
		rs.beforeFirst();//这一行代码很关键，不然连续点击就出现没有数据的情形。
		String groupAttrName = groupAttributes.getValue();
		System.out.println("选择的成组属性为：" + groupAttrName);
		ArrayList<String> groupAttrs = GroupAttributes.getHashMap().get(groupAttrName);
		System.out.println("该成组属性包括的SQL列为：" + groupAttrs);
		int count = groupAttrs.size();

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("时间序列编号");
		LineChart<Number,Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle(groupAttrName + "曲线对比");
		lineChart.setMaxSize(1000, 800);
		lineChart.setMinSize(1000, 800);
		for (int i = 0; i < count; i++) {
			Series<Number, Number> series = new Series<>();
			series.setName(groupAttrs.get(i) + "工作曲线");
			int index = Integer.parseInt(groupAttrs.get(i).substring(4));
			int j=0;
			// populating the series with data
			while (rs.next() && j < 1000) {
				series.getData().add(new XYChart.Data<Number, Number>(j, rs.getDouble(index)));
				j++;
			}
			lineChart.getData().add(series);
		}
		Stage stage = new Stage();
		Group root = new Group();
		Scene promptScene = new Scene(root,1000,800);
		promptScene.getStylesheets().add("/tbminer/TBMiner.css");		
		root.getChildren().add(lineChart);
		stage.setScene(promptScene);
		stage.show();
	}
}

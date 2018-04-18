package com.zhangqianli.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class TotalDatesQuery {
	public static void totalRecordsQuery() throws ReflectiveOperationException, Exception {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		Statement stmt = sqlconnect.createStatement();
		long startTime = System.currentTimeMillis();
		stmt.executeQuery("select attr3 from tbmrecordcleaned");
		Set<Date> dateSet = new TreeSet<>();// 使用treeset数据结构，将施工日期按照时间升序排列。
		ResultSet rs = stmt.getResultSet();
		int count = 0;
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		System.out.format("将tbmrecord表遍历一遍所用时间为： %d ms\n", usedTime);
		while (rs.next()) {
			dateSet.add(rs.getDate("attr3"));
			count++;
		}
		System.out.format("所包括的施工日期数目有： %d\n", dateSet.size());
		System.out.println("所包括的施工日期为：");
		System.out.println(dateSet);
		System.out.println("总的施工记录为：" +count);
	}
	/**
	 * 这个程序消耗太大，不应该经常运行。
	 * 更新一个本地的txt文件，该txt文件用来保存施工数据库中包含的所有施工日期，这个txt文件会被其它程序调用。
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	public static void updateDatesTxt() throws ReflectiveOperationException, Exception {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		Statement stmt = sqlconnect.createStatement();
		long startTime = System.currentTimeMillis();
		stmt.executeQuery("select attr3 from tbmrecordcleaned");
		Set<Date> dateSet = new TreeSet<>();// 使用treeset数据结构，将施工日期按照时间升序排列。
		ResultSet rs = stmt.getResultSet();
		int count = 0;
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		System.out.format("将tbmrecord表遍历一遍所用时间为： %d ms\n", usedTime);
		while (rs.next()) {
			dateSet.add(rs.getDate("attr3"));
			count++;
		}
		System.out.format("所包括的施工日期数目有： %d\n", dateSet.size());
		System.out.println("所包括的施工日期为：");
		System.out.println(dateSet);
		System.out.println("总的施工记录为：" +count);
		String datesBufferTxtName = "config/DatesBuffer.txt";
		File file = new File(datesBufferTxtName);
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		Iterator<Date> it = dateSet.iterator();
		Date temp;
		while (it.hasNext()) {
			temp = it.next();
			printWriter.println(temp);
		}
		printWriter.close();
		fileWriter.close();
		rs.close();
		stmt.close();
		sqlconnect.close();

	}
	/**
	 * 这个程序在未来版本会删除，不建议使用
	 * @return
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	@Deprecated
	public static TreeSet<Date> fetchAllDates() throws ReflectiveOperationException, Exception{
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		Statement stmt = sqlconnect.createStatement();
		long startTime = System.currentTimeMillis();
		stmt.executeQuery("select attr3 from tbmrecordcleaned");
		TreeSet<Date> dateSet = new TreeSet<>();// 使用treeset数据结构，将施工日期按照时间升序排列。
		ResultSet rs = stmt.getResultSet();
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		System.out.format("将tbmrecord表遍历一遍所用时间为： %d ms\n", usedTime);
		while (rs.next()) {
			dateSet.add(rs.getDate("attr3"));
		}
		System.out.format("所包括的施工日期数目有： %d\n", dateSet.size());
		System.out.println("所包括的施工日期为：");
		System.out.println(dateSet);
		rs.close();
		stmt.close();
		sqlconnect.close();
		return dateSet;
	}
	/**
	 * 该算法从一个缓存到硬盘的txt文件中读取所有施工日期
	 * @return 一个包括所有施工日期的TreeSet,施工日期以string格式表示。
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static TreeSet<String> fetchAllDatesFromTxt() 
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File("config/DatesBuffer.txt");
		String encoding = "UTF8";
		TreeSet<String> dateSet = new TreeSet<>();
		String temp;
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), encoding))) {
			while((temp = bufferedReader.readLine()) != null) {
//				System.out.println(temp);
				dateSet.add(temp);
			}
		}
		return dateSet;
	}
	/**
	 * 通常用于javafx程序设计中。生成施工记录属性的组合框选择项。
	 * @throws IOException
	 */
	public static void generateComboxItems1() throws IOException {
		String outputPath = "E:\\Eclipse\\HelloJavaFx1\\src\\application\\ComboBoxItems1.txt";
		File outputFile = new File(outputPath);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		String temp;
		int i = 0;
		while(i<200)
		{		
			temp = "<String fx:value=\"" + "attr" +(i+1) + "\" />";
			printWriter.println(temp);
			i++;
		}
		printWriter.close();
		fileWriter.close();
	}
	/**
	 * 根据DatesBuffer.txt文件生成组合下拉框中的选项，通常用于javafx程序设计中。施工组合框的选项
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void generateComboxItems() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String inputPath = "E:\\Eclipse\\HelloJavaFx1\\src\\application\\DatesBuffer.txt";
		String outputPath = "E:\\Eclipse\\HelloJavaFx1\\src\\application\\ComboBoxItems.txt";
		File inputFile = new File(inputPath);
		File outputFile = new File(outputPath);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		String encoding = "UTF8";// 读取txt文档时采用的字符集。
		String date;
		String temp;
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(inputFile), encoding))) {
			while ((date = bufferedReader.readLine()) != null) {
				//<String fx:value="2015-07-07" />
				temp = "<String fx:value=\"" + date + "\" />";
				printWriter.println(temp);
			}
		}
		printWriter.close();
		fileWriter.close();
	}

	public static void main(String[] args) throws Exception, Throwable {
		// String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		// String user = "root";
		// String password = "tian123kong";
		// Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		// Statement stmt = sqlconnect.createStatement();
		// long startTime = System.currentTimeMillis();
		// stmt.executeQuery(
		// "select attr3 from tbmrecordcleaned");
		// //stmt.executeQuery(
		// // "select attr3 from tbmrecord limit 10000001 ,20000000");
		// Set<java.sql.Date> dateSet = new TreeSet<>();//使用treeset数据结构，将施工日期按照时间升序排列。
		// ResultSet rs = stmt.getResultSet();
		// long endTime = System.currentTimeMillis();
		// long usedTime = endTime - startTime;
		// System.out.format("将tbmrecord表遍历一遍所用时间为： %d ms\n", usedTime);
		// while(rs.next())
		// {
		// dateSet.add(rs.getDate("attr3"));
		// }
		// System.out.format("所包括的施工日期数目有： %d\n",dateSet.size());
		// System.out.println("所包括的施工日期为：");
		// System.out.println(dateSet);
		//
		// rs.close();
		// stmt.close();
		// sqlconnect.close();
		TotalDatesQuery.updateDatesTxt();
//		TotalDatesQuery.generateComboxItems();
//		TotalDatesQuery.generateComboxItems1();
		//totalRecordsQuery();
	}
}

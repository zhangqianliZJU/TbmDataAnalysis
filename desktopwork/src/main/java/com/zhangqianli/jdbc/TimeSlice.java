package com.zhangqianli.jdbc;


import com.zhangqianli.dbcp.DBCPUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
//该程序用来获取单日所有的施工间隔

public class TimeSlice {

	/**
	 * 这个方法返回的数据规模太大，容易导致内存泄漏
	 * 
	 * @param date
	 * @return
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<ArrayList<Time>> fetchTimeSlices(String date) throws ReflectiveOperationException, Exception {
		Connection sqlconnect = DBCPUtil.getConnection();
		// PreparedStatement ps = sqlconnect.prepareStatement("select * from
		// tbmrecordcleaned where date(attr3) = ?");
		PreparedStatement ps = sqlconnect
				.prepareStatement("select attr3 from tbmrecordcleaned where attr3 >= ? and attr3 <= ?");// 这个语句有问题
		String start = date + " 00:00:00";
		String end = date + " 23:59:59";
		ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
		ps.setTimestamp(2, Timestamp.valueOf(end));
		ResultSet rs = ps.executeQuery();
		rs.last();// 移动到返回结果的最后一行
		int rowCount = rs.getRow();// 获取行数
		rs.beforeFirst();// 重新回到结果的第一行,这一行代码挺重要

		System.out.format("返回结果总共有 %d 行 \n", rowCount);
		List<ArrayList<Time>> timeSlices = new ArrayList<>();// 这就是要返回的数据结构

		List<Time> time = new ArrayList<>();// 存储所有施工日期。
		while (rs.next()) {
			time.add(rs.getTime("attr3"));
		}
		// for (int i = 0; i < 100; i++) {
		// System.out.println(time.get(i) + ": " +
		// SQLDateTimeTransform.SecondOfDay(time.get(i)));
		// }
		ArrayList<Time> element = new ArrayList<>();// 存储单个施工片段的时间
		int i = 0;
		Time t1 = time.get(0);
		Time t2 = time.get(1);
		while (i < time.size() - 1) {
			t1 = time.get(i);
			t2 = time.get(i + 1);
			int diff = SQLDateTimeTransform.SecondOfDay(t2) - SQLDateTimeTransform.SecondOfDay(t1);
			if (diff >= 1 && diff <= 300)// 设置时间价格筛选的颗粒度。
			{
				element.add(t1);
			} else {
				element.add(t1);
				timeSlices.add((ArrayList<Time>) element.clone());
				element.clear();
			}
			i++;// time的索引加1
		}
		element.add(t2);
		timeSlices.add(element);
		rs.close();
		ps.close();
		sqlconnect.close();
		return timeSlices;
	}

	/**
	 * 这个程序只存储时间片段的起始和技术片段，节省空间 这个获取时间片的程序，最好缓存到硬盘上，每次查询数据库的开销太大了，创建数组的开销也挺大。
	 * 
	 * @param date
	 * @return
	 * @throws ReflectiveOperationException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<ArrayList<String>> fetchTimeSlices1(String date) throws ReflectiveOperationException, Exception {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm?useSSL=true";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		// PreparedStatement ps = sqlconnect.prepareStatement("select * from
		// tbmrecordcleaned where date(attr3) = ?");
		PreparedStatement ps = sqlconnect
				.prepareStatement("select attr3 from tbmrecordcleaned where attr3 >= ? and attr3 <= ?");// 这个语句有问题
		String start = date + " 00:00:00";
		String end = date + " 23:59:59";
		ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
		ps.setTimestamp(2, Timestamp.valueOf(end));
		ResultSet rs = ps.executeQuery();
		rs.last();// 移动到返回结果的最后一行
		int rowCount = rs.getRow();// 获取行数
		rs.beforeFirst();// 重新回到结果的第一行,这一行代码挺重要
		System.out.format("返回结果总共有 %d 行 \n", rowCount);
		List<ArrayList<String>> timeSlices = new ArrayList<ArrayList<String>>();// 这就是要返回的数据结构
		List<String> time = new ArrayList<>();// 存储所有施工日期。
		while (rs.next()) {
			time.add(rs.getTime("attr3").toString());
		}
		while (!rs.isClosed()) {
			rs.close();
		}
		// ArrayList<Time> element = new ArrayList<>();// 存储单个施工片段的时间
		ArrayList<String> element = new ArrayList<>(2);// 存储单个施工片段的时间
		element.add(null);
		element.add(null);
		// System.out.println(element.size());
		String t1 = time.get(0);
		String t2 = time.get(1);
		String t3 = t1;
		String t4 = t2;
		int i = 0;
		int j = 0;
		for (i = 0; i < time.size() - 1; i++) {
			j = i + 1;
			element.set(0, t1);
			t3 = time.get(i);
			t4 = time.get(j);
			int diff = SQLDateTimeTransform.SecondOfDay(Time.valueOf(t4))
					- SQLDateTimeTransform.SecondOfDay(Time.valueOf(t3));
			if (diff > 300) {
				element.set(1, t3);
				timeSlices.add((ArrayList<String>) element.clone());
				element.clear();
				element.add(null);
				element.add(null);
				t1 = t4;
				t2 = time.get(j + 1);
			}
		}
		element.set(1, t4);
		timeSlices.add(element);
		time = null;
		ps.close();
		sqlconnect.close();
		return timeSlices;
	}

	public static void putTimeSlicesToTxt() throws IOException {

		String outputPath = "config/TimeSlicesBuffer.txt";
		File outputFile = new File(outputPath);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		try {
			TreeSet<String> totalDates = TotalDatesQuery.fetchAllDatesFromTxt();
			Iterator<String> it = totalDates.iterator();
			while (it.hasNext()) {
				String temp = it.next();// 容易出现问题
				System.out.println("当前日期为：" + temp);
				List<ArrayList<String>> temp1 = TimeSlice.fetchTimeSlices1(temp);
				for (int j = 0; j < temp1.size(); j++) {
					ArrayList<String> temp2 = temp1.get(j);
					String start = temp + " " + temp2.get(0);//开始时间
					printWriter.print(start);
					printWriter.print(",");
					String end = temp + " " + temp2.get(1);//结束时间
					printWriter.println(end);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			printWriter.close();
			fileWriter.close();
		}
	}
	/**
	 * 这个算法主要用在birch算法中
	 * @return
	 * @throws Throwable
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static ArrayList<String> fetchTimeSlicesFromTxt() throws Throwable, FileNotFoundException, IOException {
		ArrayList<String> result = new ArrayList<>();
		File file = new File("E:\\IntelliIdea\\TbmDataAnalysis\\desktopwork\\src\\main\\resources\\config\\TimeSlicesBuffer.txt");
		String encoding = "UTF8";
		String temp;
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), encoding))) {
			while((temp = bufferedReader.readLine()) != null) {
				result.add(temp);
			}
		}
		return result;
	}

	public static void main(String[] args) throws Throwable {
		putTimeSlicesToTxt();
	}
}

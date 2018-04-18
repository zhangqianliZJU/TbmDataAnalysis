package com.zhangqianli.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class DateTimeTest {
	public static void main(String[] args) throws Exception, Throwable {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		Statement stmt = sqlconnect.createStatement();
		// String querry = "select attr3,attr5,attr6 from tbmrecord where attr3
		// >= '2015-07-07 00:00:00' and attr3 <= '2015-07-08 00:00:00'and attr5
		// != 0 and attr6 != 0"
		// +"order by attr3";//从2015-07-07的施工数据中选择TBM运转时的施工数据，包括运行时间，刀盘转速和刀盘扭矩。
		//stmt.executeQuery(
		//		"select attr3,attr5,attr6 from tbmrecordcleaned where attr3 >= '2015-07-07 00:00:00' and attr3 <= '2015-07-08 00:00:00'");
		stmt.executeQuery(
				"select attr3,attr5,attr6 from tbmrecordcleaned limit 3000");
		ResultSet rs = stmt.getResultSet();
		
		List<Timestamp> dateTime = new ArrayList<>();
		List<Double> rspeed = new ArrayList<>();
		List<Double> torque = new ArrayList<>();
		
		Timestamp[] dateTime_java;
		double[] rspeed_double;
		double[] torque_double;
		while (rs.next()) {
			dateTime.add(rs.getTimestamp(1));
			rspeed.add(rs.getDouble(2));
			torque.add(rs.getDouble(3));
		}
		dateTime_java = new Timestamp[dateTime.size()];
		rspeed_double = new double[rspeed.size()];
		torque_double = new double[torque.size()];
		int[] time = new int[torque.size()];
		for (int i = 0; i < rspeed.size(); i++) {
			dateTime_java[i] = dateTime.get(i);
			rspeed_double[i] = rspeed.get(i);
			torque_double[i] = torque.get(i);
			time[i] = i+1;
		}	
		for (int i = 0; i < time.length; i++) {
			System.out.println(dateTime_java[i].toString() + ","+rspeed_double[i]+","+torque_double[i]);
		}
	}
}

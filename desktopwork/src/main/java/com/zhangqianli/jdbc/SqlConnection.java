package com.zhangqianli.jdbc;
import java.sql.Connection;
import java.sql.DriverManager;
//这个程序主要返回一个Mysql数据库的连接。
public class SqlConnection {
	public static Connection getConnection(String sqlURL, String user, String password) 
			throws ReflectiveOperationException, Exception {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connect = DriverManager.getConnection(sqlURL, user, password);
			//System.out.println("正在获取数据库连接");
			return connect;	
		}
}

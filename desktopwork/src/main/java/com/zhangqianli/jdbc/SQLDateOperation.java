package com.zhangqianli.jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
/**
 * @author zhangqianli
 * 主要用来连接数据库，并根据时间区间来查询施工记录，
 * 比如查询2015-07-07 00:00:00~2015-07-08 00:00:00之间的数据，数据库为未清洗过的tbmrecord。
 *
 */
public class SQLDateOperation {
	public static void main(String[] args) throws Exception, Throwable {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		Statement stmt = sqlconnect.createStatement();
		//这条语句查询指定时间间隔内的日期
		stmt.executeQuery(
			"select attr3,attr5,attr6 from tbmrecord where attr3 >= '2015-07-07 00:00:00' and attr3 <= '2015-07-08 00:00:00' and attr5 != 0 and attr6 != 0 order by attr3;");
		//下面这条指令查询一共有多少天的施工记录。
		ResultSet rs = stmt.getResultSet();
		int i = 0;
		while(rs.next())
		{
			//用ResultSet.getDate()方法可以获取MySQL中的DateTime对应的时间。
			System.out.println(rs.getDate("attr3"));
			i++;
		}
		System.out.format("查询到的施工记录为： %d\n",i);		
		rs.close();
		stmt.close();
		sqlconnect.close();
	}
}

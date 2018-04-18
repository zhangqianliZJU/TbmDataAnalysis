package com.zhangqianli.jdbc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 * @author zhangqianli
 * 本程序用来从mysql中逐行读取数据,当前的施工记录已经达到10，878，656行。
 * 从数据库逐行读取的数据，可以存储到容量为n的队列数据结构中。该队列的内容是动态变化的，可以对该队列进行采样或者建立直方图。该队列就是一个window
 *
 */
public class ReadSingleRecordFromMysql {
/**
 * @param args
 * @throws Exception 
 * @throws ReflectiveOperationException 
 */
public static void main(String[] args) throws ReflectiveOperationException, Exception {
	String sqlURL = "jdbc:mysql://localhost:3306/tbm?useServerPrepStmts=true";
	String user = "root";
	String password = "tian123kong";
	Connection conn = SqlConnection.getConnection(sqlURL, user, password);
	PreparedStatement psts = 
			conn.prepareStatement("select attr3,attr5,attr6,attr7,attr8 from tbmrecordcleaned",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
	psts.setFetchSize(Integer.MIN_VALUE);//这个设置很关键。
	ResultSet result = psts.executeQuery();
	int i = 0;
	while (result.next() && i<100) {
		System.out.println(++i + ":" + result.getDouble(2) + "," + result.getDouble(3)+ "," + result.getDouble(4)+ "," + result.getDouble(5));
	}
	conn.close();
}
}

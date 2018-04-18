package com.zhangqianli.jdbc;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * @author zhangqianli
 *@version 1.0
 *这个程序目前只是实现了如下基本功能：1 从 MySQL数据库中读取数据，然后采用R语言绘图。
 *************3 tbmrecord数据库中的主键，Mysql中的DateTime类型，如何提取到java中，并如何最终传递到R语言中，
 ************4 Double和double之间的自动装箱和拆箱，如何尽量避免这种显式的转换。
 */
public class SQL_Java_R_Connection {
	public static void main(String[] args) throws Exception, Throwable {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		RConnection rconnect = new RConnection("127.0.0.1");//先在R语言中加载Rserve包，然后运行Rserve()启动服务。
		Statement stmt = sqlconnect.createStatement();
		// String querry = "select attr3,attr5,attr6 from tbmrecord where attr3
		// >= '2015-07-07 00:00:00' and attr3 <= '2015-07-08 00:00:00'and attr5
		// != 0 and attr6 != 0"
		// +"order by attr3";//从2015-07-07的施工数据中选择TBM运转时的施工数据，包括运行时间，刀盘转速和刀盘扭矩。
		//stmt.executeQuery(
		//		"select attr3,attr5,attr6 from tbmrecordcleaned where attr3 >= '2015-07-07 00:00:00' and attr3 <= '2015-07-08 00:00:00'");
		stmt.executeQuery(
				"select attr3,attr5,attr6 from tbmrecordcleaned limit 0,2000");
		ResultSet rs = stmt.getResultSet();
		
		List<Double> rspeed = new ArrayList<>();
		List<Double> torque = new ArrayList<>();
		double[] rspeed_double;
		double[] torque_double;
		while (rs.next()) {
			rspeed.add(rs.getDouble(2));
			torque.add(rs.getDouble(3));
		}
		rspeed_double = new double[rspeed.size()];
		torque_double = new double[torque.size()];
		int[] time = new int[torque.size()];
		for (int i = 0; i < rspeed.size(); i++) {
			rspeed_double[i] = rspeed.get(i);
			torque_double[i] = torque.get(i);
			time[i] = i+1;
		}
		try {
			// rconnect.assign("ts", ts);//赋值语句，实现java和R语言之间的数据传递
			rconnect.assign("rspeed", rspeed_double);
			rconnect.assign("torque", torque_double);
			rconnect.assign("time", time);
			rconnect.eval("jpeg('d://test-1.jpg')");
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(rspeed,torque,type=\"b\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			// re.eval("plot(dose,drugB,type=\"b\",pch=23,lty=6,col=\"green\",bg=\"green\")");
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");//这部分代码主要绘制散点图
			
			rconnect.eval("jpeg('d://test-2.jpg')");
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(torque)");// 注意转义符的使用
			// re.eval("plot(dose,drugB,type=\"b\",pch=23,lty=6,col=\"green\",bg=\"green\")");
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");//这部分代码主要绘制条形图
			
			rconnect.eval("jpeg('d://test-3.jpg')");//绘制时间序列图,torque-time图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,torque,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			// re.eval("plot(dose,drugB,type=\"b\",pch=23,lty=6,col=\"green\",bg=\"green\")");
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			
			rconnect.eval("jpeg('d://test-4.jpg')");//绘制时间序列图，rspeed-time图像
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,rspeed,type=\"b\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			// re.eval("plot(dose,drugB,type=\"b\",pch=23,lty=6,col=\"green\",bg=\"green\")");
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			
			System.out.println("Done!");
			
		} catch (REngineException e) {
			e.printStackTrace();
		} finally {
			rs.close();
			rconnect.close();// 最后要记得关闭R语言的连接释放资源。
			sqlconnect.close();// 一定记得关闭SQL数据连接。
		}
	}
}

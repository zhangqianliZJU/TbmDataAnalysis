package com.zhangqianli.jdbc;
import java.sql.Time;
/**
 * @author zhangqianli
 *日期转换的相关函数
 */
//输入时间，返回该时间在一天中的对应秒数，比如输入20：42：26，则返回74546.
public class SQLDateTimeTransform {
	public static int SecondOfDay(Time time) {
		@SuppressWarnings("deprecation")
		int hour = time.getHours();
		@SuppressWarnings("deprecation")
		int minute = time.getMinutes();
		@SuppressWarnings("deprecation")
		int second = time.getSeconds();
		//System.out.println(hour+":"+minute+":"+second);
		return 3600*hour + minute * 60 + second;
	}
	public static void main(String[] args)
	{
		@SuppressWarnings("deprecation")
		int count = SecondOfDay(new Time(20,42,26));
		System.out.println(count);
	}
}

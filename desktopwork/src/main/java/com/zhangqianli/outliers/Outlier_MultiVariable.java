
package com.zhangqianli.outliers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zhangqianli.jdbc.SqlConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * @author zhangqianli
 * 用马氏距离方法联合多个特征进行异常点检测。
 *
 */
public class Outlier_MultiVariable {

	public static void main(String[] args) throws Exception, Throwable {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm?useSSL=true";
		String user = "root";
		String password = "tian123kong";
		Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
		RConnection rconnect = new RConnection("127.0.0.1");
		Statement stmt = sqlconnect.createStatement();
		//stmt.executeQuery(
		//		"select attr3,attr5,attr6,attr198,attr129,attr8,attr7 from tbmrecordcleaned where (attr3 >= '2015-07-10 05:53:56' and attr3 <= '2015-07-10 06:38:59')");
		stmt.executeQuery(
				"select attr3,attr5,attr6,attr198,attr129,attr8,attr7 from tbmrecordcleaned where (attr3 >= '2016-07-03 00:46:56' and attr3 <= '2016-07-03 01:26:08')");
		ResultSet rs = stmt.getResultSet();
		List<Double> rspeed = new ArrayList<>();
		List<Double> torque = new ArrayList<>();
		List<Double> mileage = new ArrayList<>();// 掘进里程
		List<Double> penetration_rate = new ArrayList<>();
		List<Double> advance_rate = new ArrayList<>();
		List<Double> thrust_force = new ArrayList<>();
		List<Double> TPI = new ArrayList<>();
		List<Double> FPI = new ArrayList<>();

		// attr3是时间
		// 这里明确一下需要绘制的图像：
		// 转速，扭矩，推进力、推进速度和贯入度的时间序列图像和频数分布直方图，包括单变量异常点检测前后的，单个是5X2=10
		// 一些散点图，比如TPI-FPI图像，按需求绘制。
		double[] rspeed_double;// 刀盘转速,attr5
		double[] torque_double;// 刀盘扭矩，attr6
		double[] mileage_double;// 掘进里程，attr198
		double[] penetration_rate_double;// 贯入度 ，attr129
		double[] advance_rate_double;// 推进速度，attr8
		double[] thrust_force_double;// 总推进力，attr7
		double[] TPI_double;// 导出参数TPI
		double[] FPI_double;// 导出参数FPI

		while (rs.next()) {
			rspeed.add(rs.getDouble(2));
			torque.add(rs.getDouble(3));
			mileage.add(rs.getDouble(4));
			penetration_rate.add(rs.getDouble(5));
			advance_rate.add(rs.getDouble(6));
			thrust_force.add(rs.getDouble(7));
			TPI.add(rs.getDouble(3) / rs.getDouble(5));
			FPI.add(rs.getDouble(7) / rs.getDouble(5));
		} // 尚未进行异常值检测的数据集，链表形式。

		rspeed_double = new double[rspeed.size()];
		torque_double = new double[torque.size()];
		mileage_double = new double[mileage.size()];
		penetration_rate_double = new double[penetration_rate.size()];
		advance_rate_double = new double[advance_rate.size()];
		thrust_force_double = new double[thrust_force.size()];
		TPI_double = new double[rspeed.size()];
		FPI_double = new double[rspeed.size()];

		double[] time = new double[torque.size()];
		for (int i = 0; i < rspeed.size(); i++) {
			rspeed_double[i] = rspeed.get(i);
			torque_double[i] = torque.get(i);
			mileage_double[i] = mileage.get(i);
			penetration_rate_double[i] = penetration_rate.get(i);
			advance_rate_double[i] = advance_rate.get(i);
			thrust_force_double[i] = thrust_force.get(i);
			TPI_double[i] = TPI.get(i);
			FPI_double[i] = FPI.get(i);
			time[i] = i + 1;
		} // 这是尚未进行异常值检测的数据集，数组形式，
			// System.out.println("size1 : "+TPI.size());
			// System.out.println("size2: "+FPI.size());
			// System.out.println("size3: "+advance_rate.size());

		rconnect.assign("advance_rate", advance_rate_double);
		rconnect.assign("TPI", TPI_double);
		rconnect.assign("FPI", FPI_double);
		rconnect.assign("time", time);
		REXP rexp = rconnect.eval("advance_rate_copy <- advance_rate - mean(advance_rate)");
		double[] advance_rate_copy = rexp.asDoubles();
		rexp = rconnect.eval("TPI_copy <- TPI - mean(TPI)");
		double[] TPI_copy = rexp.asDoubles();
		rexp = rconnect.eval("FPI_copy <- FPI - mean(FPI)");
		double[] FPI_copy = rexp.asDoubles();
		double[] mahadistance = new double[advance_rate_double.length];
		rconnect.eval("tbm_record_matrix <- matrix(c(advance_rate,TPI,FPI),ncol=3,byrow = FALSE)");
		rconnect.eval("tbm_record_cov <- cov(tbm_record_matrix)");

		double[] vector = new double[3];
		for (int i = 0; i < advance_rate.size(); i++) {
			vector[0] = advance_rate_copy[i];
			vector[1] = TPI_copy[i];
			vector[2] = FPI_copy[i];
			rconnect.assign("vector", vector);
			rconnect.eval("tbm_record_matrix_copy <- matrix(c(vector),ncol=3,byrow = FALSE)");
			rexp = rconnect.eval(
					"maha_distance <- tbm_record_matrix_copy%*%solve(tbm_record_cov)%*%t(tbm_record_matrix_copy)");
			//mahadistance[i] = rexp.asDouble();
			//不同的教材给的定义不一样，有的是要开根号，有的不需要开根号，但是主流是开根号，因为开根号后复合距离的量纲。
			mahadistance[i] = Math.sqrt(rexp.asDouble());

		}
		rconnect.assign("maha_distance", mahadistance);
		rconnect.eval("jpeg('E://R//Plot//AR_TPI_FPI14.jpg')");// 三维散点图
		rconnect.eval("opar <- par(no.readonly=TRUE)");
		rconnect.eval("par(pin=c(5,5))");
		rconnect.eval("par(cex.axis=0.75, font.axis=3)");
		rconnect.eval("library(scatterplot3d)");
		rconnect.eval("scatterplot3d(FPI,TPI,advance_rate,color = \"red\")");
		rconnect.eval("dev.off()");

		/*for (int i = 0; i < mahadistance.length; i++)
			System.out.println(mahadistance[i]);*/

		rconnect.eval("jpeg('E://R//Plot//maha_distance-hist.jpg')");// 三维散点图
		rconnect.eval("opar <- par(no.readonly=TRUE)");
		rconnect.eval("par(pin=c(5,5))");
		rconnect.eval("par(cex.axis=0.75, font.axis=3)");
		rconnect.eval("hist(maha_distance)");
		rconnect.eval("dev.off()");

		rconnect.eval("jpeg('E://R//Plot//maha_distance_time.jpg')");// 三维散点图
		rconnect.eval("opar <- par(no.readonly=TRUE)");
		rconnect.eval("par(pin=c(5,5))");
		rconnect.eval("par(cex.axis=0.75, font.axis=3)");
		rconnect.eval("plot(time,maha_distance,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
		rconnect.eval("dev.off()");

		System.out.println("已完成马氏距离计算！");
		List<Double> rspeed_normal = new ArrayList<>();
		List<Double> torque_normal = new ArrayList<>();
		List<Double> mileage_normal = new ArrayList<>();// 掘进里程
		List<Double> penetration_rate_normal = new ArrayList<>();
		List<Double> advance_rate_normal = new ArrayList<>();
		List<Double> thrust_force_normal = new ArrayList<>();
		List<Double> TPI_normal = new ArrayList<>();
		List<Double> FPI_normal = new ArrayList<>();
		List<Double> mahadistance_normal = new ArrayList<>();
		
		List<Double> rspeed_outlier = new ArrayList<>();
		List<Double> torque_outlier = new ArrayList<>();
		List<Double> mileage_outlier = new ArrayList<>();// 掘进里程
		List<Double> penetration_rate_outlier = new ArrayList<>();
		List<Double> advance_rate_outlier = new ArrayList<>();
		List<Double> thrust_force_outlier = new ArrayList<>();
		List<Double> TPI_outlier = new ArrayList<>();
		List<Double> FPI_outlier = new ArrayList<>();
		List<Double> mahadistance_outlier = new ArrayList<>();

		for (int i = 0; i < rspeed.size(); i++) {
			if (mahadistance[i] < 2) {
				rspeed_normal.add(rspeed.get(i)); 
				torque_normal.add(torque.get(i)); 
				mileage_normal.add(mileage.get(i)); 
				penetration_rate_normal.add(penetration_rate.get(i));																									
				advance_rate_normal.add(advance_rate.get(i)); 															
				thrust_force_normal.add(thrust_force.get(i)); 
				FPI_normal.add(FPI.get(i));
				TPI_normal.add(TPI.get(i));
				mahadistance_normal.add(mahadistance[i]);
			} else {
				rspeed_outlier.add(rspeed.get(i)); 													
				torque_outlier.add(torque.get(i)); 
				mileage_outlier.add(mileage.get(i)); 
				penetration_rate_outlier.add(penetration_rate.get(i));
				advance_rate_outlier.add(advance_rate.get(i)); 
				thrust_force_outlier.add(thrust_force.get(i));
				TPI_outlier.add(TPI.get(i));
				FPI_outlier.add(FPI.get(i));
				mahadistance_outlier.add(mahadistance[i]);
			}
		} // 进行异常值检测的数据集，链表形式，
		int normal_size = rspeed_normal.size();
		int outlier_size = rspeed_outlier.size();
		System.out.println("正常数据集中施工数据总数为： " + normal_size);
		System.out.println("数据集中异常施工数据总数为： " + outlier_size);

		double[] rspeed_double_normal = new double[normal_size];// 刀盘转速,attr5
		double[] torque_double_normal = new double[normal_size];// 刀盘扭矩，attr6
		double[] mileage_double_normal = new double[normal_size];// 掘进里程，attr198
		double[] penetration_rate_double_normal = new double[normal_size];// 贯入度,attr129
		double[] advance_rate_double_normal = new double[normal_size];// 推进速度，attr8
		double[] thrust_force_double_normal = new double[normal_size];// 总推进力，attr7
		double[] TPI_double_normal = new double[normal_size];
		double[] FPI_double_normal = new double[normal_size];
		double[] time_normal = new double[normal_size];
		double[] mahadistance_double_normal = new double[normal_size];
		
		double[] rspeed_double_outlier = new double[outlier_size];// 刀盘转速,attr5
		double[] torque_double_outlier = new double[outlier_size];// 刀盘扭矩，attr6
		double[] mileage_double_outlier = new double[outlier_size];// 掘进里程，attr198
		double[] penetration_rate_double_outlier = new double[outlier_size];// 贯入度
		double[] advance_rate_double_outlier = new double[outlier_size];// 推进速度，attr8
		double[] thrust_force_double_outlier = new double[outlier_size];// 总推进力，attr7
		double[] time_outlier = new double[outlier_size];
		double[] TPI_double_outlier = new double[outlier_size];
		double[] FPI_double_outlier = new double[outlier_size];
		double[] mahadistance_double_outlier = new double[outlier_size];
		
		for (int i = 0; i < rspeed_normal.size(); i++) {
			rspeed_double_normal[i] = rspeed_normal.get(i);
			torque_double_normal[i] = torque_normal.get(i);
			mileage_double_normal[i] = mileage_normal.get(i);
			penetration_rate_double_normal[i] = penetration_rate_normal.get(i);
			advance_rate_double_normal[i] = advance_rate_normal.get(i);
			thrust_force_double_normal[i] = thrust_force_normal.get(i);
			TPI_double_normal[i] = TPI_normal.get(i);
			FPI_double_normal[i] = FPI_normal.get(i);
			time_normal[i] = i + 1;
			mahadistance_double_normal[i] = mahadistance_normal.get(i);
			
		} // 完成异常值检测的数据集，数组形式，正常值。

		for (int i = 0; i < rspeed_outlier.size(); i++) {
			rspeed_double_outlier[i] = rspeed_outlier.get(i);
			torque_double_outlier[i] = torque_outlier.get(i);
			mileage_double_outlier[i] = mileage_outlier.get(i);
			penetration_rate_double_outlier[i] = penetration_rate_outlier.get(i);
			advance_rate_double_outlier[i] = advance_rate_outlier.get(i);
			thrust_force_double_outlier[i] = thrust_force_outlier.get(i);
			TPI_double_outlier[i] = TPI_outlier.get(i);
			FPI_double_outlier[i] = FPI_outlier.get(i);
			mahadistance_double_outlier[i] = mahadistance_outlier.get(i);
			time_outlier[i] = i + 1;
		} // 完成异常值检测的数据集，数组形式，异常值。

		try {
			// rconnect.assign("ts", ts);//赋值语句，实现java和R语言之间的数据传递
			//rconnect = new RConnection();
			rconnect.assign("rspeed", rspeed_double);
			rconnect.assign("torque", torque_double);
			rconnect.assign("time", time);
			rconnect.assign("mileage", mileage_double);
			rconnect.assign("penetration_rate", penetration_rate_double);
			rconnect.assign("advance_rate", advance_rate_double);
			rconnect.assign("thrust_force", thrust_force_double);
			rconnect.assign("TPI", TPI_double);
			rconnect.assign("FPI", FPI_double);

			rconnect.eval("end_mileage <- max(mileage)");// 利用eval函数将R语言中的变量返回到Java。
			rconnect.eval("start_mileage <- min(mileage)");
			rexp = rconnect.eval("total_mileage <- end_mileage-start_mileage");

			//double total_mileage = rexp.asDouble();
			//System.out.println("2017年2月16日的总掘进里程为：" + total_mileage + " m");
			
			rconnect.eval("jpeg('E://R//Plot//rspeed-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,rspeed,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//rspeed-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(rspeed)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//torque-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,torque,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//torque-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(torque)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//penetration-rate-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,penetration_rate,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//penetration-rate-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(penetration_rate)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//advance-rate-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,advance_rate,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//advance-rate-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(advance_rate)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//thrust-force-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,thrust_force,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//thrust-force-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(thrust_force)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//TPI-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,TPI,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//TPI-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(TPI)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//FPI-time.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time,FPI,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//FPI-hist.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(FPI)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			
			rconnect.eval("jpeg('E://R//Plot//FPI-TPI.jpg')");// plot-12，贯入度-推进速度散点图，TPI-FPI图像
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(TPI,FPI,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			System.out.println("已完成总体数据集的图像绘制");

			rconnect.assign("rspeed_normal", rspeed_double_normal);
			rconnect.assign("torque_normal", torque_double_normal);
			rconnect.assign("time_normal", time_normal);
			rconnect.assign("mileage_normal", mileage_double_normal);
			rconnect.assign("penetration_rate_normal", penetration_rate_double_normal);
			rconnect.assign("advance_rate_normal", advance_rate_double_normal);
			rconnect.assign("thrust_force_normal", thrust_force_double_normal);
			rconnect.assign("TPI_normal", TPI_double_normal);
			rconnect.assign("FPI_normal", FPI_double_normal);
			rconnect.assign("mahadistance_normal", mahadistance_double_normal);

	
			rconnect.eval("jpeg('E://R//Plot//normal//FPI-TPI-normal.jpg')");// plot-12，贯入度-推进速度散点图，TPI-FPI图像
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(TPI_normal,FPI_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			
			rconnect.eval("jpeg('E://R//Plot//normal//rspeed-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,rspeed_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//rspeed-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(rspeed_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//torque-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,torque_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//torque-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(torque_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//penetration-rate-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,penetration_rate_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//penetration-rate-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(penetration_rate_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//advance-rate-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,advance_rate_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//advance-rate-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(advance_rate_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//thrust-force-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,thrust_force_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//thrust-force-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(thrust_force_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//TPI-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,TPI_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//TPI-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(TPI_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//FPI-time-normal.jpg')");// rspeed-time图像。，
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,FPI_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//FPI-hist-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(FPI_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");

			
			
			rconnect.eval("jpeg('E://R//Plot//normal//AR_TPI_FPI_normal.jpg')");// 三维散点图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("library(scatterplot3d)");
			rconnect.eval("scatterplot3d(FPI_normal,TPI_normal,advance_rate_normal,color = \"red\")");
			rconnect.eval("dev.off()");

		
			rconnect.eval("jpeg('E://R//Plot//normal//mahadistance-hist-normal.jpg')");// 三维散点图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("hist(mahadistance_normal)");
			rconnect.eval("dev.off()");

			rconnect.eval("jpeg('E://R//Plot//normal//mahadistance_time_normal.jpg')");// 三维散点图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("plot(time_normal,mahadistance_normal,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
			rconnect.eval("dev.off()");
			
			System.out.println("已完成正常数据集的图像绘制");

			rconnect.eval("jpeg('E://R//Plot//normal//TPI-boxplot-normal.jpg')");// 转速直方图
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("boxplot(TPI_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			
			// 绘制TPI和FPI的箱线图，Boxplot.
			rconnect.eval("jpeg('E://R//Plot//normal//FPI-boxplot-normal.jpg')");// FPI箱线图。
			rconnect.eval("opar <- par(no.readonly=TRUE)");
			rconnect.eval("par(pin=c(5,5))");
			rconnect.eval("par(cex.axis=0.75, font.axis=3)");
			rconnect.eval("boxplot(FPI_normal)");// 注意转义符的使用
			rconnect.eval("par(opar)");
			rconnect.eval("dev.off()");
			
			System.out.println("Size of TPI_double is : " + TPI_double.length);
			System.out.println("Size of TPI_double_normal is : " + TPI_double_normal.length);

			System.out.println("All is Done!");

		} finally {
			rs.close();
			rconnect.close();// 最后要记得关闭R语言的连接释放资源。
			sqlconnect.close();// 一定记得关闭SQL数据连接。
		}
	}
}

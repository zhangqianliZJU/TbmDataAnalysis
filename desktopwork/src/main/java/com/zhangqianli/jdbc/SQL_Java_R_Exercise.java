package com.zhangqianli.jdbc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

public class SQL_Java_R_Exercise {

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
        //stmt.executeQuery(
        //		"select attr3,attr5,attr6,attr198,attr129,attr8 from tbmrecordcleaned where (date(attr3) = '2015-07-08') limit 0,5000");
        stmt.executeQuery(
                "select attr3,attr5,attr6,attr198,attr129,attr8,attr7 from tbmrecordcleaned where (attr3 >= '2015-07-10 05:53:56' and attr3 <= '2015-07-10 06:38:59')");
        ResultSet rs = stmt.getResultSet();
        List<Double> rspeed = new ArrayList<>();
        List<Double> torque = new ArrayList<>();
        List<Double> mileage = new ArrayList<>();//掘进里程
        List<Double> penetration_rate = new ArrayList<>();
        List<Double> advance_rate = new ArrayList<>();
        List<Double> thrust_force = new ArrayList<>();
        new ArrayList<>();
        new ArrayList<>();

        double[] rspeed_double;//刀盘转速,attr5
        double[] torque_double;//刀盘扭矩，attr6
        double[] mileage_double;//掘进里程，attr198
        double[] penetration_rate_double;//贯入度 ，attr129
        double[] advance_rate_double;//推进速度，attr8
        double[] thrust_force_double;//总推进力，attr7

        while (rs.next()) {
            rspeed.add(rs.getDouble(2));
            torque.add(rs.getDouble(3));
            mileage.add(rs.getDouble(4));
            penetration_rate.add(rs.getDouble(5));
            advance_rate.add(rs.getDouble(6));
            thrust_force.add(rs.getDouble(7));
        }

        rspeed_double = new double[rspeed.size()];
        torque_double = new double[torque.size()];
        mileage_double = new double[mileage.size()];
        penetration_rate_double = new double[penetration_rate.size()];
        advance_rate_double = new double[advance_rate.size()];
        thrust_force_double = new double[thrust_force.size()];

        int[] time = new int[torque.size()];
        for (int i = 0; i < rspeed.size(); i++) {
            rspeed_double[i] = rspeed.get(i).doubleValue();
            torque_double[i] = torque.get(i).doubleValue();
            mileage_double[i] = mileage.get(i).doubleValue();
            penetration_rate_double[i] = penetration_rate.get(i).doubleValue();
            advance_rate_double[i] = advance_rate.get(i).doubleValue();
            thrust_force_double[i] = thrust_force.get(i).doubleValue();
            time[i] = i + 1;
        }
        try {
            // rconnect.assign("ts", ts);//赋值语句，实现java和R语言之间的数据传递
            rconnect.assign("rspeed", rspeed_double);
            rconnect.assign("torque", torque_double);
            rconnect.assign("time", time);
            rconnect.assign("mileage", mileage_double);
            rconnect.assign("penetration_rate", penetration_rate_double);
            rconnect.assign("advance_rate", advance_rate_double);
            rconnect.assign("thrust_force", thrust_force_double);

            rconnect.eval("end_mileage <- max(mileage)");//利用eval函数将R语言中的变量返回到Java。
            rconnect.eval("start_mileage <- min(mileage)");
            REXP rexp = rconnect.eval("total_mileage <- end_mileage-start_mileage");
           
            double total_mileage = rexp.asDouble();

            System.out.println("2017年2月16日的总掘进里程为：" + total_mileage + " m");
            /*REXP rexp = rconnect.eval("time1 <- time");//将R语言中的数据提取转换到Java中。			
			int[] time1 = rexp.asIntegers();//实现了java和R语言之间的数据传递。
			
			for (int i = 0; i < time1.length; i++) {
				System.out.println(time1[i]);
			}*/

            File.createTempFile("test-", ".jpg");
            rconnect.eval("jpeg('E://R//Plot//plot1.jpg')");//plot-1是扭矩-转速图像。E:\科研\TBM施工数据分析\sample\plot\plot1.jpg
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(rspeed,torque,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");//这部分代码主要绘制散点图
            
            rconnect.eval("jpeg('E://R//Plot//plot2.jpg')");//plot-2，扭矩直方图。
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("hist(torque)");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");//这部分代码主要绘制条形图
            
            rconnect.eval("jpeg('E://R//Plot//plot3.jpg')");//plot-3是torque-time图
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(time,torque,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");            
            
            rconnect.eval("jpeg('E://R//Plot//plot5.jpg')");//plot-5，mileage-time图
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(time,mileage,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot6.jpg')");//plot-6，penetration-time图
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(time,penetration_rate,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            // re.eval("plot(dose,drugB,type=\"b\",pch=23,lty=6,col=\"green\",bg=\"green\")");
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot7.jpg')");//plot-7，rspeed-time图
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(time,rspeed,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot8.jpg')");//plot-8,advance_rate~time图像。
            rconnect.eval("opar <- par(no.readonly=TRUE)");//就以它为例子进行滤波。
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(time,advance_rate,type=\"p\",pch=19,lty=2,col=\"red\")");
            //线性滤波器的效果貌似还不错。
            //rconnect.eval("k <- 10");
            //rconnect.eval("lines(filter(advance_rate, rep(1/k,k)),  col = \"blue\",  lwd = 3 )");

            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot9.jpg')");//plot-9，贯入度-推进速度散点图，可以看出两者高度线性相关，说明其采用的计算公式为：
            rconnect.eval("opar <- par(no.readonly=TRUE)");//penetration_rate = advance_rate/rspeed;
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("plot(advance_rate,penetration_rate,type=\"p\",pch=19,lty=2,col=\"red\")");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot10.jpg')");//进行异常点剔除后的推进速度-时间图像，
            rconnect.eval("opar <- par(no.readonly=TRUE)");//penetration_rate = advance_rate/rspeed;
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            //采用rm.outliers()对一个数据框进行异常值检测。
            rconnect.eval("raw <- data.frame(time,advance_rate)");
            rconnect.eval("library(outliers)");//效果一般？？？
            rconnect.eval("raw1 <- rm.outlier(raw,fill = FALSE,median = TRUE,opposite = FALSE)");
            rconnect.eval("plot(raw1$time,raw1$advance_rate,type=\"p\",pch=19,lty=2,col=\"red\")");
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");

            rconnect.eval("jpeg('E://R//Plot//plot11.jpg')");//plot-11，推进速度直方图，判断其是否满足正态分布。
            rconnect.eval("opar <- par(no.readonly=TRUE)");
            rconnect.eval("par(pin=c(5,5))");
            rconnect.eval("par(cex.axis=0.75, font.axis=3)");
            rconnect.eval("hist(advance_rate)");// 注意转义符的使用
            rconnect.eval("par(opar)");
            rconnect.eval("dev.off()");//这部分代码主要绘制条形图
            
            System.out.println("Done!");

        } catch (IOException e) {
        } catch (REngineException e) {
        } finally {
            rs.close();
            rconnect.close();// 最后要记得关闭R语言的连接释放资源。
            sqlconnect.close();// 一定记得关闭SQL数据连接。
        }
    }
}

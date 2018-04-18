//BIRCH.java  

package com.zhangqianli.birch;

import com.zhangqianli.dbcp.DBCPUtil;
import com.zhangqianli.jdbc.SqlConnection;
import com.zhangqianli.jdbc.TimeSlice;
import com.zhangqianli.jdbc.TotalDatesQuery;
import com.zhangqianli.outliers.OutlierDetect;
import com.zhangqianli.wavlet.Wavelet1;
import javafx.application.Platform;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.math3.stat.StatUtils;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author zhangqianli
 */
public class BIRCH {
    ArrayList<ArrayList<Double>> dataOfPlot;
    public static final int dimen = 4; // 这个应该是每个数据点所包含的属性的数量。
    LeafNode leafNodeHead = new LeafNode();
    int point_num = 0; // point instance计数

    // 逐条扫描数据库，建立B-树
    public BIRCH() {
        dataOfPlot = new ArrayList<>();
    }

    public TreeNode buildBTree(String filename) {
        // 先建立一个叶子节点
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;

        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        // 打开文件，从文件中读取原始数据
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Data File Not Exists.");
            System.exit(2);
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null && line.trim() != "") {
                point_num++;
                // System.out.println(point_num);
                // System.out.println(line);

                String[] cont = line.split("[,|\\s+]");// 正则表达式

                // String[] cont=line.split(" ");//正则表达式
                // String[] cont = line.split("
                // ");//这个正则表达式，和这个作者设想的txt格式数据不一定相同。
                // 读入point instance
                double[] data = new double[dimen];

                for (int i = 0; i < data.length; i++) {

                    data[i] = Double.parseDouble(cont[i]);
                    // System.out.println(data[i]);
                }
                // 这一行主要是为每个point instance生成一个ID，ID格式为：行号+数值，这样便于识别每个簇中都包含哪些数据
                // String mark=String.valueOf(point_num)+cont[data.length-1];
                // //这里是不是应该为cont[data.length-1]
                String mark = String.valueOf(point_num) + cont[data.length]; // 这里是不是应该为cont[data.length]

                // System.out.println(mark);
                // 根据一个point instance创建一个MinCluster
                CF cf = new CF(data);
                MinCluster subCluster = new MinCluster();
                subCluster.setCf(cf);
                subCluster.getInst_marks().add(mark);
                // 把新到的point instance插入树中
                root.absorbSubCluster(subCluster);
                // 要始终保证root是树的根节点
                while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                    root = root.getParent();
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public TreeNode buildBTree1() throws ReflectiveOperationException, Exception {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);

        String sqlURL = "jdbc:mysql://localhost:3306/tbm?useServerPrepStmts=true";
        String user = "root";
        String password = "tian123kong";
        Connection conn = SqlConnection.getConnection(sqlURL, user, password);
        // 这里选取四个属性,分别为：刀——盘转速、刀盘扭矩、总推进力和推进速度,attr5~attr8
        PreparedStatement psts = conn.prepareStatement("SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        psts.setFetchSize(Integer.MIN_VALUE);// 这个设置很关键。
        ResultSet result = psts.executeQuery();
        int i = 0;
        // while (result.next() && i < 8000000) {
        while (result.next()) {
            /*
             * System.out.println(++i + ": " + result.getDouble(2) + "--" +
             * result.getDouble(3) + "--" + result.getDouble(4) + "--" +
             * result.getDouble(5));
             */
            double[] data = new double[dimen];
            // 数量级最好统一
            data[0] = result.getDouble(2) / 5;
            data[1] = result.getDouble(3) / 3000;
            data[2] = result.getDouble(4) / 15000;
            data[3] = result.getDouble(5) / 25000;
            // 这一行主要是为每个point instance生成一个ID，ID格式为：行号+数值，这样便于识别每个簇中都包含哪些数据
            // String mark=String.valueOf(point_num)+cont[data.length-1];
            // //这里是不是应该为cont[data.length-1]
            // String mark=String.valueOf(point_num)+cont[data.length];
            // //这里是不是应该为cont[data.length]
            i++;
            String mark = String.valueOf(i) + " : good"; // 这里是不是应该为cont[data.length]

            // System.out.println(mark);
            // 根据一个point instance创建一个MinCluster
            CF cf = new CF(data);
            MinCluster subCluster = new MinCluster();
            subCluster.setCf(cf);
            subCluster.getInst_marks().add(mark);

            // 把新到的point instance插入树中
            root.absorbSubCluster(subCluster);// 这一行十分关键

            // 要始终保证root是树的根节点
            while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                root = root.getParent();
            }
        }
        conn.close();
        System.out.println("********************");
        System.out.println("总的施工记录为：" + i);
        System.out.println("********************");
        return root;
    }

    /**
     * 以施工片段方式从SQL中读取数据，并且先进行小波变换，还没有进行异常点检测
     *
     * @return
     * @throws ReflectiveOperationException
     * @throws Exception
     */
    public TreeNode buildBTree2() throws ReflectiveOperationException, Exception {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        TreeSet<String> totalDates = TotalDatesQuery.fetchAllDatesFromTxt();
        Iterator<String> it = totalDates.iterator();
        String temp;
        ArrayList<Time> temp1;
        Wavelet1 wavelet = new Wavelet1();
        String sqlURL = "jdbc:mysql://localhost:3306/tbm";
        String user = "root";
        String password = "tian123kong";
        Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
        PreparedStatement ps;
        ResultSet result;
        int count = 0;
        int size = totalDates.size();
        // while (it.hasNext() && count <200) {//先查看200天数据
        while (it.hasNext()) {// 先查看200天数据
            System.out.println("当前日期：" + (count + 1) + "/" + size);
            count++;
            temp = it.next();
            List<ArrayList<Time>> timeSlices = TimeSlice.fetchTimeSlices(temp.toString());
            for (int i = 0; i < timeSlices.size(); i++) {
                temp1 = timeSlices.get(i);
                ps = sqlconnect.prepareStatement(
                        "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                String start = temp + " " + temp1.get(0);
                String end = temp + " " + temp1.get(temp1.size() - 1);
                ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                ps.setTimestamp(2, Timestamp.valueOf(end));
                result = ps.executeQuery();
                result.last();// 移动到返回结果的最后一行
                int rowCount = result.getRow();// 获取行数
                result.beforeFirst();// 重新回到结果的第一行
                if (rowCount > 10) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                    double[] attr5 = new double[rowCount];
                    double[] attr6 = new double[rowCount];
                    double[] attr7 = new double[rowCount];
                    double[] attr8 = new double[rowCount];
                    int j = 0;
                    while (result.next()) {
                        attr5[j] = result.getDouble(2);
                        attr6[j] = result.getDouble(3);
                        attr7[j] = result.getDouble(4);
                        attr8[j] = result.getDouble(5);
                        j++;
                    }
                    // 小波变换降噪
                    attr5 = wavelet.denoise(attr5);
                    attr6 = wavelet.denoise(attr6);
                    attr7 = wavelet.denoise(attr7);
                    attr8 = wavelet.denoise(attr8);
                    for (int k = 0; k < rowCount; k++) {
                        double[] data = new double[dimen];
                        data[0] = attr5[k] / 5;
                        data[1] = attr6[k] / 3000;
                        data[2] = attr7[k] / 15000;
                        data[3] = attr8[k] / 25000;
                        String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]

                        // System.out.println(mark);
                        // 根据一个point instance创建一个MinCluster
                        CF cf = new CF(data);
                        MinCluster subCluster = new MinCluster();
                        subCluster.setCf(cf);
                        subCluster.getInst_marks().add(mark);

                        // 把新到的point instance插入树中
                        root.absorbSubCluster(subCluster);// 这一行十分关键

                        // 要始终保证root是树的根节点
                        while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                            root = root.getParent();
                        }
                    }
                    // result.close();
                    // ps.close();
                    // sqlconnect.close();
                }
            }

        }
        return root;
    }

    /**
     * 加入异常点检测
     *
     * @return
     * @throws Exception
     * @throws ReflectiveOperationException
     */


    /**
     * 这个程序终于正确了。 大块的数组拷贝和频繁的数据库连接消耗计算机内存。
     *
     * @return
     * @throws ReflectiveOperationException
     * @throws Exception
     */
    public TreeNode buildBTree5() throws ReflectiveOperationException, Exception {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        TreeSet<String> totalDates = TotalDatesQuery.fetchAllDatesFromTxt();
        Iterator<String> it = totalDates.iterator();
        String temp;
        ArrayList<String> temp1;
        Wavelet1 wavelet = new Wavelet1();
        int count = 0;
        int size = totalDates.size();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        String start;
        String end;
        int rowCount;
        ArrayList<double[]> withOutlier;
        List<ArrayList<String>> timeSlices;
        PreparedStatement ps;
        ResultSet result;
        String sqlURL = "jdbc:mysql://localhost:3306/tbm";
        String user = "root";
        String password = "tian123kong";
        Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
        while (it.hasNext()) {// 先查看200天数据
            System.out.println("当前日期：" + count + "/" + size);
            count++;
            temp = it.next();
            System.out.println(temp);
            timeSlices = TimeSlice.fetchTimeSlices1(temp);// 这一行语句会出问题？？？
            // for (int i = 0; i < timeSlices.size(); i++) {
            for (int i = 0; i < timeSlices.size(); i++) {
                ps = sqlconnect.prepareStatement(
                        "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                temp1 = timeSlices.get(i);
                // 这条语句极有可能造成内存泄漏
                start = temp + " " + temp1.get(0);
                end = temp + " " + temp1.get(1);
                ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                ps.setTimestamp(2, Timestamp.valueOf(end));
                result = ps.executeQuery();
                result.last();// 移动到返回结果的最后一行
                rowCount = result.getRow();// 获取行数
                // System.out.println("Slice length = " + rowCount);
                result.beforeFirst();// 重新回到结果的第一行
                if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                    attr5 = new double[rowCount];
                    attr6 = new double[rowCount];
                    attr7 = new double[rowCount];
                    attr8 = new double[rowCount];
                    int j = 0;
                    while (result.next()) {
                        attr5[j] = result.getDouble(2);
                        attr6[j] = result.getDouble(3);
                        attr7[j] = result.getDouble(4);
                        attr8[j] = result.getDouble(5);
                        j++;
                    }
                    result.close();
                    ps.close();
                    attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                    attr6 = wavelet.denoise(attr6);
                    attr7 = wavelet.denoise(attr7);
                    attr8 = wavelet.denoise(attr8);
                    withOutlier = new ArrayList<>();
                    // 异常点检测
                    withOutlier.add(attr5);
                    withOutlier.add(attr6);
                    withOutlier.add(attr7);
                    withOutlier.add(attr8);
                    double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                    double limit = StatUtils.percentile(mahaDist, 90);
                    // System.out.println("异常点检测之后的长度为：" + rowCount1);
                    // 这里加一个高斯异常点检测？
                    for (int k = 0; k < rowCount; k++) {
                        if (mahaDist[k] < limit) {
                            data = new double[dimen];
                            data[0] = attr5[k] / 5;
                            data[1] = attr6[k] / 3000;
                            data[2] = attr7[k] / 15000;
                            data[3] = attr8[k] / 25000;
                            String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                            // System.out.println(mark);
                            // 根据一个point instance创建一个MinCluster
                            cf = new CF(data);
                            subCluster = new MinCluster();
                            subCluster.setCf(cf);
                            subCluster.getInst_marks().add(mark);
                            // 把新到的point instance插入树中
                            root.absorbSubCluster(subCluster);// 这一行十分关键
                            // System.gc();
                            // 要始终保证root是树的根节点
                            while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                                root = root.getParent();
                            }
                        }

                    }

                }
            }
        }
        sqlconnect.close();
        return root;
    }

    public TreeNode buildBTree6() throws ReflectiveOperationException, Exception {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        // 这个程序已经经过了优化，直接从硬盘加载所有施工日期
        TreeSet<String> totalDates = TotalDatesQuery.fetchAllDatesFromTxt();// 这个程序已经经过了优化，直接从硬盘加载所有施工日期
        Iterator<String> it = totalDates.iterator();
        String temp;
        ArrayList<String> temp1;
        Wavelet1 wavelet = new Wavelet1();
        int count = 0;
        int size = totalDates.size();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        String start;
        String end;
        int rowCount;
        ArrayList<double[]> withOutlier;
        List<ArrayList<String>> timeSlices;
        PreparedStatement ps;
        ResultSet result;
        String sqlURL = "jdbc:mysql://localhost:3306/tbm";
        String user = "root";
        String password = "tian123kong";
        Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
        while (it.hasNext()) {// 先查看200天数据
            System.out.println("当前日期：" + count + "/" + size);
            count++;
            temp = it.next();
            System.out.println(temp);
            // 下面这行代码，也有较大的资源消耗
            timeSlices = TimeSlice.fetchTimeSlices1(temp);// 这一行语句会出问题？？？

            // for (int i = 0; i < timeSlices.size(); i++) {
            for (int i = 0; i < timeSlices.size(); i++) {
                ps = sqlconnect.prepareStatement(
                        "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
                temp1 = timeSlices.get(i);
                // 这条语句极有可能造成内存泄漏
                start = temp + " " + temp1.get(0);
                end = temp + " " + temp1.get(1);
                ps.setTimestamp(1, Timestamp.valueOf(start));// 这种方法果然快很多,毕竟主键是建过索引的。
                ps.setTimestamp(2, Timestamp.valueOf(end));
                result = ps.executeQuery();
                result.last();// 移动到返回结果的最后一行
                rowCount = result.getRow();// 获取行数
                // System.out.println("Slice length = " + rowCount);
                result.beforeFirst();// 重新回到结果的第一行
                if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                    attr5 = new double[rowCount];
                    attr6 = new double[rowCount];
                    attr7 = new double[rowCount];
                    attr8 = new double[rowCount];
                    int j = 0;
                    while (result.next()) {
                        attr5[j] = result.getDouble(2);
                        attr6[j] = result.getDouble(3);
                        attr7[j] = result.getDouble(4);
                        attr8[j] = result.getDouble(5);
                        j++;
                    }
                    result.close();
                    ps.close();
                    attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                    attr6 = wavelet.denoise(attr6);
                    attr7 = wavelet.denoise(attr7);
                    attr8 = wavelet.denoise(attr8);
                    withOutlier = new ArrayList<>();
                    // 异常点检测
                    withOutlier.add(attr5);
                    withOutlier.add(attr6);
                    withOutlier.add(attr7);
                    withOutlier.add(attr8);
                    double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                    double limit = StatUtils.percentile(mahaDist, 90);
                    // System.out.println("异常点检测之后的长度为：" + rowCount1);
                    // 这里加一个高斯异常点检测？
                    for (int k = (int) (rowCount * 0.1); k < (int) (rowCount * 0.9); k++) {// 剔除掉TBM启动和停止阶段各10%数据
                        if (mahaDist[k] < limit) {
                            data = new double[dimen];
                            data[0] = attr5[k] / 5;
                            data[1] = attr6[k] / 3000;
                            data[2] = attr7[k] / 15000;
                            data[3] = attr8[k] / 25000;
                            String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                            // System.out.println(mark);
                            // 根据一个point instance创建一个MinCluster
                            cf = new CF(data);
                            subCluster = new MinCluster();
                            subCluster.setCf(cf);
                            subCluster.getInst_marks().add(mark);
                            // 把新到的point instance插入树中
                            root.absorbSubCluster(subCluster);// 这一行十分关键
                            // System.gc();
                            // 要始终保证root是树的根节点
                            while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                                root = root.getParent();
                            }
                        }

                    }

                }
            }
        }
        sqlconnect.close();
        return root;
    }

    public TreeNode buildBTree7() throws Throwable {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        Wavelet1 wavelet = new Wavelet1();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        int rowCount;
        ArrayList<double[]> withOutlier;
        PreparedStatement ps;
        ResultSet result;
        String sqlURL = "jdbc:mysql://localhost:3306/tbm";
        String user = "root";
        String password = "tian123kong";
        Connection sqlconnect = SqlConnection.getConnection(sqlURL, user, password);
        ArrayList<String> timeSlices;
        timeSlices = TimeSlice.fetchTimeSlicesFromTxt();
        int timeSlicesSize = timeSlices.size();
        Iterator<String> it = timeSlices.iterator();
        int sliceCount = 0;
        while (it.hasNext()) {
            String ttemp = it.next();
            System.out.println("当前位于：" + sliceCount + "/" + timeSlicesSize);
            sliceCount++;
            String[] ttemp1 = ttemp.split(",");
            ps = sqlconnect.prepareStatement(
                    "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
            // 这条语句极有可能造成内存泄漏
            ps.setTimestamp(1, Timestamp.valueOf(ttemp1[0]));// 这种方法果然快很多,毕竟主键是建过索引的。
            ps.setTimestamp(2, Timestamp.valueOf(ttemp1[1]));
            result = ps.executeQuery();
            result.last();// 移动到返回结果的最后一行
            rowCount = result.getRow();// 获取行数
            // System.out.println("Slice length = " + rowCount);
            result.beforeFirst();// 重新回到结果的第一行
            if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                attr5 = new double[rowCount];
                attr6 = new double[rowCount];
                attr7 = new double[rowCount];
                attr8 = new double[rowCount];
                int j = 0;
                while (result.next()) {
                    attr5[j] = result.getDouble(2);
                    attr6[j] = result.getDouble(3);
                    attr7[j] = result.getDouble(4);
                    attr8[j] = result.getDouble(5);
                    j++;
                }
                result.close();
                ps.close();
                attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                attr6 = wavelet.denoise(attr6);
                attr7 = wavelet.denoise(attr7);
                attr8 = wavelet.denoise(attr8);
                withOutlier = new ArrayList<>();
                // 异常点检测
                withOutlier.add(attr5);
                withOutlier.add(attr6);
                withOutlier.add(attr7);
                withOutlier.add(attr8);
                double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                double limit = StatUtils.percentile(mahaDist, 90);
                // System.out.println("异常点检测之后的长度为：" + rowCount1);
                // 这里加一个高斯异常点检测？
                for (int k = (int) (rowCount * 0.1); k < (int) (rowCount * 0.9); k++) {// 剔除掉TBM启动和停止阶段各10%数据
                    if (mahaDist[k] < limit) {
                        data = new double[dimen];
                        data[0] = attr5[k] / 5;
                        data[1] = attr6[k] / 3000;
                        data[2] = attr7[k] / 15000;
                        data[3] = attr8[k] / 25000;
                        String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                        // System.out.println(mark);
                        // 根据一个point instance创建一个MinCluster
                        cf = new CF(data);
                        subCluster = new MinCluster();
                        subCluster.setCf(cf);
                        subCluster.getInst_marks().add(mark);
                        // 把新到的point instance插入树中
                        root.absorbSubCluster(subCluster);// 这一行十分关键
                        // System.gc();
                        // 要始终保证root是树的根节点
                        while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                            root = root.getParent();
                        }
                    }

                }

            }
        }
        // 先查看200天数据
        // 下面这行代码，也有较大的资源消耗

        // for (int i = 0; i < timeSlices.size(); i++) {

        sqlconnect.close();
        return root;
    }

    public TreeNode buildBTree8() throws Throwable {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        Wavelet1 wavelet = new Wavelet1();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        int rowCount;
        ArrayList<double[]> withOutlier;
        PreparedStatement ps;
        ResultSet result;
		/*String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";*/
        Connection sqlconnect = DBCPUtil.getConnection();
        ArrayList<String> timeSlices;
        timeSlices = TimeSlice.fetchTimeSlicesFromTxt();
        int timeSlicesSize = timeSlices.size();
        Iterator<String> it = timeSlices.iterator();
        int sliceCount = 0;
        while (it.hasNext()/* && sliceCount<1000*/) {
            String ttemp = it.next();
            System.out.println("当前位于：" + sliceCount + "/" + timeSlicesSize);
            sliceCount++;
            String[] ttemp1 = ttemp.split(",");
            ps = sqlconnect.prepareStatement(
                    "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
            // 这条语句极有可能造成内存泄漏
            ps.setTimestamp(1, Timestamp.valueOf(ttemp1[0]));// 这种方法果然快很多,毕竟主键是建过索引的。
            ps.setTimestamp(2, Timestamp.valueOf(ttemp1[1]));
            result = ps.executeQuery();
            result.last();// 移动到返回结果的最后一行
            rowCount = result.getRow();// 获取行数
            // System.out.println("Slice length = " + rowCount);
            result.beforeFirst();// 重新回到结果的第一行
            if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                attr5 = new double[rowCount];
                attr6 = new double[rowCount];
                attr7 = new double[rowCount];
                attr8 = new double[rowCount];
                int j = 0;
                while (result.next()) {
                    attr5[j] = result.getDouble(2);
                    attr6[j] = result.getDouble(3);
                    attr7[j] = result.getDouble(4);
                    attr8[j] = result.getDouble(5);
                    j++;
                }
                result.close();
                ps.close();

                withOutlier = new ArrayList<>();
                // 异常点检测
                withOutlier.add(attr5);
                withOutlier.add(attr6);
                withOutlier.add(attr7);
                withOutlier.add(attr8);
                double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                double limit = StatUtils.percentile(mahaDist, 90);
                //先异常点检测，再进行小波变换
                attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                attr6 = wavelet.denoise(attr6);
                attr7 = wavelet.denoise(attr7);
                attr8 = wavelet.denoise(attr8);
                // System.out.println("异常点检测之后的长度为：" + rowCount1);
                // 这里加一个高斯异常点检测？
                for (int k = (int) (rowCount * 0.2); k < rowCount; k++) {// 剔除掉TBM启动和停止阶段各10%数据
                    if (mahaDist[k] < limit) {
                        data = new double[dimen];
                        data[0] = attr5[k] / 5;//这几个规则基本没问题
                        data[1] = attr6[k] / 3000;
                        data[2] = attr7[k] / 15000;
//						data[3] = attr8[k] / 25000;
                        data[3] = attr8[k] / 100;
                        String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                        // System.out.println(mark);
                        // 根据一个point instance创建一个MinCluster
                        cf = new CF(data);
                        subCluster = new MinCluster();
                        subCluster.setCf(cf);
                        subCluster.getInst_marks().add(mark);
                        // 把新到的point instance插入树中
                        root.absorbSubCluster(subCluster);// 这一行十分关键
                        // System.gc();
                        // 要始终保证root是树的根节点
                        while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                            root = root.getParent();
                        }
                    }

                }

            }
        }
        // 先查看200天数据
        // 下面这行代码，也有较大的资源消耗

        // for (int i = 0; i < timeSlices.size(); i++) {

        sqlconnect.close();
        return root;
    }

    /**
     * 这个程序用来动态显示叶子节点，展示地质演化过程
     *
     * @return
     * @throws Throwable
     */
    public ArrayList<ArrayList<Double>> getData() {
        return dataOfPlot;
    }

    public TreeNode buildBTree9() throws Throwable {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        Wavelet1 wavelet = new Wavelet1();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        int rowCount;
        ArrayList<double[]> withOutlier;
        PreparedStatement ps;
        ResultSet result;
		/*String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";*/
        Connection sqlconnect = DBCPUtil.getConnection();
        ArrayList<String> timeSlices;
        timeSlices = TimeSlice.fetchTimeSlicesFromTxt();
        int timeSlicesSize = timeSlices.size();
        Iterator<String> it = timeSlices.iterator();
        int sliceCount = 0;
        while (it.hasNext()) {
            String ttemp = it.next();
            System.out.println("当前位于：" + sliceCount + "/" + timeSlicesSize);
            sliceCount++;
            String[] ttemp1 = ttemp.split(",");
            ps = sqlconnect.prepareStatement(
                    "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
            // 这条语句极有可能造成内存泄漏
            ps.setTimestamp(1, Timestamp.valueOf(ttemp1[0]));// 这种方法果然快很多,毕竟主键是建过索引的。
            ps.setTimestamp(2, Timestamp.valueOf(ttemp1[1]));
            result = ps.executeQuery();
            result.last();// 移动到返回结果的最后一行
            rowCount = result.getRow();// 获取行数
            // System.out.println("Slice length = " + rowCount);
            result.beforeFirst();// 重新回到结果的第一行
            if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                attr5 = new double[rowCount];
                attr6 = new double[rowCount];
                attr7 = new double[rowCount];
                attr8 = new double[rowCount];
                int j = 0;
                while (result.next()) {
                    attr5[j] = result.getDouble(2);
                    attr6[j] = result.getDouble(3);
                    attr7[j] = result.getDouble(4);
                    attr8[j] = result.getDouble(5);
                    j++;
                }
                result.close();
                ps.close();
                withOutlier = new ArrayList<>();
                // 异常点检测
                withOutlier.add(attr5);
                withOutlier.add(attr6);
                withOutlier.add(attr7);
                withOutlier.add(attr8);
                double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                double limit = StatUtils.percentile(mahaDist, 90);
                //先异常点检测，再进行小波变换
                attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                attr6 = wavelet.denoise(attr6);
                attr7 = wavelet.denoise(attr7);
                attr8 = wavelet.denoise(attr8);
                // System.out.println("异常点检测之后的长度为：" + rowCount1);
                // 这里加一个高斯异常点检测？
                for (int k = (int) (rowCount * 0.2); k < rowCount; k++) {// 剔除掉TBM启动和停止阶段各10%数据
                    if (mahaDist[k] < limit) {
                        data = new double[dimen];
                        data[0] = attr5[k] / 5;//这几个规则基本没问题
                        data[1] = attr6[k] / 3000;
                        data[2] = attr7[k] / 15000;
//						data[3] = attr8[k] / 25000;
                        data[3] = attr8[k] / 100;
                        String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                        // System.out.println(mark);
                        // 根据一个point instance创建一个MinCluster
                        cf = new CF(data);
                        subCluster = new MinCluster();
                        subCluster.setCf(cf);
                        subCluster.getInst_marks().add(mark);
                        // 把新到的point instance插入树中
                        root.absorbSubCluster(subCluster);// 这一行十分关键
                        // System.gc();
                        // 要始终保证root是树的根节点
                        while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                            root = root.getParent();
                        }

                    }

                }

            }
            dataOfPlot.clear();//这样很浪费内存啊
            ArrayList<CF> cf_total = this.getAllMinClusters(leafNodeHead);
            System.out.println("CF数目为： " + cf_total.size());

            Iterator<CF> cf_total_it = cf_total.iterator();
            while (cf_total_it.hasNext()) {
                CF temp = cf_total_it.next();
                ArrayList<Double> plotTemp = new ArrayList<>();
                double temp1 = temp.getLS()[1] / temp.getN();
                double temp2 = temp.getLS()[2] / temp.getN();
                if(temp1 > 0 && temp1 <10 && temp2 > 0 && temp2 <10) {
                    plotTemp.add(temp1);
                    plotTemp.add(temp2);
                    dataOfPlot.add(plotTemp);
                }
            }
        }
        sqlconnect.close();
        return root;
    }

    public TreeNode buildBTree10(ScatterChart<Number, Number> scatterChart) throws Throwable {
        // 从数据库中逐行读取数据并将其存入B-树中
        LeafNode leaf = new LeafNode();
        TreeNode root = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        Wavelet1 wavelet = new Wavelet1();
        double[] attr5;
        double[] attr6;
        double[] attr7;
        double[] attr8;
        double[] data;// 插入birch的Instance
        CF cf;
        MinCluster subCluster;
        int rowCount;
        ArrayList<double[]> withOutlier;
        PreparedStatement ps;
        ResultSet result;
		/*String sqlURL = "jdbc:mysql://localhost:3306/tbm";
		String user = "root";
		String password = "tian123kong";*/
        Connection sqlconnect = DBCPUtil.getConnection();
        ArrayList<String> timeSlices;
        timeSlices = TimeSlice.fetchTimeSlicesFromTxt();
        int timeSlicesSize = timeSlices.size();
        Iterator<String> it = timeSlices.iterator();
        int sliceCount = 0;
        while (it.hasNext()) {
            String ttemp = it.next();
            System.out.println("当前位于：" + sliceCount + "/" + timeSlicesSize);
            sliceCount++;
            String[] ttemp1 = ttemp.split(",");
            ps = sqlconnect.prepareStatement(
                    "SELECT attr3,attr5,attr6,attr7,attr8 FROM tbmrecordcleaned WHERE attr3 >= ? AND attr3 <= ?");
            // 这条语句极有可能造成内存泄漏
            ps.setTimestamp(1, Timestamp.valueOf(ttemp1[0]));// 这种方法果然快很多,毕竟主键是建过索引的。
            ps.setTimestamp(2, Timestamp.valueOf(ttemp1[1]));
            result = ps.executeQuery();
            result.last();// 移动到返回结果的最后一行
            rowCount = result.getRow();// 获取行数
            // System.out.println("Slice length = " + rowCount);
            result.beforeFirst();// 重新回到结果的第一行
            if (rowCount > 20) {// 如果一个施工片段包含的施工记录小于10，则直接删除该片段
                attr5 = new double[rowCount];
                attr6 = new double[rowCount];
                attr7 = new double[rowCount];
                attr8 = new double[rowCount];
                int j = 0;
                while (result.next()) {
                    attr5[j] = result.getDouble(2);
                    attr6[j] = result.getDouble(3);
                    attr7[j] = result.getDouble(4);
                    attr8[j] = result.getDouble(5);
                    j++;
                }
                result.close();
                ps.close();
                withOutlier = new ArrayList<>();
                // 异常点检测
                withOutlier.add(attr5);
                withOutlier.add(attr6);
                withOutlier.add(attr7);
                withOutlier.add(attr8);
                double[] mahaDist = OutlierDetect.mahaDistance1(withOutlier);
                double limit = StatUtils.percentile(mahaDist, 90);
                //先异常点检测，再进行小波变换
                attr5 = wavelet.denoise(attr5);// 是这段代码的问题啊
                attr6 = wavelet.denoise(attr6);
                attr7 = wavelet.denoise(attr7);
                attr8 = wavelet.denoise(attr8);
                // System.out.println("异常点检测之后的长度为：" + rowCount1);
                // 这里加一个高斯异常点检测？
                for (int k = (int) (rowCount * 0.2); k < rowCount; k++) {// 剔除掉TBM启动和停止阶段各10%数据
                    if (mahaDist[k] < limit) {
                        data = new double[dimen];
                        data[0] = attr5[k] / 5;//这几个规则基本没问题
                        data[1] = attr6[k] / 3000;
                        data[2] = attr7[k] / 15000;
//						data[3] = attr8[k] / 25000;
                        data[3] = attr8[k] / 100;
                        String mark = String.valueOf(k) + " : good"; // 这里是不是应该为cont[data.length]
                        // System.out.println(mark);
                        // 根据一个point instance创建一个MinCluster
                        cf = new CF(data);
                        subCluster = new MinCluster();
                        subCluster.setCf(cf);
                        subCluster.getInst_marks().add(mark);
                        // 把新到的point instance插入树中
                        root.absorbSubCluster(subCluster);// 这一行十分关键
                        // System.gc();
                        // 要始终保证root是树的根节点
                        while (root.getParent() != null) { // 因为有可能发生簇的分裂，根节点会发生变化，因此将全部点插入树之后需要进行检验
                            root = root.getParent();
                        }

                    }

                }

            }
            ArrayList<CF> cf_total = this.getAllMinClusters(leafNodeHead);
            System.out.println("CF数目为： " + cf_total.size());
            Iterator<CF> cf_total_it = cf_total.iterator();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            while (cf_total_it.hasNext()) {
                CF temp = cf_total_it.next();
                series.getData().add(new XYChart.Data<>(temp.getLS()[1] / temp.getN(), temp.getLS()[2] / temp.getN()));
            }
            Platform.runLater(()->{
                scatterChart.getData().set(0, series);

                try {
                    Thread.sleep(100);//这一行代码可能并没有什么屌用
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        sqlconnect.close();
        return root;
    }

    public TreeNode copyTree() {
        LeafNode leafNodeHead1 = new LeafNode();
        LeafNode leaf = new LeafNode();
        TreeNode newTree = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead1.setNext(leaf);
        leaf.setPre(leafNodeHead1);
        LeafNode temp;
        while (this.leafNodeHead.getNext() != null) {
            temp = this.leafNodeHead.getNext();
            for (MinCluster cluster : temp.getChildren()) {
                newTree.absorbSubCluster(cluster);
            }
        }
        return newTree;
    }

    public LeafNode copyTree1() {
        LeafNode leafNodeHead1 = new LeafNode();
        LeafNode leaf = new LeafNode();
        TreeNode newTree = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead1.setNext(leaf);
        leaf.setPre(leafNodeHead1);
        LeafNode temp;
        while (this.leafNodeHead.getNext() != null) {
            temp = this.leafNodeHead.getNext();
            for (MinCluster cluster : temp.getChildren()) {
                newTree.absorbSubCluster(cluster);
            }
        }
        return leafNodeHead1;
    }

    /**
     * @return 返回按新的T值生成的新B-树。 需要注意：要一边删除
     */
    public LeafNode copyTree2() {
        LeafNode leafNodeHead1 = new LeafNode();
        LeafNode leaf = new LeafNode();
        TreeNode newTree = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead1.setNext(leaf);
        leaf.setPre(leafNodeHead1);
        LeafNode temp = this.leafNodeHead.getNext();
        while (temp != null) {
            Iterator<MinCluster> it = temp.getChildren().iterator();
            while (it.hasNext()) {
                MinCluster minCluster = (MinCluster) it.next();
                newTree.absorbSubCluster(minCluster);
                it.remove();
            }
            temp = temp.getNext();
        }
        return leafNodeHead1;
    }

    /**
     * @param limit 小于此limit的微簇不用于BIRCH树的重建
     * @return
     */
    public LeafNode copyTree3(int limit) {
        LeafNode leafNodeHead1 = new LeafNode();
        LeafNode leaf = new LeafNode();
        TreeNode newTree = leaf;// 建树从第一个叶子节点开始
        // 把叶子节点加入存储叶子节点的双向链表
        leafNodeHead1.setNext(leaf);
        leaf.setPre(leafNodeHead1);
        LeafNode temp = this.leafNodeHead.getNext();
        while (temp != null) {
            Iterator<MinCluster> it = temp.getChildren().iterator();
            while (it.hasNext()) {
                MinCluster minCluster = (MinCluster) it.next();
                if (minCluster.getCf().getN() >= limit) {
                    newTree.absorbSubCluster(minCluster);
                }
                it.remove();
            }
            temp = temp.getNext();
        }
        return leafNodeHead1;
    }

    // 打印B-树的所有叶子节点
    public void printLeaf(LeafNode header) {
        // point_num清0
        point_num = 0;
        while (header.getNext() != null) {
            // System.out.println("\n一个叶子节点:");
            System.out.println("\n一个叶子节点,该叶子节点包含的微簇数目为：" + header.getNext().getChildren().size());
            header = header.getNext();
            for (MinCluster cluster : header.getChildren()) {
                // System.out.println("\n一个最小簇:");
                System.out.println("\n一个最小簇,该簇包含的记录数目为：" + cluster.getCf().getN());
                // for (String mark : cluster.getInst_marks()) {
                // point_num++;
                // System.out.print(mark + "\t");
                // }
            }
        }
        System.out.println("$$$$$$$$$$$$");
    }

    // 打印指定根节点的子树
    public void printTree(TreeNode root) {
        if (!root.getClass().getName().equals("birch.LeafNode")) {
            NonleafNode nonleaf = (NonleafNode) root;
            for (TreeNode child : nonleaf.getChildren()) {
                printTree(child); // 通过递归的方式打印
            }
        } else {
            System.out.println("\n一个叶子节点NNN:");
            LeafNode leaf = (LeafNode) root;
            System.out.println("该子树包含的子项数目为：" + leaf.getChildren().size());
            for (MinCluster cluster : leaf.getChildren()) {
                System.out.println("\n一个最小簇:");
                System.out.println("该最小簇包含的记录数目为：" + cluster.getCf().getN());
                for (String mark : cluster.getInst_marks()) {
                    System.out.print(mark + "\t");
                    point_num++;
                }
            }
        }
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$");
    }

    /**
     * @param root  B-树的根节点
     * @param limit 小于此值的LeafNode节点的子项将被删除
     * @return 返回剔除了异常点后的根节点 这种方法有缺点，有可能导致一个叶子节点中的所有子项全部被删除。
     */
    public TreeNode outlier(TreeNode root, int limit) {
        if (!root.getClass().getName().equals("birch.LeafNode")) {
            NonleafNode nonleaf = (NonleafNode) root;
            for (TreeNode child : nonleaf.getChildren()) {
                outlier(child, limit); // 通过递归的方式打印
            }
        } else {
            System.out.println("\n一个叶子节点OOO:");
            LeafNode leaf = (LeafNode) root;
            System.out.println("该子树包含的子项数目为：" + leaf.getChildren().size());
            // 采用迭代器来删除元素。
            Iterator<MinCluster> it = leaf.getChildren().iterator();
            while (it.hasNext()) {
                MinCluster minCluster = (MinCluster) it.next();
                if (minCluster.getCf().getN() < limit) {

                    leaf.deleteCFUpToRoot(minCluster.getCf());// 应该加上这么一句删除更新操作语句。

                    System.out.println("删除掉了该叶子节点中的一个minCluster,数量为：" + minCluster.getCf().getN());
                    it.remove();
                }
            }
        }
        // 此处还应该加入删除空的叶子节点的操作
        return root;
    }

    /**
     * 该函数应该紧跟上述outlier函数
     *
     * @param root
     * @return
     */
    public TreeNode condense(TreeNode root) {
        if (!root.getClass().getName().equals("birch.LeafNode")) {
            NonleafNode nonleaf = (NonleafNode) root;
            for (TreeNode child : nonleaf.getChildren()) {
                condense(child); // 通过递归的方式打印
            }
        } else {
            System.out.println("\n一个叶子节点CCC:");
            LeafNode leaf = (LeafNode) root;
            if (leaf.getChildren().size() == 0) {
                LeafNode temp;
                do {
                    temp = leaf.getNext();
                } while (temp.getChildren().size() == 0);
                leaf.getPre().setNext(temp);
            }
        }
        return root;
    }

    public ArrayList<CF> getAllMinClusters(LeafNode head) {
        int count = 0;// 记录微簇的数量
        ArrayList<CF> cf = new ArrayList<>();
        while (head.getNext() != null) {
            head = head.getNext();
            for (MinCluster cluster : head.getChildren()) {
                cf.add(cluster.getCf());
                count++;
            }
        }
        System.out.println("该Birch结构所包含的微簇数目为：" + count);
        return cf;
    }

    public void kmeans(ArrayList<CF> input) throws REngineException {
        int size = input.size();
        double[] rspeed = new double[size];
        double[] torque = new double[size];
        double[] thrust_force = new double[size];
        double[] advance_rate = new double[size];

        for (int i = 0; i < input.size(); i++) {
            CF temp = input.get(i);
            rspeed[i] = temp.getLS()[0] / temp.getN();
            torque[i] = temp.getLS()[1] / temp.getN();
            thrust_force[i] = temp.getLS()[2] / temp.getN();
            advance_rate[i] = temp.getLS()[3] / temp.getN();
        }
        System.out.println("单个数组长度为：" + rspeed.length);
        System.out.println("现在输出数组中内容：");
        for (int i = 0; i < size; i++) {
            System.out.println(i + ":" + rspeed[i] + "," + torque[i] + "," + thrust_force[i] + "," + advance_rate[i]);
        }
        RConnection rconnect = new RConnection("127.0.0.1");
        rconnect.assign("rspeed", rspeed);
        rconnect.assign("torque", torque);
        rconnect.assign("thrust_force", thrust_force);
        rconnect.assign("advance_rate", advance_rate);
        System.out.println("已经完成从java向R语言的数据传递！");

        // rconnect.eval("stream <-
        // matrix(c(rspeed,torque,thrust_force,advance_rate),ncol = 4,byrow =
        // FALSE)");
        // rconnect.eval("stream <- matrix(c(rspeed,thrust_force),ncol = 2,byrow
        // = FALSE)");
        rconnect.eval("stream1 <- matrix(c(torque,thrust_force),ncol = 2,byrow = FALSE)");
        rconnect.eval("stream <- stream1[-1,]");
        rconnect.eval("stream_cluster <- kmeans(stream,5)");

        // rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster-2.jpg')");//
        // 没进行规范化，直接聚类
        rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster7.jpg')");// 没进行规范化，直接聚类
        rconnect.eval("opar <- par(no.readonly=TRUE)");
        rconnect.eval("par(pin=c(5,5))");
        rconnect.eval("par(cex.axis=0.75, font.axis=3)");
        // rconnect.eval("plot(stream[c(0,2)],col = stream_cluster$cluster)");//
        // 注意转义符的使用
        rconnect.eval("plot(stream,col = stream_cluster$cluster)");// 注意转义符的使用
        rconnect.eval("points(stream_cluster$centers,col = 1:2,pch=25,cex=2)");// 绘制聚类中心
        rconnect.eval("par(opar)");
        rconnect.eval("dev.off()");
        rconnect.close();
    }

    public void kmeans1(ArrayList<CF> input) throws REngineException, Throwable {
        // 这个程序对4个变量再次进行异常点检测

        ArrayList<Double> rspeed = new ArrayList<>();
        ArrayList<Double> torque = new ArrayList<>();
        ArrayList<Double> thrust_force = new ArrayList<>();
        ArrayList<Double> advance_rate = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            CF temp = input.get(i);
            double[] ls = temp.getLS();
            int count = temp.getN();
            boolean condition = ls[0] / count <= 10 && ls[1] / count <= 10 && ls[2] / count <= 10
                    && ls[3] / count <= 10;
            if (condition) {
                rspeed.add(ls[0] / count);
                torque.add(ls[1] / count);
                thrust_force.add(ls[2] / count);
                advance_rate.add(ls[3] / count);
            }
        }
        System.out.println("单个数组长度为：" + rspeed.size());
        System.out.println("现在输出数组中内容：");
        int size = rspeed.size();
        double[] rspeed_double = new double[size];
        double[] torque_double = new double[size];
        double[] thrust_force_double = new double[size];
        double[] advance_rate_double = new double[size];
        for (int i = 0; i < size; i++) {
            System.out.println(i + ":" + rspeed.get(i) + "," + torque.get(i) + "," + thrust_force.get(i) + ","
                    + advance_rate.get(i));
            rspeed_double[i] = rspeed.get(i);
            torque_double[i] = torque.get(i);
            thrust_force_double[i] = thrust_force.get(i);
            advance_rate_double[i] = advance_rate.get(i);
        }
        RConnection rconnect = new RConnection("127.0.0.1");
        rconnect.assign("rspeed", rspeed_double);
        rconnect.assign("torque", torque_double);
        rconnect.assign("thrust_force", thrust_force_double);
        rconnect.assign("advance_rate", advance_rate_double);
        System.out.println("已经完成从java向R语言的数据传递！");

        // rconnect.eval("stream <-
        // matrix(c(rspeed,torque,thrust_force,advance_rate),ncol = 4,byrow =
        // FALSE)");
        // rconnect.eval("stream <- matrix(c(rspeed,thrust_force),ncol = 2,byrow
        // = FALSE)");
        rconnect.eval("stream1 <- matrix(c(torque,thrust_force),ncol = 2,byrow = FALSE)");
        rconnect.eval("stream <- stream1[-1,]");
        rconnect.eval("stream_cluster <- kmeans(stream,5)");

        // rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster-2.jpg')");//
        // 没进行规范化，直接聚类
        rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster16.jpg')");// 没进行规范化，直接聚类
        rconnect.eval("opar <- par(no.readonly=TRUE)");
        rconnect.eval("par(pin=c(5,5))");
        rconnect.eval("par(cex.axis=0.75, font.axis=3)");
        // rconnect.eval("plot(stream[c(0,2)],col = stream_cluster$cluster)");//
        // 注意转义符的使用
        rconnect.eval("plot(stream,col = stream_cluster$cluster)");// 注意转义符的使用
        rconnect.eval("points(stream_cluster$centers,col = 1:2,pch=25,cex=2)");// 绘制聚类中心
        rconnect.eval("par(opar)");
        rconnect.eval("dev.off()");
        rconnect.close();
    }

    /**
     * @throws Exception
     */
    public void kmeans2(ArrayList<CF> input) throws Exception {
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("rspeed"));
        attrs.add(new Attribute("torque"));
        attrs.add(new Attribute("thrust_force"));
        attrs.add(new Attribute("advance_rate"));
        int capacity = input.size();
        Instances data = new Instances("tbm_record", attrs, capacity);//所有的施工记录
        for (int i = 0; i < input.size(); i++) {
            CF temp = input.get(i);
            double[] ls = temp.getLS();
            int count = temp.getN();
            boolean condition = ls[0] / count <= 10 && ls[1] / count <= 10 && ls[2] / count <= 10
                    && ls[3] / count <= 10;
            if (condition) {
                ls[0] = ls[0] / count;
                ls[1] = ls[1] / count;
                ls[2] = ls[2] / count;
                ls[3] = ls[3] / count;
                Instance temp1 = new DenseInstance(1, ls);
                data.add(temp1);
            }
        }
        SimpleKMeans kmeans = new SimpleKMeans();
        String[] options = new String[4];
        options[0] = "-N";
        options[1] = "5";
        options[2] = "-init";
        options[3] = "1";
        kmeans.setOptions(options);
        kmeans.buildClusterer(data);//默认使用Eucliden distance
        ClusterEvaluation cv = new ClusterEvaluation();
        cv.setClusterer(kmeans);
        cv.evaluateClusterer(data);
        System.out.println(cv.clusterResultsToString());
    }

    public void kmeans3(ArrayList<CF> input) throws REngineException, Throwable {
        // 这个程序对4个变量再次进行异常点检测

        ArrayList<Double> rspeed = new ArrayList<>();
        ArrayList<Double> torque = new ArrayList<>();
        ArrayList<Double> thrust_force = new ArrayList<>();
        ArrayList<Double> advance_rate = new ArrayList<>();
        File outputFile = new File("config/LeafNodesBuffer2.txt");
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int i = 0; i < input.size(); i++) {
            CF temp = input.get(i);
            double[] ls = temp.getLS();
            int count = temp.getN();
            boolean condition = ls[0] / count <= 10 && ls[1] / count <= 10 && ls[2] / count <= 10
                    && ls[3] / count <= 10;
            if (condition) {
                printWriter.print(count);
                printWriter.print(",");
                rspeed.add(ls[0] / count);
                printWriter.print(ls[0]);
                printWriter.print(",");
                torque.add(ls[1] / count);
                printWriter.print(ls[1]);
                printWriter.print(",");
                thrust_force.add(ls[2] / count);
                printWriter.print(ls[2]);
                printWriter.print(",");
                advance_rate.add(ls[3] / count);
                printWriter.print(ls[3]);
                printWriter.println();
            }
        }
        printWriter.close();
        System.out.println("单个数组长度为：" + rspeed.size());
        System.out.println("现在输出数组中内容：");
        int size = rspeed.size();
        double[] rspeed_double = new double[size];
        double[] torque_double = new double[size];
        double[] thrust_force_double = new double[size];
        double[] advance_rate_double = new double[size];
        for (int i = 0; i < size; i++) {
            System.out.println(i + ":" + rspeed.get(i) + "," + torque.get(i) + "," + thrust_force.get(i) + ","
                    + advance_rate.get(i));
            rspeed_double[i] = rspeed.get(i);
            torque_double[i] = torque.get(i);
            thrust_force_double[i] = thrust_force.get(i);
            advance_rate_double[i] = advance_rate.get(i);
        }
        RConnection rconnect = new RConnection("127.0.0.1");
        rconnect.assign("rspeed", rspeed_double);
        rconnect.assign("torque", torque_double);
        rconnect.assign("thrust_force", thrust_force_double);
        rconnect.assign("advance_rate", advance_rate_double);
        System.out.println("已经完成从java向R语言的数据传递！");

        // rconnect.eval("stream <-
        // matrix(c(rspeed,torque,thrust_force,advance_rate),ncol = 4,byrow =
        // FALSE)");
        // rconnect.eval("stream <- matrix(c(rspeed,thrust_force),ncol = 2,byrow
        // = FALSE)");
//		rconnect.eval("stream1 <- matrix(c(torque,thrust_force),ncol = 2,byrow = FALSE)");
        rconnect.eval("stream1 <- matrix(c(rspeed，torque,thrust_force，advance_rate),ncol = 4,byrow = FALSE)");
        rconnect.eval("stream <- stream1[-1,]");
        rconnect.eval("stream_cluster <- kmeans(stream,5)");

        // rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster-2.jpg')");//
        // 没进行规范化，直接聚类
        rconnect.eval("jpeg('E://R//Plot//normal//stream-cluster17.jpg')");// 没进行规范化，直接聚类
        rconnect.eval("opar <- par(no.readonly=TRUE)");
        rconnect.eval("par(pin=c(5,5))");
        rconnect.eval("par(cex.axis=0.75, font.axis=3)");
        // rconnect.eval("plot(stream[c(0,2)],col = stream_cluster$cluster)");//
        // 注意转义符的使用
//		rconnect.eval("plot(stream,col = stream_cluster$cluster)");// 注意转义符的使用
        rconnect.eval(" scatterplot3d(stream[,1],stream[,2],stream[,3],col = stream_cluster$cluster)");// 注意转义符的使用
        rconnect.eval("points(stream_cluster$centers,col = 1:2,pch=25,cex=2)");// 绘制聚类中心
        rconnect.eval("par(opar)");
        rconnect.eval("dev.off()");
        rconnect.close();
    }

    public static void main(String[] args) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Params.setT(0.15);//坑，这行语句不要忘掉
        BIRCH birch = new BIRCH();
        birch.point_num = 0;
        birch.buildBTree8();// 这里编程小波变换
        System.out.println("fuck");
        System.out.println("现在开始打印所有叶子节点：");
        birch.printLeaf(birch.leafNodeHead);
        System.out.println("记录数目为： " + birch.point_num);
        System.out.println("************************************");
        System.out.println("---------------现在开始树的复制,不改变T值：------------------");
        LeafNode newTree = birch.copyTree3(50);
        birch.point_num = 0;
        birch.printLeaf(newTree);
        System.out.println("异常点检测后记录数目为： " + birch.point_num);
        System.out.println("*****************************");
        System.out.println("*****************************");
        // birch.globalCluster(newTree);
        ArrayList<CF> cf_total = birch.getAllMinClusters(newTree);
        System.out.println("现在开始输出微簇的信息：");
        for (int i = 0; i < cf_total.size(); i++) {
            System.out.println("这是第" + i + "个微簇，包含的记录数目为： " + cf_total.get(i).getN());
        }
        Long endTime = System.currentTimeMillis();
        Long runTime = startTime - endTime;
        System.out.println("BIRCH算法所用时间为：" + runTime + "ms");
        System.out.println("-------------现在开始绘图-----------");
        birch.kmeans3(cf_total);
//		birch.kmeans2(cf_total);
        System.out.println("-------绘图结束--------");
        System.out.println("--------------End-----------");
    }
}
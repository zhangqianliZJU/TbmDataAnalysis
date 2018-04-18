package com.zhangqianli.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

public class ImportMultiTxtToMysqlCleaned {
	public static void importTxts(Connection connect,List<File> files) throws Exception {
		connect.setAutoCommit(false); // 设置手动提交
		// 生成从txt导入数据到mysql的命令。
		String populateTable = ImportTxtToMysqlCommand.importSqlCommand();
		PreparedStatement psts = connect.prepareStatement(populateTable);
		String encoding = "UTF8";// 读取txt文档时采用的字符集。
		String lineText;
		long startTime = System.currentTimeMillis();// 数据导入的开始时间。
		System.out.format("需要导入的文件总数目为: %d%n", files.size());
		for (int i = 0; i < files.size(); i++) {
			long startTime1 = System.currentTimeMillis();// 数据导入的开始时间。
			System.out.format("现在正在导入第  %d 个文件:%n", i + 1);
			File file = files.get(i);
			System.out.println(file.getName());
			if (file.isFile() && file.exists()) {
				try (BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), encoding))) {
					bufferedReader.readLine();// 跨过第一行
					while ((lineText = bufferedReader.readLine()) != null) {
						String[] res = lineText.split("\t");
						boolean isZero = false;
							isZero = Double.parseDouble(res[4]) == 0.0 || Double.parseDouble(res[5]) == 0.0
									|| Double.parseDouble(res[6]) == 0.0 || Double.parseDouble(res[7]) == 0.0
									|| Double.parseDouble(res[128]) == 0.0;
						if (!isZero) {
							psts.setString(1, res[0]);
							psts.setLong(2, Long.parseLong(res[1]));
							psts.setTimestamp(3, Timestamp.valueOf(res[2]));
							for (int j = 3; j < 200; j++) {
								psts.setDouble(j + 1, Double.parseDouble(res[j]));
							}
							psts.addBatch();
						}
					}
					psts.executeBatch();
					connect.commit();
				}
				long endTime1 = System.currentTimeMillis();
				System.out.println("导入第  " + (i + 1) + " 个数据运行时间为：" + (endTime1 - startTime1) + "ms");
			}
		}
		connect.close();// 最后一定要记得关闭数据库连接释放占用的资源。
		long endTime = System.currentTimeMillis();
		System.out.println("导入所有数据的运行时间为：" + (endTime - startTime) + "ms");
	}
	public static void main(String[] args) throws Exception, Throwable {
		String sqlURL = "jdbc:mysql://localhost:3306/tbm?useServerPrepStmts=true";
		String user = "root";
		String password = "tian123kong";
		Connection connect = SqlConnection.getConnection(sqlURL, user, password);
		connect.setAutoCommit(false); // 设置手动提交
		// 生成从txt导入数据到mysql的命令。
		String populateTable = ImportTxtToMysqlCommand.importSqlCommand();
		PreparedStatement psts = connect.prepareStatement(populateTable);
		// System.out.println(populateTable);
		String txt_directory = "J:\\TBM_TXT";// txt文件所在路径
		File[] source = FetchFileList.getFileName(txt_directory);
		String encoding = "UTF8";// 读取txt文档时采用的字符集。
		String lineText;
		long startTime = System.currentTimeMillis();// 数据导入的开始时间。
		System.out.format("需要导入的文件总数目为: %d%n", source.length);
		for (int i = 0; i < source.length; i++) {
			long startTime1 = System.currentTimeMillis();// 数据导入的开始时间。
			System.out.format("现在正在导入第  %d 个文件:%n", i + 1);
			File file = source[i];
			System.out.println(file.getName());
			if (file.isFile() && file.exists()) {
				try (BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), encoding))) {
					bufferedReader.readLine();// 跨过第一行
					while ((lineText = bufferedReader.readLine()) != null) {
						String[] res = lineText.split("\t");
						boolean isZero = false;
							isZero = Double.parseDouble(res[4]) == 0.0 || Double.parseDouble(res[5]) == 0.0
									|| Double.parseDouble(res[6]) == 0.0 || Double.parseDouble(res[7]) == 0.0
									|| Double.parseDouble(res[128]) == 0.0;
						if (!isZero) {
							psts.setString(1, res[0]);
							psts.setLong(2, Long.parseLong(res[1]));
							psts.setTimestamp(3, Timestamp.valueOf(res[2]));
							for (int j = 3; j < 200; j++) {
								psts.setDouble(j + 1, Double.parseDouble(res[j]));
							}
							psts.addBatch();
						}
					}
					psts.executeBatch();
					connect.commit();
				}
				long endTime1 = System.currentTimeMillis();
				System.out.println("导入第  " + (i + 1) + " 个数据运行时间为：" + (endTime1 - startTime1) + "ms");
			}
		}
		connect.close();// 最后一定要记得关闭数据库连接释放占用的资源。
		long endTime = System.currentTimeMillis();
		System.out.println("导入所有数据的运行时间为：" + (endTime - startTime) + "ms");
	}
}

package com.zhangqianli.jdbc;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author zhangqianli 
 * 生成把txt文档读入mysql数据库的sql命令。
 * 有两个重载函数，返回类型为String类型的静态函数用于在程序中生成SQL命令，返回值为空的静态函数将生成的SQL命令存入一个txt文件。
 */
public class ImportTxtToMysqlCommand {
	private static StringBuffer sqlCommand = new StringBuffer();

	public static String importSqlCommand(/* String commandPath */) throws Exception {
		sqlCommand.append("INSERT INTO tbmrecordcleaned(\n");// 在此处修改数据库名称。
		for (int i = 1; i < 200; i++) {
			sqlCommand.append("attr" + i + ",\n");
		}
		sqlCommand.append("attr200)\n");
		sqlCommand.append("VALUES(\n");
		for (int i = 0; i < 199; i++) {
			// sqlCommand.append("res["+i+"],\n");
			sqlCommand.append("?,\n");
		}
		// sqlCommand.append("res["+199+"]);");
		sqlCommand.append("?)");

		return sqlCommand.toString();
		/*
		 * try(PrintWriter outputStream = new PrintWriter(new
		 * FileWriter(commandPath))) {
		 * outputStream.print(sqlCommand.toString()); }
		 */
	}

	public static void importSqlCommand(String commandPath) throws Exception {
		sqlCommand.append("INSERT INTO tbmrecordcleaned(\n");
		for (int i = 1; i < 200; i++) {
			sqlCommand.append("attr" + i + ",\n");
		}
		sqlCommand.append("attr200)\n");
		sqlCommand.append("VALUES(\n");
		for (int i = 0; i < 199; i++) {
			sqlCommand.append("?,\n");
		}
		sqlCommand.append("?);");
		// return sqlCommand.toString();

		try (PrintWriter outputStream = new PrintWriter(new FileWriter(commandPath))) {
			outputStream.print(sqlCommand.toString());
		}
	}

	public static void main(String[] args) throws Throwable {
		String commandPath = "E:/Eclipse/Kmeans/src/importSqlCommand1.txt";
		importSqlCommand(commandPath);
	}

}
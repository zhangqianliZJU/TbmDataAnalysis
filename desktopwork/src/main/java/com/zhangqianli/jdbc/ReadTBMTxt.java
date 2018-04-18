package com.zhangqianli.jdbc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author zhangqianli
 *
 */
public class ReadTBMTxt {
	private static int attributeNumber;// 记录单挑施工记录的属性数目。
	private static int nonZeroCount;// 记录非零行的数目。
	private static String encoding = "UTF8";// txt文件的编码方式。

	/**
	 * @param filePath
	 *            读入的TBM施工txt格式源文件
	 * @return 单条施工记录的属性数目，为200；
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static int printAttributeNumber(String filePath)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File(filePath);
		String lineText;
		if (file.isFile() && file.exists()) {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), encoding))) {
				lineText = bufferedReader.readLine();
				System.out.println(lineText);// 打印所有属性的名称。
				String[] split = lineText.split("\t");
				attributeNumber = split.length;
				for (int i = 0; i < attributeNumber; i++) {
					System.out.println(i + ": " + split[i]);
				}
			}
		}
		return attributeNumber;
	}

	// 将所有属性读取并且写入一个text文档。
	public static void writeAttributeNames(String filePath, String attributeNamesPath)
			throws IOException, Exception, Throwable {
		File file = new File(filePath);
		String lineText;
		if (file.isFile() && file.exists()) {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), encoding));
					PrintWriter outputStream = new PrintWriter(new FileWriter(attributeNamesPath))) {
				lineText = bufferedReader.readLine();
				String[] split = lineText.split("\t");
				attributeNumber = split.length;
				for (int i = 0; i < attributeNumber; i++) {
					outputStream.println(split[i]);
				}
			}
		}
	}

	public static void printNonZeroRecord(String inputFilePath)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File(inputFilePath);// txt文件路径
		String lineText;
		boolean isZero;
		if (file.isFile() && file.exists()) {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), encoding))) {
				System.out.println(bufferedReader.readLine());// 跨过第一行
				while ((lineText = bufferedReader.readLine()) != null) {
					String[] split = lineText.split("\t");
					isZero = Double.parseDouble(split[4]) != 0.0 && Double.parseDouble(split[5]) != 0.0
							&& Double.parseDouble(split[6]) != 0.0 && Double.parseDouble(split[7]) != 0.0
							&& Double.parseDouble(split[128]) != 0.0;
					if (isZero == true) {
						System.out.println(lineText);
						nonZeroCount++;
					}
				}
				System.out.format("Number of Nonzero Record is %d.\n", nonZeroCount);
			}
		}
	}

	public static void writeNonZeroRecordToTxt(String inputFilePath, String outputFilePath)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File(inputFilePath);// 输入txt文件路径
		String lineText;
		boolean isZero;
		if (file.isFile() && file.exists()) {
			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), encoding));
					PrintWriter outputStream = new PrintWriter(new FileWriter(outputFilePath))) {
				bufferedReader.readLine();
				while ((lineText = bufferedReader.readLine()) != null) {
					String[] split = lineText.split("\t");
					isZero = Double.parseDouble(split[4]) != 0.0 && Double.parseDouble(split[5]) != 0.0
							&& Double.parseDouble(split[6]) != 0.0 && Double.parseDouble(split[7]) != 0.0
							&& Double.parseDouble(split[128]) != 0.0;
					if (isZero == true) {
						outputStream.println(lineText);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception, Throwable {
		String filePath1 = "E:/Eclipse/Kmeans/src/CREC188_20150719.txt";
		String attributeNamesPath = "E:/Eclipse/Kmeans/src/attributeNames_no_index.txt";
		printAttributeNumber(filePath1);
		writeAttributeNames(filePath1, attributeNamesPath);
		// printNonZeroRecord(filePath1);
		// writeNonZeroRecordToTxt(filePath1,nonZeroRecordPath);
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhangqianli.outliers;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import com.mathworks.engine.MatlabEngine;

public class OutlierDetect {
	public static double[] outlier3sigma(double[] input)
			throws RserveException, REngineException, REXPMismatchException {

		double mean = StatUtils.mean(input);
		double std_dev = StatUtils.variance(input);


		double low_3_sigma = mean - 3 * std_dev;
		double up_3_sigma = mean + 3 * std_dev;
		ArrayList<Double> temp = new ArrayList<>();
		for (int i = 0; i < input.length; i++) {
			if (input[i] <= up_3_sigma && input[i] >= low_3_sigma)
				temp.add(input[i]);
		}
		double[] temp1 = new double[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			temp1[i] = temp.get(i);
		}
		return temp1;
	}

	// 这个剔除方法太严格了，很容易把数据删除空。
	// 改成matlab中的马氏距离公式
	public static ArrayList<double[]> outlierofTimeSlice(ArrayList<double[]> input) {
		ArrayList<double[]> up_low_3sigma = new ArrayList<>();
		ArrayList<double[]> checked = new ArrayList<>();

		int col = input.size();// 列数
		int row = input.get(0).length;// 行数
		double[] temp = new double[2];// 存储上下3sigma的数组
		double[] temp1 = new double[col];// 存储单个施工记录的数组，长度为矩阵列数
		double element;// 矩阵元素
		for (int i = 0; i < input.size(); i++) {
			// temp[0] = Statics.low_3_sigma(input.get(i));
			//// temp[1] = Statics.up_3_sigma(input.get(i));
			// temp[0] =
			// StatUtils.geometricMean(input.get(i))-StatUtils.variance(input.get(i));
			// temp[1] =
			// StatUtils.geometricMean(input.get(i))+StatUtils.variance(input.get(i));
			temp[0] = StatUtils.percentile(input.get(i), 0.05);

			temp[1] = StatUtils.percentile(input.get(i), 0.95);
			// 用一下分位数。
			// System.out.println("temp = " + temp[0] + "," + temp[1]);
			up_low_3sigma.add(temp.clone());
		}
		for (int i = 0; i < row; i++) {
			boolean test = true;
			boolean test1 = true;
			for (int j = 0; j < col; j++) {
				element = input.get(j)[i];
				test1 = test1 && (element >= up_low_3sigma.get(j)[0] && element <= up_low_3sigma.get(j)[1]);
				System.out.println(up_low_3sigma.get(j)[0] + "," + element + "," + up_low_3sigma.get(j)[1]);
				// System.out.println("test1 = :" + test1);
				test = test && test1;

				if (test1) {

					temp1[j] = element;
				}
			}
			System.out.println("test = " + test);
			if (test) {
				checked.add(temp1.clone());
			}
		}
		// 下面是重新生成异常点检测后的数据。
		int row1 = checked.size();
		double[] temp2 = new double[row1];
		ArrayList<double[]> result = new ArrayList<>();
		for (int i = 0; i < col; i++) {
			for (int j = 0; j < checked.size(); j++) {
				temp2[j] = checked.get(j)[i];
			}
			result.add(temp2);
		}
		return result;
	}

	public static ArrayList<double[]> outlierofTimeSlice1(ArrayList<double[]> input, MatlabEngine ml,
			double percentValue) {// 注意，percentile >0 && percentile < 100;
		ArrayList<double[]> result = null;
		int col = input.size();
		int row = input.get(0).length;
		String[] colName = new String[col];
		for (int i = 0; i < col; i++) {
			colName[i] = "col" + i;
		}
		try {
			for (int i = 0; i < col; i++) {
				ml.putVariable(colName[i], input.get(i));
			}
			for (int i = 0; i < col; i++) {
				String temp = "tbm(" + (i + 1) + ",:)=" + "col" + i + ";";
				ml.eval(temp);
			}
			ml.eval("tbm=tbm';");// 转置
			ml.eval("mahaDistance = mahal(tbm,tbm);");
			Future<double[]> futureEval = ml.getVariableAsync("mahaDistance");
			ml.eval("clear;");
			double[] mahaDist = futureEval.get();
			// System.out.println("mahaDsit length = " + mahaDist.length);
			double limit = StatUtils.percentile(mahaDist, percentValue);
			// System.out.println("limit = " + limit);
			// System.out.println("percentile of 0.1 = " + StatUtils.percentile(mahaDist,
			// 10));
			// System.out.println("percentile of 0.9 = " + StatUtils.percentile(mahaDist,
			// 90));
			double[] temp = new double[col];
			ArrayList<double[]> checked = new ArrayList<>();
			for (int i = 0; i < row; i++) {
				if (mahaDist[i] < limit) {
					for (int j = 0; j < col; j++) {
						temp[j] = input.get(j)[i];
					}
					checked.add(temp);
				}

			}
			// System.out.println("checked length = " + checked.size());
			int row1 = checked.size();
			double[] temp2 = new double[row1];
			result = new ArrayList<>();
			for (int i = 0; i < col; i++) {
				for (int j = 0; j < row1; j++) {
					temp2[j] = checked.get(j)[i];
				}
				result.add(temp2);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	public static ArrayList<double[]> mahaDistance(ArrayList<double[]> input, double percentile) {
		ArrayList<double[]> result = null;
		int col = input.size();
		int row = input.get(0).length;
		double[] mean = new double[col];

		for (int i = 0; i < col; i++) {
			mean[i] = StatUtils.mean(input.get(i));
		}
		RealMatrix cov = new Array2DRowRealMatrix(col, col);// 协方差矩阵
		for (int i = 0; i < col; i++) {
			for (int j = i; j < col; j++) {
				if (i == j) {
					cov.setEntry(i, j, StatUtils.variance(input.get(i)));
				} else {
					ArrayRealVector v1 = new ArrayRealVector(input.get(i));
					double dotProduct = v1.dotProduct(new ArrayRealVector(input.get(j)));
					double covar = dotProduct / (row - 1) + (1 - 2 * row / (row - 1)) * mean[i] * mean[j];
					cov.setEntry(i, j, covar);
					cov.setEntry(j, i, covar);
				}
			}
		}
		// return cov;
		RealMatrix mat1 = new Array2DRowRealMatrix(row, col);// 这是输入的数据
		for (int i = 0; i < col; i++) {
			mat1.setColumn(i, input.get(i));
		}
		double[] mahaDistance = new double[row];
		for (int i = 0; i < row; i++) {
			RealMatrix temp = mat1.getRowMatrix(i);
			RealMatrix temp1 = temp.transpose();
			RealMatrix mahaDistEntry = temp.multiply(cov).multiply(temp1);
			mahaDistance[i] = mahaDistEntry.getEntry(0, 0);
		}
		double limit = StatUtils.percentile(mahaDistance, percentile);
		// System.out.println("maha limit = " + limit);
		// System.out.println("percentile of 0.1 = " + StatUtils.percentile(mahaDist,
		// 10));
		// System.out.println("percentile of 0.9 = " + StatUtils.percentile(mahaDist,
		// 90));
		double[] temp = new double[col];
		ArrayList<double[]> checked = new ArrayList<>();
		for (int i = 0; i < row; i++) {
			if (mahaDistance[i] < limit) {
				for (int j = 0; j < col; j++) {
					temp[j] = input.get(j)[i];
				}
				checked.add(temp);
			}
		}
		// System.out.println("checked length = " + checked.size());
		int row1 = checked.size();
		double[] temp2 = new double[row1];
		result = new ArrayList<>();
		for (int i = 0; i < col; i++) {
			for (int j = 0; j < row1; j++) {
				temp2[j] = checked.get(j)[i];
			}
			result.add(temp2);
		}
		return result;
	}
	/**
	 * 此函数切记要进行normalize处理
	 * @param input
	 * @return
	 */
	public static double[] mahaDistance1(ArrayList<double[]> input) {
		int col = input.size();
		int row = input.get(0).length;
		//先进性归一化处理,由此可见这一步骤非常重要，不然维度较大的特征会吞没维度小的特征
		input.stream().map(it -> StatUtils.normalize(it));
		//RealMatrix mean_value = new Array2DRowRealMatrix(mean);//已经标准化了，这一步没有必要
		RealMatrix cov = new Array2DRowRealMatrix(col, col);// 协方差矩阵
		for (int i = 0; i < col; i++) {
			for (int j = i; j < col; j++) {
				if (i == j) {
					cov.setEntry(i, j, 1);
				} else {
					ArrayRealVector v1 = new ArrayRealVector(input.get(i));
					double dotProduct = v1.dotProduct(new ArrayRealVector(input.get(j)));
					double covar = dotProduct / (row - 1);
					cov.setEntry(i, j, covar);
					cov.setEntry(j, i, covar);
				}
			}
		}
		// return cov;
		RealMatrix mat1 = new Array2DRowRealMatrix(row, col);// 这是输入的数据
		for (int i = 0; i < col; i++) {
			mat1.setColumn(i, input.get(i));
		}
		double[] mahaDistance = new double[row];
		for (int i = 0; i < row; i++) {
			RealMatrix temp = mat1.getRowMatrix(i);
			RealMatrix temp1 = temp.transpose();
			RealMatrix mahaDistEntry = temp.multiply(cov).multiply(temp1);
			mahaDistance[i] = mahaDistEntry.getEntry(0, 0);
		}
		return mahaDistance;
	}
	public static double[] mahaDistance2(ArrayList<ArrayList<Double>> input) {
		int size = input.get(0).size();
		ArrayList<double[]> input1 = new ArrayList<>();
		for(int i=0;i<input.size();i++)
		{
			double[] temp = new double[size];
			for(int j=0;j<size;j++) {
				temp[j] = input.get(i).get(j);
			}
			input1.add(temp);			
		}
		return mahaDistance1(input1);
	}
	public static double[] mahaDistance3(RealMatrix input) {
		int col = input.getColumnDimension();
		int row = input.getRowDimension();
		for (int i = 0; i < col; i++) {
			double[] temp = new double[row];
			temp = StatUtils.normalize(input.getColumn(i));
			input.setColumn(i, temp);
		}
		//RealMatrix mean_value = new Array2DRowRealMatrix(mean);//已经标准化了，这一步没有必要
		Covariance variance = new Covariance(input);
		
		RealMatrix cov = variance.getCovarianceMatrix();// 协方差矩阵
		// return cov;
		double[] mahaDistance = new double[row];
		for (int i = 0; i < row; i++) {
			RealMatrix temp = input.getRowMatrix(i);
			//temp=temp.subtract(mean_value);//计算点到中心马氏距离
			RealMatrix temp1 = temp.transpose();
			RealMatrix mahaDistEntry = temp.multiply(cov).multiply(temp1);
			mahaDistance[i] = mahaDistEntry.getEntry(0, 0);
		}
		return mahaDistance;
	}
	public static void main(String[] args) {
		RealMatrix mat1 = new Array2DRowRealMatrix(5, 2);
		double[] a = { 1, 2, 3, 4, 5 };
		double[] b = { 5, 4, 3, 2, 1 };
		mat1.setColumn(0, a);
		System.out.println("column dimension =" + mat1.getColumnDimension());
		System.out.println("row dimension =" + mat1.getRowDimension());
		mat1.setColumn(1, b);
		System.out.println("column dimension =" + mat1.getColumnDimension());
		System.out.println("row dimension =" + mat1.getRowDimension());

	}
}

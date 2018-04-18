/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhangqianli.outliers;

import java.util.List;
import java.util.ArrayList;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

/**
 *
 * @author zhangqianli
 */
public class Statics {

    public static double mean(List<Double> input){
        int size = input.size();
        double sum = 0;
        for(int i=0;i<size;i++) {
        	sum+=input.get(i);
        }
        return sum/size;
    }

    public static double mean(double[] input){
    	 int size = input.length;
         double sum = 0;
         for(int i=0;i<size;i++) {
         	sum+=input[i];
         }
         return sum/size;
    }

    public static double std_dev(List<Double> input){
      double mean = mean(input);
      int size = input.size();
      double diff = 0;
      double sum2 =0;
      for(int i=0;i<size;i++) {
    	  diff = input.get(i)-mean;
    	  sum2 += Math.pow(diff, 2);
      }
      sum2 = sum2/size;
      return Math.sqrt(sum2);
    }

    public static double std_dev(double[] input){
        double mean = mean(input);
        int size = input.length;
        double diff = 0;
        double sum2 =0;
        for(int i=0;i<size;i++) {
      	  diff = input[i]-mean;
      	  sum2 += Math.pow(diff, 2);
        }
        sum2 = sum2/size;
        return Math.sqrt(sum2);
    }

    public static double low_3_sigma(List<Double> input){
        double mean = mean(input);
        double std_dev = std_dev(input);
        double low_3_sigma = mean - 3 * std_dev;
        return low_3_sigma;
    }

    public static double low_3_sigma(double[] input){
        double mean = mean(input);
        double std_dev = std_dev(input);
        double low_3_sigma = mean - 3 * std_dev;
        return low_3_sigma;
    }

    public static double up_3_sigma(List<Double> input){
        double mean = mean(input);
        double std_dev = std_dev(input);
        double up_3_sigma = mean + 3 * std_dev;
        return up_3_sigma;
    }

    public static double up_3_sigma(double[] input){
        double mean = mean(input);
        double std_dev = std_dev(input);
        double up_3_sigma = mean + 3 * std_dev;
        return up_3_sigma;
    }

    public static void main(String[] args) throws REngineException, RserveException, REXPMismatchException {
        List<Double> input = new ArrayList<>();
        double[] input1 = new double[4];
        for (int i = 0; i < 4; i++) {
            input.add(i + 1.0);
            input1[i] = i + 1;
        }
        System.out.println(input);
        System.out.println(mean(input));
        System.out.println(std_dev(input));
        System.out.println(up_3_sigma(input));
        System.out.println(low_3_sigma(input));

        System.out.println("----------------------------");
        for (int i = 0; i < input1.length; i++) {
            System.out.print(input1[i] + " ");
        }
        System.out.println(mean(input1));
        System.out.println(std_dev(input1));
        System.out.println(up_3_sigma(input1));
        System.out.println(low_3_sigma(input1));
    }
}

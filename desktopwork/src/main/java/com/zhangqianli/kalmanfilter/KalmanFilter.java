package com.zhangqianli.kalmanfilter;

import java.io.*;
import java.util.ArrayList;

public class KalmanFilter {
    /**
     *
     */
    private ArrayList<Double> input;
    private ArrayList<Double> output;
    private double predict_bias = 3;//固定值
    private double measure_bias = 5;//固定值

    public ArrayList<Double> getOutput() {
        return output;
    }

    private double covariance = 0;//更新值
    private double optimal_bias = 3;//更新值，迭代更新

    public double getPredict_bias() {
        return predict_bias;
    }

    /**
     * @param predict_bias constant prediction error of kalmanfilter
     */
    public void setPredict_bias(double predict_bias) {
        this.predict_bias = predict_bias;
    }

    public double getMeasure_bias() {
        return measure_bias;
    }

    public void setMeasure_bias(double measure_bias) {
        this.measure_bias = measure_bias;

    }

    public KalmanFilter() {
        input = new ArrayList<>();
        output = new ArrayList<>();
    }

    public KalmanFilter(ArrayList<Double> input) {
        this();
        this.input = input;
    }

    /**
     *
     */
    public void filter() {
        double previous = input.get(0);
        output.add(previous);
        double predict = previous;
        for (int i = 1; i < input.size(); i++) {
            optimal_bias = Math.sqrt(optimal_bias * optimal_bias + predict_bias * predict_bias);

            covariance = Math.pow(optimal_bias, 2) / (Math.pow(optimal_bias, 2) + Math.pow(measure_bias, 2));

            double measure = input.get(i);
            predict += covariance * (measure - predict);
            output.add(predict);
            optimal_bias *= Math.sqrt(1.0 - covariance);
        }
    }

    public static void main(String[] args) {
        try {
            File file = new File("config/KalmanFilter.txt");
            FileInputStream fStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fStream);
            BufferedReader bfReader = new BufferedReader(reader);
            ArrayList<Double> input = new ArrayList<>();
            String temp;
            while (null != (temp = bfReader.readLine())) {
                input.add(Double.parseDouble(temp));
            }
            KalmanFilter kfFiler = new KalmanFilter(input);
            kfFiler.filter();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

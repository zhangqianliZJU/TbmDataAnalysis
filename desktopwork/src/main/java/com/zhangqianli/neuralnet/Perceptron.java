package com.zhangqianli.neuralnet;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * 感知机学习算法，神经网络基础
 */
public class Perceptron {
    private Instances instances;//训练用实例
    private Instance weight;//法向量
    private double bias;//截距
    private double learnRate;//学习速度
    private int count;//最大学习次数

    public Instances getInstances() {
        return instances;
    }

    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    public double getLearnRate() {
        return learnRate;
    }

    public void setLearnRate(double learnRate) {
        this.learnRate = learnRate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Perceptron() {
        bias = 0;
        learnRate = 1;
        count = 100;
    }

    public Perceptron(Instances instances) {
        this();
        this.instances = instances;
        weight = new DenseInstance(instances.numAttributes() - 1);
        for (int i = 0; i < weight.numAttributes(); i++) {
            weight.setValue(i, 0.0);
        }
    }

    public void train() {

        int i = 0;
        boolean notStopYet = true;
        do {
            int sum = 0;
            for (int j = 0; j < weight.numAttributes(); j++) {
                sum += weight.value(j) * instances.get(i).value(j);
            }
            sum += bias;
            //这里还要有一个循环
            System.out.println("count = " + count);
            System.out.println("weight = : " + weight);
            System.out.println("bias = : " + bias);

            if (sum * instances.get(i).classValue() <= 0) {
                notStopYet = notStopYet && false;
                for (int k = 0; k < weight.numAttributes(); k++) {
                    weight.setValue(k, weight.value(k) + learnRate * instances.get(i).classValue() * instances.get(i).value(k));
                }
                bias += learnRate * instances.get(i).classValue();
            } else {
                notStopYet = notStopYet && true;
            }
            i++;
            if (i == instances.size()) {
                i = 0;
                //对全局做一个检查，判断是否退出循环
                if (true == notStopYet) {
                    System.out.println("已经找到了最合适的参数");
                    System.out.println("weight = " + weight);
                    System.out.println("bias = " + bias);
                    break;
                } else {
                    notStopYet = true;
                    System.out.println("开启下一轮迭代");
                }
            }
            count++;
            if (100 == count) {
                System.out.println("已经达到程序最大循环次数，但是算法仍然没有收敛！");
                break;
            }
        }
        while (true);
    }

    public int classify(Instance instance) {
        return -1;
    }

    public static void main(String[] args) {
        try {
            Instances instances = ConverterUtils.DataSource.read("config/perceptron.arff");
            System.out.println("instances = " + instances);
            instances.setClassIndex(instances.numAttributes() - 1);
            Perceptron perceptron = new Perceptron(instances);
            perceptron.train();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.zhangqianli.neuralnet;

import java.util.ArrayList;

public class Neuron {
    private double input = 0;
    private double output = 0;
    private ArrayList<Double> weights;//单个神经元与下一层网络的所有连接权重。

    public Neuron() {
        weights = new ArrayList<>();
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public ArrayList<Double> getWeights() {
        return weights;
    }

    public void calculateOutput() {
        output = 1.0 / (1 + Math.exp(-input));
    }
}

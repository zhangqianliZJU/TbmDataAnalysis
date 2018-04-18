package com.zhangqianli.neuralnet;

import weka.core.Instance;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private ArrayList<Neuron> neurons;
    private ArrayList<Double> errors;//用一个链表专门存储误差。
    private Layer preLayer;
    private Layer nextLayer;
    private int numOfNeurons;

    //这里进行神经网络层中的神经元的初始化工作，权重向量的初始化在别处进行。
    public Layer(int numOfNeurons) {
        neurons = new ArrayList<>();
        errors = new ArrayList<>();
        this.numOfNeurons = numOfNeurons;
        for (int i = 0; i < numOfNeurons; i++) {
            Neuron temp = new Neuron();
            neurons.add(temp);
        }
    }

    public void setPreLayer(Layer preLayer) {
        this.preLayer = preLayer;
    }

    public void setNextLayer(Layer nextLayer) {
        this.nextLayer = nextLayer;
    }

    public void updateInput(Instance instance, int numofInput) {
        if (numofInput != numOfNeurons) {
            System.out.println("输入数据格式不正确，请检查后再次输入！");
        } else {
            for (int i = 0; i < numofInput; i++) {
                neurons.get(i).setInput(instance.value(i));
                neurons.get(i).calculateOutput();
            }
        }
    }

    public int getNumOfNeurons() {
        return numOfNeurons;
    }

    public ArrayList<Neuron> getNeurons() {
        return neurons;
    }

    public void updateOutput() {
        //这里得用一个循环。
        while (null != this.nextLayer) {
            Layer nextLayer = this.nextLayer;
            int numofNeurons = nextLayer.getNumOfNeurons();
            for (int i = 0; i < numofNeurons; i++) {
                int input = 0;
                for (int j = 0; j < this.getNumOfNeurons(); j++) {
                    //计算加权输入
                    Neuron temp = this.getNeurons().get(j);
                    input += temp.getOutput() * temp.getWeights().get(i);
                }
                nextLayer.getNeurons().get(i).setInput(input);//在这里计算
                //记得添加激活函数
                nextLayer.getNeurons().get(i).calculateOutput();
            }
        }
    }

    //前向更新神经元的输入和输出
    public void forward(Instance instance, int numofInput) {
        if (numofInput != numOfNeurons) {
            System.out.println("输入数据格式不正确，请检查后再次输入！");
            return;
        } else {
            for (int i = 0; i < numofInput; i++) {
                neurons.get(i).setInput(instance.value(i));
                neurons.get(i).calculateOutput();
            }
        }
        while (null != this.nextLayer) {
            Layer nextLayer = this.nextLayer;
            int numofNeurons = nextLayer.getNumOfNeurons();
            for (int i = 0; i < numofNeurons; i++) {
                int input = 0;
                for (int j = 0; j < this.getNumOfNeurons(); j++) {
                    //计算加权输入
                    Neuron temp = this.getNeurons().get(j);
                    input += temp.getOutput() * temp.getWeights().get(i);
                }
                nextLayer.getNeurons().get(i).setInput(input);//在这里计算
                //记得添加激活函数
                nextLayer.getNeurons().get(i).calculateOutput();
            }
        }
    }

    public ArrayList<Double> getErrors() {
        return errors;
    }

    //误差反向传播
    public void backforward(Instance instance, int numOfOutput, double learnRate) {
        while (null != this.preLayer) {
            Layer preLayer = this.preLayer;
            if (null == this.nextLayer) {
                int index = 0;//前一层的神经元的索引
                for (int i = instance.numAttributes() - numOfOutput; i < instance.numAttributes(); i++) {
                    //计算error,更新输出层的权重
                    double output = this.getNeurons().get(index).getOutput();
                    double err = output - instance.value(i);
                    double err1 = err * output * (1 - output);
                    this.errors.add(err1);//计算出本层每个神经元上的误差，并用该误差来更新与该神经元每一个连接的权重。
                    ArrayList<Neuron> preLayerNeurons = preLayer.getNeurons();
                    for (int j = 0; j < preLayerNeurons.size(); j++) {
                        Neuron temp = preLayerNeurons.get(j);
                        double output1 = temp.getOutput();
                        double err2 = err1 * output1;
                        ArrayList<Double> weights = temp.getWeights();
                        double newWeight = weights.get(index) + learnRate * err2;
                        weights.set(index, newWeight);
                    }
                    index++;
                }
            } else {//更新隐含层权重
                Layer nextLayer = this.nextLayer;
                int index = 0;
                ArrayList<Double> nextLayerError = nextLayer.getErrors();
                ArrayList<Neuron> neurons = this.getNeurons();
                for (int i = 0; i < neurons.size(); i++) {
                    Neuron temp = neurons.get(i);
                    double err = 0;
                    for (int j = 0; j < nextLayerError.size(); j++) {
                        err += temp.getWeights().get(j) * nextLayerError.get(j);
                    }
                    err *= temp.getOutput() * (1 - temp.getOutput());
                    this.errors.add(err);
                    ArrayList<Neuron> preLayerNeurons = preLayer.getNeurons();
                    for (int j = 0; j < preLayerNeurons.size(); j++) {
                        Neuron temp1 = preLayerNeurons.get(j);
                        double output1 = temp1.getOutput();
                        double err1 = err * output1;
                        ArrayList<Double> weights = temp.getWeights();
                        double newWeight = weights.get(index) + learnRate * err1;
                        weights.set(index, newWeight);
                    }
                }
            }
        }
    }

    public Layer getPreLayer() {
        return preLayer;
    }

    public Layer getNextLayer() {
        return nextLayer;
    }
}

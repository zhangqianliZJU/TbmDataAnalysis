package com.zhangqianli.neuralnet;

import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * 使用方法：
 * 1 添加一个输入层
 * 2 添加若干个隐含层
 * 3 添加一个输出层
 * 4 指定样本数据接口格式，输入和输出包含在一个Instance内，使用BP网络前需要指定输入和输出的维数
 * 5 逐个添加样本，训练BP网络
 */
public class BPNeuralNet {

    private Layer inputLayer;
    private Layer outputLayer;
    private ArrayList<Layer> hiddenLayers;
    private int inputDataDim;//输入数据的维数
    private int outputDataDim;//输出数据的维数
    private double learnRate = 0.5;

    public BPNeuralNet() {
        hiddenLayers = new ArrayList<>();
    }

    public int getInputDataDim() {
        return inputDataDim;
    }

    public void setInputDataDim(int inputDataDim) {
        this.inputDataDim = inputDataDim;
    }

    public int getOutputDataDim() {
        return outputDataDim;
    }

    public void setOutputDataDim(int outputDataDim) {
        this.outputDataDim = outputDataDim;
    }

    public void addInputLayer(Layer inputLayer) {
        this.inputLayer = inputLayer;
        this.inputLayer.setPreLayer(null);//输入层的前一层永远为空，输入层的下一层需要在添加别的层时设置
    }

    public void addOutputLayer(Layer outputLayer) {
        this.outputLayer = outputLayer;
        this.outputLayer.setNextLayer(null);//与输入层相反，输出层的下一层永远为空。
        Layer lastHiddenLayer = hiddenLayers.get(hiddenLayers.size() - 1);
        lastHiddenLayer.setNextLayer(outputLayer);
        this.outputLayer.setPreLayer(lastHiddenLayer);

        //权重的初始化可以放在此处进行

        Layer currentLayer = outputLayer;
        while (null != currentLayer.getPreLayer()) {
            int sizeOfErrors = currentLayer.getNumOfNeurons();
            ArrayList<Double> errors = currentLayer.getErrors();
            for(int i=0;i<sizeOfErrors;i++){
                errors.add(0.0);
            }
            Layer preLayer = currentLayer.getPreLayer();
            ArrayList<Neuron> neurons = preLayer.getNeurons();

            int sizeOfWeight = currentLayer.getNumOfNeurons();
            for (int i = 0; i < neurons.size(); i++) {
                Neuron temp = neurons.get(i);
                for (int j = 0; j < sizeOfWeight; j++) {
                    temp.getWeights().add(Math.random() * Math.exp(1));
                }

            }
            currentLayer = currentLayer.getPreLayer();
        }
    }

    public void addHiddenLayer(Layer hiddenLayer) {
        if (0 == hiddenLayers.size()) {
            this.inputLayer.setNextLayer(hiddenLayer);
            hiddenLayer.setPreLayer(this.inputLayer);
        } else {
            Layer lastHiddenLayer = hiddenLayers.get(hiddenLayers.size() - 1);
            lastHiddenLayer.setNextLayer(hiddenLayer);
            hiddenLayer.setPreLayer(lastHiddenLayer);
        }
        hiddenLayers.add(hiddenLayer);
    }

    public void forward(Instance instance, int numofInput) {
        if (numofInput != inputLayer.getNumOfNeurons()) {
            System.out.println("输入数据格式不正确，请检查后再次输入！");
            return;
        } else {
            ArrayList<Neuron> neurons = inputLayer.getNeurons();
            for (int i = 0; i < numofInput; i++) {
                neurons.get(i).setInput(instance.value(i));
                neurons.get(i).calculateOutput();
            }
        }
        Layer currentLayer = inputLayer;
        while (null != currentLayer.getNextLayer()) {
            Layer nextLayer = currentLayer.getNextLayer();
            int numofNeurons = nextLayer.getNumOfNeurons();
            ArrayList<Neuron> nextLayerNeurons = nextLayer.getNeurons();
            ArrayList<Neuron> currentLayerNerons = currentLayer.getNeurons();
            int numOfCurrentLayerNeurons = currentLayer.getNumOfNeurons();
            for (int i = 0; i < numofNeurons; i++) {
                double input = 0;
                Neuron temp = nextLayerNeurons.get(i);
                for (int j = 0; j < numOfCurrentLayerNeurons; j++) {
                    Neuron temp1 = currentLayerNerons.get(j);
                    input += temp1.getOutput() * temp1.getWeights().get(i);
                }
                temp.setInput(input);
                temp.calculateOutput();
            }
            currentLayer = currentLayer.getNextLayer();
        }
    }

    private void updateOutputLayer(Instance instance, int numOfOutput, double learnRate) {
        int index = 0;//前一层的神经元的索引
        for (int i = (instance.numAttributes() - numOfOutput); i < instance.numAttributes(); i++) {
            System.out.println("Instance属性数目为：" + instance.numAttributes());
            System.out.println("numofOutput为：" + numOfOutput);

            //计算error,更新输出层的权重
            double output = outputLayer.getNeurons().get(index).getOutput();
            double err = output - instance.value(i);
            double err1 = err * output * (1 - output);
                outputLayer.getErrors().set(index,err1);
            index++;
        }
        ArrayList<Neuron> preLayerNeurons = outputLayer.getPreLayer().getNeurons();
        ArrayList<Double> errors = outputLayer.getErrors();
        System.out.println("输出层error大小为：" + errors.size());
        for (int i = 0; i < preLayerNeurons.size(); i++) {
            Neuron temp = preLayerNeurons.get(i);
            double output1 = temp.getOutput();
            ArrayList<Double> weights = temp.getWeights();
            for(int j=0;j< errors.size();j++){
                double err2 = errors.get(j);
                err2 *= output1;
                double newWeight = weights.get(j) + learnRate * err2;
                weights.set(j, newWeight);
//                weights.add(j, newWeight);
            }
        }
        System.out.println("能够更新输出层");
    }
//这个函数有问题，有死循环问题
    private void updateHiddenLayer(double learnRate) {
        Layer currentLayer = outputLayer.getPreLayer();
        while (null != currentLayer.getPreLayer()) {
            ArrayList<Double> nextLayerError = currentLayer.getNextLayer().getErrors();
            ArrayList<Neuron> currentLayerNeurons = currentLayer.getNeurons();
            //更新单个神经元上的误差
            for (int i = 0; i < currentLayerNeurons.size(); i++) {
                Neuron temp = currentLayerNeurons.get(i);
                double err = 0;
                for (int j = 0; j < nextLayerError.size(); j++) {
                    err += temp.getWeights().get(j) * nextLayerError.get(j);
                }
                err = err * temp.getOutput() * (1 - temp.getOutput());//这行奇葩的语句
//                currentLayer.getErrors().add(err);
                    currentLayer.getErrors().set(i, err);
            }
            ArrayList<Neuron> preLayerNeurons = currentLayer.getPreLayer().getNeurons();
            //更新与该层神经元相连接的所有权重
            ArrayList<Double> errors = currentLayer.getErrors();
            for (int i = 0; i < preLayerNeurons.size(); i++) {
                Neuron temp = preLayerNeurons.get(i);
                double output1 = temp.getOutput();
                for(int j=0;j< errors.size();j++){
                    double err2 = errors.get(j);
                    err2 *= output1;
                    ArrayList<Double> weights = temp.getWeights();
                    double newWeight = weights.get(j) + learnRate * err2;
//                    weights.set(j, newWeight);
                    weights.set(j, newWeight);
                }
            }
            currentLayer = currentLayer.getPreLayer();
        }
        System.out.println("能够更新隐含层");
    }

    public void backforward(Instance instance, int numOfOutput, double learnRate) {
        updateOutputLayer(instance, numOfOutput, learnRate);
        updateHiddenLayer(learnRate);
    }

    //通过逐个添加训练样本来训练网络
    public void train(Instance instance) {
        //网络的更新方法，下面两个方法可以合二为一。
     /*   inputLayer.updateInput(instance, inputDataDim);
        inputLayer.updateOutput();*/
        //前向更新神经元
        forward(instance, inputDataDim);
        //误差反向传播
        backforward(instance, outputDataDim, learnRate);
    }

    public void printWeights() {
        Layer currentLayer = inputLayer;
        int layer = 1;
        while (null != currentLayer) {
            ArrayList<Neuron> neurons = currentLayer.getNeurons();
            System.out.println("当前位于第" + layer + "层");
            System.out.println("该层的神经元数目为：" + currentLayer.getNumOfNeurons());
            for (Neuron e : neurons) {
                System.out.println(e.getWeights());
            }
            currentLayer = currentLayer.getNextLayer();
            layer++;
        }
    }
}

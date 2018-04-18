package com.zhangqianli.neuralnet;

public class BPNeuralNetTest {
     public static void main(String[] args){
         BPNeuralNet neuralNet = new BPNeuralNet();
         neuralNet.addInputLayer(new Layer(2));
         neuralNet.addHiddenLayer(new Layer(4));
         neuralNet.addHiddenLayer(new Layer(4));
         neuralNet.addOutputLayer(new Layer(2));
         neuralNet.setInputDataDim(2);
         neuralNet.setOutputDataDim(2);
         neuralNet.printWeights();
       /*  try {
             Instances instances = ConverterUtils.DataSource.read("config/BPNeuralNet.arff");
             System.out.println(instances);
             for(int i=0;i<instances.size();i++){
                 Instance temp = instances.get(i);
                 neuralNet.train(temp);
             }
             neuralNet.printWeights();
         } catch (Exception e) {
             e.printStackTrace();
         }*/
     }
}

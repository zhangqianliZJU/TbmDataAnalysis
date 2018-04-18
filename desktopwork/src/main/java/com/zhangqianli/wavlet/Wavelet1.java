package com.zhangqianli.wavlet;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

public class Wavelet1 {
	MatlabEngine ml;
	public Wavelet1() throws InterruptedException, ExecutionException {
		Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
		// Get engine instance from the future result
		ml = eng.get();
	}
	public double[] denoise(double[] input)
			throws IllegalStateException, InterruptedException, ExecutionException {
//		System.out.println("Input signal length = : " + input.length);
		// Put the matrix in the MATLAB workspace
		ml.putVariableAsync("input", input);
		// Evaluate the command to search in MATLAB
		ml.eval("[thr,sorh,keepapp]=ddencmp('den','wv',input);");
		ml.eval("xd = wdencmp('gbl',input,'db4',2,thr,sorh,keepapp);");
//		ml.eval("subplot(2,2,1)");
//		ml.eval("plot(input);");
//		ml.eval("subplot(2,2,2);");
//		ml.eval("plot(xd);");

		// Get result from the workspace
		Future<double[]> futureEval = ml.getVariableAsync("xd");
		double[] output = futureEval.get();
		return output;
	}
	public ArrayList<Double> denoise(ArrayList<Double> input) 
			throws IllegalStateException, InterruptedException, ExecutionException {
		double[] input1 = new double[input.size()];
		for(int i=0;i<input.size();i++) {
			input1[i] = input.get(i);
		}
		double[] temp = denoise(input1);
		ArrayList<Double> temp1 = new ArrayList<>();
		for(int i=0;i<temp1.size();i++) {
			temp1.add(temp[i]);
		}
		return temp1;
	}
	public void close() throws EngineException {
		ml.disconnect();
	}
}

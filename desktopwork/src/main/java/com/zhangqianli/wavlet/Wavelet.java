package com.zhangqianli.wavlet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.MatlabEngine;

public class Wavelet {
	/**
	 * 一维信号DWT去噪
	 * 
	 * @param input
	 * @return
	 * @throws InterruptedException
	 * @throws IllegalStateException
	 * @throws ExecutionException
	 */
	public static double[] denoise(double[] input)
			throws IllegalStateException, InterruptedException, ExecutionException {
		// Start MATLAB asynchronously
		Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
		// Get engine instance from the future result
		MatlabEngine ml = eng.get();
		System.out.println("Input signal length = : " + input.length);
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
		ml.disconnect();
		return output;

	}
}

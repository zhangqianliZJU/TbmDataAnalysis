package com.zhangqianli.wavlet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.MatlabEngine;

public class MatlabPlot {
	public static void hist(double[] input) throws IllegalStateException, InterruptedException, ExecutionException {
		Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
		// Get engine instance from the future result
		MatlabEngine ml = eng.get();
		// Put the matrix in the MATLAB workspace
		ml.putVariableAsync("A", input);

		// Evaluate the command to search in MATLAB
		ml.eval("hist(A)");
		// Disconnect from the MATLAB session
		ml.disconnect();
	}

	public static void main() {
		try {
			// Start MATLAB asynchronously
			Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
			// Get engine instance from the future result
			MatlabEngine ml = eng.get();
			double[] input = null;
			// Put the matrix in the MATLAB workspace
			ml.putVariableAsync("A", input);

			// Evaluate the command to search in MATLAB
			ml.eval("B=A(A>5);");

			// Get result from the workspace
			Future<double[]> futureEval = ml.getVariableAsync("B");
			// Disconnect from the MATLAB session
			ml.disconnect();
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

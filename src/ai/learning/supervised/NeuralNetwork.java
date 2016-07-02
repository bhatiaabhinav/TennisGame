package ai.learning.supervised;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.NDArray;

public abstract class NeuralNetwork {
	
	MultiLayerNetwork net;
	MultiLayerConfiguration conf;
	protected int numOfInputs;
	protected int numOfOuputs;
	
	public NeuralNetwork(int numOfInputs, int numOfOutputs) {
		 this.numOfInputs = numOfInputs;
		 this.numOfOuputs = numOfOutputs;
	}
	
	public void init() {
		conf = getDeepDenseLayerNetworkConfiguration();
		 net = new MultiLayerNetwork(conf);
		 net.init();
	}
	
	public abstract MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration();
	
	public void train(double[][] inputs, double[][] targets) {
		net.fit(new NDArray(inputs), new NDArray(targets));
	}
	
	public void copyTo(NeuralNetwork targetQ) {
		targetQ.net.setParams(net.params());
	}

	public double[] getOutput(double[] input) {
		double[][] inputRows = new double[1][];
		inputRows[0] = input;
		NDArray inp = new NDArray(inputRows);
		INDArray results = net.output(inp);
		return results.data().asDouble();
	}
}

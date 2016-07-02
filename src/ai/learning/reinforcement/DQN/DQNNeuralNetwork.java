package ai.learning.reinforcement.DQN;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import ai.learning.reinforcement.HyperParameters;
import ai.learning.supervised.NeuralNetwork;

public class DQNNeuralNetwork extends NeuralNetwork {
	
	HyperParameters hyperParms;

	public DQNNeuralNetwork(int numOfInputs, int numOfOutputs,
			HyperParameters hyperparams) {
		super(numOfInputs, numOfOutputs);
		
		hyperParms = hyperparams;
	}

	@Override
	public MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration() {
		return new NeuralNetConfiguration.Builder()
        .iterations(1)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .learningRate(hyperParms.learningRate)
        .weightInit(WeightInit.NORMALIZED)
        .updater(Updater.RMSPROP).momentum(hyperParms.gradientMomentum).rmsDecay(hyperParms.squaredGradientMomentum)
        .list(3)
        .layer(0, new DenseLayer.Builder().nIn(numOfInputs).nOut(2 * numOfInputs)
                .activation("relu")
                .build())
        .layer(1, new DenseLayer.Builder().nIn(2 * numOfInputs).nOut(2 * numOfOuputs)
                .activation("relu")
                .build())
        .layer(2, new OutputLayer.Builder(LossFunction.MSE)
                .activation("identity")
                .nIn(2 * numOfOuputs).nOut(numOfOuputs).build())
        .pretrain(false).backprop(true).build();
	}

}

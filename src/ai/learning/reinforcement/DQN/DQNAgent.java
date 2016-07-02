package ai.learning.reinforcement.DQN;


import java.util.List;
import java.util.Map;

import ai.learning.reinforcement.Action;
import ai.learning.reinforcement.BaseReinforcementLearner;
import ai.learning.reinforcement.ChosenAction;
import ai.learning.reinforcement.HyperParameters;
import ai.learning.reinforcement.State;
import ai.learning.reinforcement.Utils;
import ai.learning.supervised.NeuralNetwork;
 
public class DQNAgent extends BaseReinforcementLearner {
 
	public final HyperParameters hyperParameters;
	ReplayMemory replayMemory;
	Action[] possibleActions;
	int inputStateVectorLength;
	public NeuralNetwork Q;
	NeuralNetwork targetQ;
	ChosenAction prevAction;
	Map<Action, Integer> actionIndexMap;
	double averageTDError;
 
	public DQNAgent(int inputStateVectorLength, Action[] possibleActions) {
		this(new HyperParameters(),inputStateVectorLength, possibleActions);
	}
 
	public DQNAgent(HyperParameters hyperParams, int inputStateVectorLength, Action[] possibleActions) {
		this.hyperParameters = hyperParams;
		this.possibleActions = possibleActions;
		this.actionIndexMap = Utils.createActionIndexMap(possibleActions);
		this.inputStateVectorLength = inputStateVectorLength;
	}
	
	@Override
	protected void onInitialize() {
		replayMemory = new ReplayMemory(hyperParameters.replayMemorySize);
		Q = new DQNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		targetQ = new DQNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		Q.init();
		targetQ.init();
		Q.copyTo(targetQ);
	}

	@Override
	public ChosenAction act() {
		if (!hyperParameters.shouldActionRepeat(getEpisodeFrameIndex())) {
			prevAction = Utils.chooseActionEpsilonGreedy_withoutExpectedReturn(getExplorationProbability(), Q, getCurrentState(), possibleActions);
		}
		return prevAction;
	}

	@Override
	protected void onFrameEnd(State prevState, ChosenAction prevAction, double reward) {
		replayMemory.addExperience(new Experience(prevState, actionIndexMap.get(prevAction.action), getCurrentState(), reward));
 
		if (getFrameIndex() >= hyperParameters.replayStartSize && (getFrameIndex() % hyperParameters.updateFrequency == 0)) {
			//System.out.println("Learning Things");
			performGradientDescentStep();
		}
 
		if (getFrameIndex() % hyperParameters.targetNetworkUpdateFrequency == 0) {
			Q.copyTo(targetQ);
		}
	}
 

	private void performGradientDescentStep() {
		List<Experience> experiences = replayMemory.getRandomExperiences(hyperParameters.miniBatchSize);
		
		double[][] trainingInputs = new double[hyperParameters.miniBatchSize][];
		double[][] trainingTargets = new double[hyperParameters.miniBatchSize][];
		
		int i = 0;
		for(Experience e : experiences) {
			State state = e.startState;
			double qValue;
			if (e.nextState.isTerminal()) {
				qValue = e.reward;
			} else {
				ChosenAction bestActionInNextState = Utils.chooseActionGreedy(targetQ, e.nextState, possibleActions);
				qValue = e.reward + hyperParameters.discountFactor * bestActionInNextState.expectedReturn;
			}
			
			double[] inputTuple = state.getTuple();
			double[] targetTuple = Q.getOutput(inputTuple);
			double tdError = qValue - targetTuple[e.actionId];
			averageTDError += 0.5 * (Math.abs(tdError) - averageTDError);
			targetTuple[e.actionId] = qValue;
			
			trainingInputs[i] = inputTuple;
			trainingTargets[i] = targetTuple;
			
			i++;
		}
		
		Q.train(trainingInputs, trainingTargets);
	}

	@Override
	public double getExplorationProbability() {
		return hyperParameters.linearlyAnnealedExplorationProbability(getFrameIndex());
	}

	@Override
	public double getDiscountFactor() {
		return hyperParameters.discountFactor;
	}

	@Override
	public double getAverageTDError() {
		return averageTDError;
	}
	
}
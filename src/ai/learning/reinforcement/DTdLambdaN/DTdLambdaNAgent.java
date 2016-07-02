package ai.learning.reinforcement.DTdLambdaN;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ai.learning.reinforcement.Action;
import ai.learning.reinforcement.BaseReinforcementLearner;
import ai.learning.reinforcement.ChosenAction;
import ai.learning.reinforcement.HyperParameters;
import ai.learning.reinforcement.State;
import ai.learning.reinforcement.Utils;
import ai.learning.supervised.NeuralNetwork;

public class DTdLambdaNAgent extends BaseReinforcementLearner {

	protected final HyperParameters hyperParameters;
	protected final Action[] possibleActions;
	protected final Map<Action, Integer> actionIndexMap;
	protected final int inputStateVectorLength;
	
	protected Queue<EligibleStateActionPair> eligibleStates;
	protected ChosenAction prevAction;
	protected NeuralNetwork targetQ;
	public NeuralNetwork Q;
	
	final double[][] trainingInputs;
	final double[][] trainingTargets;
	int trainingBatchSize;
	double averageTDError;
	
	public DTdLambdaNAgent(int inputStateVectorLength, Action[] possibleActions) {
		this(new HyperParameters(),inputStateVectorLength, possibleActions);
	}
 
	public DTdLambdaNAgent(HyperParameters hyperParams, int inputStateVectorLength, Action[] possibleActions) {
		this.hyperParameters = hyperParams;
		this.possibleActions = possibleActions;
		this.actionIndexMap = Utils.createActionIndexMap(possibleActions);
		this.inputStateVectorLength = inputStateVectorLength;
		
		trainingInputs = new double[hyperParameters.miniBatchSize][];
		trainingTargets = new double[hyperParameters.miniBatchSize][];
	}
	
	
	@Override
	protected void onInitialize() {
		eligibleStates = new LinkedList<EligibleStateActionPair>();
		
		Q = new DTdLambdaNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		targetQ = new DTdLambdaNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		Q.init();
		targetQ.init();
		Q.copyTo(targetQ);
	}

	@Override
	protected ChosenAction act() {
		if (!hyperParameters.shouldActionRepeat(getEpisodeFrameIndex())) {
			prevAction = Utils.chooseActionEpsilonGreedy_withoutExpectedReturn(getExplorationProbability(), Q, getCurrentState(), possibleActions);
		}
		return prevAction;
	}
	
	@Override
	protected void onFrameEnd(State prevState, ChosenAction prevAction, double reward) {
		
		EligibleStateActionPair s = new EligibleStateActionPair();
		s.state = prevState;
		s.action = prevAction.action;
		s.eligibility = 1;
		s.targetReturn = prevAction.expectedReturn;
		
		eligibleStates.add(s);
		
		double expectedReturnFromPrevState;
		
		if (getCurrentState().isTerminal()) {
			expectedReturnFromPrevState = reward;
		} else {
			if (hyperParameters.onPolicy) {
				ChosenAction nextAction;
				nextAction = Utils.chooseActionEpsilonGreedy(getExplorationProbability(), targetQ, getCurrentState(), possibleActions);
				expectedReturnFromPrevState = reward + hyperParameters.discountFactor * nextAction.expectedReturn;
			} else {
				ChosenAction nextBestAction;
				nextBestAction = Utils.chooseActionGreedy(targetQ, getCurrentState(), possibleActions);
				expectedReturnFromPrevState = reward + hyperParameters.discountFactor * nextBestAction.expectedReturn;
			}
		}
		
		double tdError = expectedReturnFromPrevState - prevAction.expectedReturn;
		averageTDError += 0.5 * (Math.abs(tdError) - averageTDError);
		
		//now propagate the error backwards:
		Iterator<EligibleStateActionPair> it = eligibleStates.iterator();
		while (it.hasNext()) {
			EligibleStateActionPair esap = it.next();
			esap.targetReturn += esap.eligibility * tdError;
			esap.eligibility *= hyperParameters.lambda * hyperParameters.discountFactor;
			
			if (esap.eligibility < 0.005 || getCurrentState().isTerminal()) {
				it.remove();
				double[] inputTuple = esap.state.getTuple();
				double[] targetTuple = Q.getOutput(inputTuple);
				targetTuple[actionIndexMap.get(esap.action)] = esap.targetReturn;
				addTrainingTuple(inputTuple, targetTuple);
			}
		}
		
		if (getFrameIndex() % hyperParameters.targetNetworkUpdateFrequency == 0) {
			Q.copyTo(targetQ);
		}
		
	}
	
	private void addTrainingTuple(double[] input, double[] target) {
		trainingInputs[trainingBatchSize] = input;
		trainingTargets[trainingBatchSize] = target;
		trainingBatchSize++;
		
		if (trainingBatchSize % hyperParameters.miniBatchSize == 0) {
			Q.train(trainingInputs, trainingTargets);
			trainingBatchSize = 0;
		}
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

package ai.learning.reinforcement.DTdLambdaN;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ai.learning.reinforcement.Action;
import ai.learning.reinforcement.BaseReinforcementLearner;
import ai.learning.reinforcement.ChosenAction;
import ai.learning.reinforcement.HyperParameters;
import ai.learning.reinforcement.State;
import ai.learning.reinforcement.Utils;
import ai.learning.reinforcement.DQN.Experience;
import ai.learning.reinforcement.DQN.ReplayMemory;
import ai.learning.supervised.NeuralNetwork;

public class DTdLambdaNAgent extends BaseReinforcementLearner {

	protected final HyperParameters hyperParameters;
	protected final Action[] possibleActions;
	protected final Map<Action, Integer> actionIndexMap;
	protected final int inputStateVectorLength;
	
	
	protected Queue<EligibleStateActionPair> curEpisodeEligibleStates;
	protected HashSet<EligibleStateActionPair> trainingPendingEligibleStates;
	protected ReplayMemory replayMemory;
	double[][] trainingInputs = null;
	double[][] trainingTargets = null;
	protected ChosenAction prevAction;
	protected ChosenAction nextAction;
	protected NeuralNetwork targetQ;
	public NeuralNetwork Q;
	
	boolean trainMethodToggleFlag = true;
	//TrainingData data;
	double averageTDError;
	
	public DTdLambdaNAgent(int inputStateVectorLength, Action[] possibleActions) {
		this(new HyperParameters(),inputStateVectorLength, possibleActions);
	}
 
	public DTdLambdaNAgent(HyperParameters hyperParams, int inputStateVectorLength, Action[] possibleActions) {
		this.hyperParameters = hyperParams;
		this.possibleActions = possibleActions;
		this.actionIndexMap = Utils.createActionIndexMap(possibleActions);
		this.inputStateVectorLength = inputStateVectorLength;
	}
	
	
	@Override
	protected void onInitialize() {
		curEpisodeEligibleStates = new LinkedList<EligibleStateActionPair>();
		trainingPendingEligibleStates = new HashSet<>();
		replayMemory = new ReplayMemory(hyperParameters.replayMemorySize);
		Q = new DTdLambdaNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		targetQ = new DTdLambdaNNeuralNetwork(inputStateVectorLength, possibleActions.length, hyperParameters);
		Q.init();
		targetQ.init();
		Q.copyTo(targetQ);
	}

	@Override
	protected ChosenAction act() {
		if (nextAction == null) {
			return _act();
		} else {
			return nextAction;
		}
	}
	
	protected ChosenAction _act() {
		if (getCurrentState().isTerminal()) return null;
		
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
		s.initiallyPredictedReturn = prevAction.expectedReturn;
		s.error = 0;
		
		curEpisodeEligibleStates.add(s);
		replayMemory.addExperience(new Experience(prevState, actionIndexMap.get(prevAction.action), getCurrentState(), reward));
		
		nextAction = _act();
		
		double expectedReturnFromPrevState;
		
		if (getCurrentState().isTerminal()) {
			expectedReturnFromPrevState = reward;
		} else {
			if (hyperParameters.onPolicy) {
				expectedReturnFromPrevState = reward + hyperParameters.discountFactor * nextAction.expectedReturn;
			} else {
				ChosenAction nextBestAction;
				if (nextAction.isGreedy) {
					nextBestAction = nextAction;
				} else {
					nextBestAction = Utils.chooseActionGreedy(targetQ, getCurrentState(), possibleActions);
					if (nextAction.action == nextBestAction.action) {
						nextAction.isGreedy = true;
					}
				}
				expectedReturnFromPrevState = reward + hyperParameters.discountFactor * nextBestAction.expectedReturn;
			}
		}
		
		double tdError = expectedReturnFromPrevState - prevAction.expectedReturn;
		//clip the error:
		//if (tdError < -1) tdError = -1;
		//if (tdError > 1) tdError = 1;
		
		
		//now propagate the error backwards:
		Iterator<EligibleStateActionPair> it = curEpisodeEligibleStates.iterator();
		while (it.hasNext()) {
			EligibleStateActionPair esap = it.next();
			esap.error += esap.eligibility * tdError;
			if (!hyperParameters.onPolicy && nextAction!=null && !nextAction.isGreedy) {
				esap.eligibility = 0;
			} else {
				esap.eligibility *= hyperParameters.lambda * hyperParameters.discountFactor;
			}
			
			
			if (esap.eligibility < 0.0005 || getCurrentState().isTerminal()) {
				it.remove();
//				if (esap.error > 1) esap.error = 1;
//				if (esap.error < -1) esap.error = -1;
				esap.targetReturn = esap.initiallyPredictedReturn + esap.error;
				//System.out.println(esap.targetReturn);
				averageTDError += 0.5 * (esap.error - averageTDError);
				//data.addTrainingTuple(esap);
				trainingPendingEligibleStates.add(esap);
			}
		}
		
		if (getFrameIndex() >= hyperParameters.replayStartSize && (getFrameIndex() % hyperParameters.updateFrequency == 0)) {
			
			if (trainMethodToggleFlag) {
				if (trainingPendingEligibleStates.size() > 10 * hyperParameters.miniBatchSize) {
					EligibleStateActionPair[] trainingData = new EligibleStateActionPair[hyperParameters.miniBatchSize];
					// data.getRandomExperiences(hyperParameters.miniBatchSize,
					// trainingData);
					it = trainingPendingEligibleStates.iterator();
					for (int i = 0; i < trainingData.length; i++) {
						trainingData[i] = it.next();
						it.remove();
					}
					trainingInputs = new double[hyperParameters.miniBatchSize][];
					trainingTargets = new double[hyperParameters.miniBatchSize][];
					for (int i = 0; i < trainingData.length; i++) {
						EligibleStateActionPair esap = trainingData[i];
						double[] inputTuple = esap.state.getTuple();
						double[] targetTuple = Q.getOutput(inputTuple);
						targetTuple[actionIndexMap.get(esap.action)] = esap.targetReturn;
						trainingInputs[i] = inputTuple;
						trainingTargets[i] = targetTuple;
					}

				}

				if (trainingInputs != null)
					Q.train(trainingInputs, trainingTargets);
			} else {
				performGradientDescentStep();
			}
			
			trainMethodToggleFlag = !trainMethodToggleFlag;
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
			if (tdError > 1) tdError = 1;
			if (tdError < -1) tdError = -1;
			averageTDError += 0.5 * (tdError - averageTDError);
			targetTuple[e.actionId] += tdError;
			
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

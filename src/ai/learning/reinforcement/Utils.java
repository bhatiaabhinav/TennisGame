package ai.learning.reinforcement;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ai.learning.supervised.NeuralNetwork;

public class Utils {
	
	static Random r = new Random();
	
	public static ChosenAction chooseActionGreedy(NeuralNetwork policy, State state, Action[] possibleActions) {
		return chooseActionEpsilonGreedy(0, policy, state, possibleActions);
	}
	
	public static ChosenAction chooseActionEpsilonGreedy(double epsilon, NeuralNetwork policy, State state, Action[] possibleActions) {
		ChosenAction a = new ChosenAction();
		double[] outputQValues = policy.getOutput(state.getTuple());
		if (r.nextDouble() < epsilon) {
			int actionId = r.nextInt(possibleActions.length);
			a.action = possibleActions[actionId];
			a.expectedReturn = outputQValues[actionId];
			a.isGreedy = false;
		} else {
			int maxAt = 0;
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < outputQValues.length; i++) {
				if (outputQValues[i] > max) {
					max = outputQValues[i];
					maxAt = i;
				}
			}
			a.action = possibleActions[maxAt];
			a.expectedReturn = max;
			a.isGreedy = true;
		}
		return a;
	}
	
	public static ChosenAction chooseActionEpsilonGreedy_withoutExpectedReturn(double epsilon, NeuralNetwork policy, State state, Action[] possibleActions) {
		ChosenAction a = new ChosenAction();
		if (r.nextDouble() < epsilon) {
			a.action = randomAction(possibleActions);
			a.isGreedy = false;
		} else {
			double[] outputQValues = policy.getOutput(state.getTuple());
			int maxAt = 0;
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < outputQValues.length; i++) {
				if (outputQValues[i] > max) {
					max = outputQValues[i];
					maxAt = i;
				}
			}
			a.action = possibleActions[maxAt];
			a.expectedReturn = max;
			a.isGreedy = true;
		}
		return a;
	}
	
	public static Action randomAction(Action[] possibleActions) {
		return possibleActions[r.nextInt(possibleActions.length)];
	}
	
	public static Map<Action, Integer> createActionIndexMap(Action[] possibleActions) {
		HashMap<Action, Integer> map = new HashMap<>();
		for (int i = 0; i < possibleActions.length; i++) {
			map.put(possibleActions[i], i);
		}
		return map;
	}
	
}

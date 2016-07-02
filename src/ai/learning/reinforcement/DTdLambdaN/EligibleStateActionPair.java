package ai.learning.reinforcement.DTdLambdaN;

import ai.learning.reinforcement.Action;
import ai.learning.reinforcement.State;

public class EligibleStateActionPair {
	public State state;
	public Action action;
	public double eligibility;
	public double targetReturn;
}

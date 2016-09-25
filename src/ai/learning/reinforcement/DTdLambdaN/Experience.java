package ai.learning.reinforcement.DTdLambdaN;

import ai.learning.reinforcement.State;

public class Experience {
	public final State startState;
	public final int actionId;
	public final State nextState;
	public final double reward;
 
	public Experience(State startState, int actionId, State nextState, double reward) {
		this.startState = startState;
		this.actionId = actionId;
		this.nextState = nextState;
		this.reward = reward;
	}
}
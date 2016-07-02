package ai.learning.reinforcement;

public abstract class BaseReinforcementLearner implements ReinforcementLearner {
	
	int frameIndex;
	int episodeIndex;
	int episodeFrameIndex;
	State currentState;
	ChosenAction currentAction;
	double averageBestActionQ;
	double episodeDiscountedReturn;
	double averageEpisodeDiscountedReturn;
	double totalReward;
	
	@Override
	public void initialize() {
		episodeIndex = 0;
		frameIndex = 0;
		currentState = State.getTerminalState();
		currentAction = null;
		averageBestActionQ = 0;
		episodeDiscountedReturn = 0;
		averageEpisodeDiscountedReturn = 0;
		totalReward = 0;
		
		onInitialize();
	}

	protected abstract void onInitialize();

	@Override
	public final Action act(State currentState) {
		if (getCurrentState().isTerminal()) {
			startEpisode(currentState);
		}
		
		currentAction = act();
		
		if (currentAction.isGreedy) {
			averageBestActionQ += 0.5 * (currentAction.expectedReturn - averageBestActionQ);
		}
		
		return currentAction.action;
	}

	private void startEpisode(State episodeStartState) {
		episodeFrameIndex = 0;
		episodeDiscountedReturn = 0;
		currentState = episodeStartState;
	}
	
	protected abstract ChosenAction act();

	@Override
	public final void endFrame(State nextState, double reward) {
		episodeDiscountedReturn += Math.pow(getDiscountFactor(), episodeIndex) * reward;
		totalReward += reward;
		frameIndex++;
		episodeFrameIndex++;
		State prevState = currentState;
		ChosenAction prevAction = currentAction;
		currentState = nextState;
		currentAction = null;
		
		onFrameEnd(prevState, prevAction, reward);
	}

	protected abstract void onFrameEnd(State prevState, ChosenAction PrevAction, double reward);

	@Override
	public final void endFrameAndEpisode(double reward) {
		endFrame(State.getTerminalState(), reward);
		averageEpisodeDiscountedReturn += 0.5 * (episodeDiscountedReturn - averageEpisodeDiscountedReturn);
	}

	@Override
	public abstract double getExplorationProbability();

	@Override
	public final double getAverageValue() {
		return averageBestActionQ;
	}
	
	@Override
	public final State getCurrentState() {
		return currentState;
	}	
	
	@Override
	public final int getFrameIndex() {
		return frameIndex;
	}

	@Override
	public final int getEpisodeIndex() {
		return episodeIndex;
	}

	@Override
	public final int getEpisodeFrameIndex() {
		return episodeFrameIndex;
	}
	
	@Override
	public final double getEpisodeDiscountedReturn() {
		return episodeDiscountedReturn;
		
	}
	
	@Override
	public final double getAverageEpisodeDiscountedReturn() {
		return averageEpisodeDiscountedReturn;
	}
	
	@Override
	public double getTotalReward() {
		return totalReward;
	}
}

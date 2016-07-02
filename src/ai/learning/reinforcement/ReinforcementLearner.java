package ai.learning.reinforcement;

public interface ReinforcementLearner {
	public void initialize();
	public Action act(State currentState);
	public void endFrame(State nextState, double reward);
	public void endFrameAndEpisode(double reward);
	public double getExplorationProbability();
	public double getDiscountFactor();
	public double getAverageValue();
	public double getAverageTDError();
	public double getEpisodeDiscountedReturn();
	public double getAverageEpisodeDiscountedReturn();
	public double getTotalReward();
	public int getFrameIndex();
	public int getEpisodeIndex();
	public int getEpisodeFrameIndex();
	public State getCurrentState();
}

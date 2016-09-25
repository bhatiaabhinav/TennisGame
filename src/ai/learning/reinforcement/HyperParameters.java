package ai.learning.reinforcement;

public class HyperParameters {
	public int miniBatchSize = 32;
	public int replayMemorySize = 1000000;
	public int agentHistoryLength = 4;
	public int targetNetworkUpdateFrequency = 10000;
	public double discountFactor = 0.99;
	public int actionRepeat = 4;
	public int updateFrequency = 4;
	public double learningRate = 0.05;
	public double gradientMomentum = 0.95;
	public double squaredGradientMomentum = 0.95;
	public double minSquaredGradient = 0.01;
	public double initialExploration = 1;
	public double finalExploration = 0.1;
	public int finalExplorationFrame = 300000;
	public int replayStartSize = 50000;
	public int noOpMax = 30;
	public boolean onPolicy = false;
	public double lambda = 0.7;
	
	public double linearlyAnnealedExplorationProbability(int frameIndex) {
		if (frameIndex < replayStartSize) {
			return 1;
		} else if (frameIndex >= replayStartSize + finalExplorationFrame) {
			return finalExploration;
		} else {
			return initialExploration 
					- (initialExploration - finalExploration)
					* ((double)(frameIndex - replayStartSize) / (double)finalExplorationFrame);
		}
	}
	
	public boolean shouldActionRepeat(int episodeFrameIndex) {
		return episodeFrameIndex % actionRepeat != 0;
	}
 }
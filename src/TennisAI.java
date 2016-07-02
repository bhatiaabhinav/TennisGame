import ai.learning.reinforcement.HyperParameters;
import ai.learning.reinforcement.ReinforcementLearner;
import ai.learning.reinforcement.DQN.DQNAgent;
import ai.learning.reinforcement.DTdLambdaN.DTdLambdaNAgent;

public class TennisAI {
	public ReinforcementLearner agent;

	public void init() {
		
		HyperParameters params = new HyperParameters();
		params.finalExplorationFrame = 300000;
		params.discountFactor = 1;
		params.replayMemorySize = 100000;
		
		firstAction = true;
		
//		agent = new DQNAgent(params, TennisState.STATE_VECTOR_LENGTH,
//				TennisAction.POSSIBLE_ACTIONS);
		agent = new DTdLambdaNAgent(params, TennisState.STATE_VECTOR_LENGTH,
				TennisAction.POSSIBLE_ACTIONS);
		
		agent.initialize();
	}

	boolean firstAction = true;

	public TennisAction act(TennisState state) {

		if (!firstAction) {
			agent.endFrame(state, 0);
		}
		TennisAction action = (TennisAction) agent.act(state);
		firstAction = false;
		return action;
	}

	public void rallyEnded(boolean wonPoint) {
		agent.endFrameAndEpisode(wonPoint ? 1 : -1);
		firstAction = true;
	}
}




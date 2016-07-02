

public class AIPlayer extends Player {

	public TennisAI ai = new TennisAI();
	
	public AIPlayer() {
		ai.init();
	}
	
	@Override
	public TennisAction act() {
		return ai.act(state);
	}

	@Override
	public void RallyEnded(boolean won) {
		ai.rallyEnded(won);
	}

}

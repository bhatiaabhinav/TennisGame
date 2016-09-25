
public class SimpleAIPlayer extends Player {

	@Override
	public TennisAction act() {
		if (state.ballVy < 0) {
			if (!ballWithinReach()) {
				double moveX = state.ballWing - state.playerWing;
				HorizontalInput h = moveX > 0 ? HorizontalInput.RIGHT : HorizontalInput.LEFT;
				VerticalInput v = state.playerDepth < -1 ? VerticalInput.UP : VerticalInput.DOWN;
				return new TennisAction(h, v, ShotInput.NONE);
			} else {
				//hit opposite of opponent
				HorizontalInput h;
				if (state.oppnWing < 0) h = HorizontalInput.RIGHT;
				else h = HorizontalInput.LEFT;
				
				VerticalInput v;
				if (state.oppnDepth > 0) v = VerticalInput.UP;
				else v = VerticalInput.NONE;
				
				return new TennisAction(h, v, ShotInput.PRESSED);
			}
			
			
		} else {
			//move towards center of baseline
			HorizontalInput h = state.playerWing < 0 ? HorizontalInput.RIGHT : HorizontalInput.LEFT;
			VerticalInput v = state.playerDepth < -1 ? VerticalInput.UP : VerticalInput.DOWN;
			return new TennisAction(h, v, ShotInput.NONE);
		}
	}

	@Override
	public void RallyEnded(boolean won) {
		
	}

}

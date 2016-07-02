import ai.learning.reinforcement.State;


public class TennisState extends State {

	public static final int STATE_VECTOR_LENGTH = 13;

	public double playerDepth; // -1 = Baseline, 1 = Net
	public double playerWing; // -1 = Left, 1 = Right
	public double playerVx; // -1 = Running Left, 1 = Running Right
	public double playerVy; // -1 = Running Backwards towards baseline, 1 =
							// Running Forward towards net
	public double oppnDepth; // -1 = Net, 1 = Baseline
	public double oppnWing; // -1 = Left (His right), 1 = Right (His Left)
	public double oppnVx; // -1 = Oppn Running Left (His Right), 1 = Oppn
							// running right (His Left)
	public double oppnVy; // -1 = Oppn Running towards me (net), Oppn Running
							// away from me (his baseline)
	public double ballDepth; // -1 = My baseline, 1 = oppn baseline
	public double ballWing; // -1 = left, 1 = right
	public double ballVx; // -1 = Traveling Left, 1 = Traveling Right
	public double ballVy; // -1 = Traveling towards me, 1 = Traveling away from
							// me
	public double ballVz; // -1 = Going down (falling), 1 = Going up (Rising)

	public void setFromTuple(double[] tuple) {
		playerDepth = tuple[0];
		playerWing = tuple[1];
		playerVx = tuple[2];
		playerVy = tuple[3];
		oppnDepth = tuple[4];
		oppnWing = tuple[5];
		oppnVx = tuple[6];
		oppnVy = tuple[7];
		ballDepth = tuple[8];
		ballWing = tuple[9];
		ballVx = tuple[10];
		ballVy = tuple[11];
		ballVz = tuple[12];
	}
	
	@Override
	public double[] getTuple() {
		return new double[] { playerDepth, playerWing, playerVx, playerVy,
				oppnDepth, oppnWing, oppnVx, oppnVy, ballDepth, ballWing,
				ballVx, ballVy, ballVz };
	}
	
	@Override
	public String toString() {
		return getTuple().toString();
	}

}

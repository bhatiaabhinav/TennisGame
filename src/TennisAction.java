import ai.learning.reinforcement.Action;


public class TennisAction extends Action {

	public static final TennisAction[] POSSIBLE_ACTIONS = new TennisAction[HorizontalInput
			.values().length
			* VerticalInput.values().length
			* ShotInput.values().length];

	static {
		int index = 0;
		for (HorizontalInput x : HorizontalInput.values()) {
			for (VerticalInput y : VerticalInput.values()) {
				for (ShotInput s : ShotInput.values()) {
					POSSIBLE_ACTIONS[index++] = new TennisAction(x, y, s);
				}
			}
		}
	}

	public HorizontalInput xInput;
	public VerticalInput yInput;
	public ShotInput shotInput;

	public TennisAction(HorizontalInput x, VerticalInput y, ShotInput s) {
		this.xInput = x;
		this.yInput = y;
		this.shotInput = s;
	}

	public String toString() {
		return xInput + " " + yInput + " " + shotInput;
	}
}

enum HorizontalInput {
	LEFT(-1), NONE(0), RIGHT(1);

	int value;

	HorizontalInput(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

enum VerticalInput {
	DOWN(-1), NONE(0), UP(1);

	int value;

	VerticalInput(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

enum ShotInput {
	NONE, PRESSED
}
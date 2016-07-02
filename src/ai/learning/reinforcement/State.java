package ai.learning.reinforcement;

public abstract class State {
	private static final State TERMINAL_STATE = new TerminalState();
 
	public final boolean isTerminal() {
		return this == TERMINAL_STATE;
	}
	
	public static final State getTerminalState() {
		return TERMINAL_STATE;
	}
	
	public abstract double[] getTuple();
}

final class TerminalState extends State {

	@Override
	public double[] getTuple() {
		return null;
	}
	
}
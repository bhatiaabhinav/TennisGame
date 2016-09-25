package ai.learning.reinforcement;

public class DiscreteIntAction extends Action {
	int value;
	
	public DiscreteIntAction(int value) {
		this.value = value;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public static DiscreteIntAction[] createNewDiscreteActionSpace(int size) {
		DiscreteIntAction[] arr = new DiscreteIntAction[size];
		for (int i = 0; i < size; i++) {
			arr[i] = new DiscreteIntAction(i);
		}
		return arr;
	}
}

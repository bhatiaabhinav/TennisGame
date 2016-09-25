package ai.learning.reinforcement;

public class BoxState extends State {

	double[] tuple;
	
	public BoxState(double[] tuple) {
		this.tuple = tuple;
	}
	
	@Override
	public double[] getTuple() {
		return tuple;
	}

}

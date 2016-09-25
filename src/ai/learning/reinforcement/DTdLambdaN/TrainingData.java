package ai.learning.reinforcement.DTdLambdaN;

import java.util.Random;

public class TrainingData {

	public final int capacity;
	EligibleStateActionPair[] esaps;
	int front;
	int size;
	Random random = new Random();

	public TrainingData(int capacity) {
		this.capacity = capacity;
		esaps = new EligibleStateActionPair[capacity];
		front = 0;
		size = 0;
	}

	public void addTrainingTuple(EligibleStateActionPair esap) {
		esaps[front] = esap;
		front = (front + 1) % capacity;
		size = (size + 1) > capacity ? capacity : (size + 1);
	}

	public void getRandomExperiences(int count, EligibleStateActionPair[] trainingEsaps) {
		for (int i = 0; i < count; i++) {
			trainingEsaps[i] = esaps[random.nextInt(size < capacity ? size
					: capacity)];
		}
	}

	public void clear() {
		front = 0;
		size = 0;
	}
}

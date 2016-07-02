package ai.learning.reinforcement.DQN;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ReplayMemory {

	public final int capacity;
	Experience[] queue;
	int front;
	int size;
	Random random = new Random();

	public ReplayMemory(int capacity) {
		this.capacity = capacity;
		queue = new Experience[capacity];
		front = 0;
		size = 0;
	}

	public void addExperience(Experience e) {
		queue[front] = e;
		front = (front + 1) % capacity;
		size = (size + 1) > capacity ? capacity : (size + 1);
	}

	public List<Experience> getRandomExperiences(int count) {

		LinkedList<Experience> experiences = new LinkedList<>();
		while (experiences.size() < count) {
			experiences.add(queue[random.nextInt(size < capacity ? size
					: capacity)]);
		}
		return experiences;
	}

	public void clear() {
		front = 0;
		size = 0;
	}
}

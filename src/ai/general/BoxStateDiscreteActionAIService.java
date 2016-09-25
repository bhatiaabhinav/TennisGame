package ai.general;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import ai.learning.reinforcement.BoxState;
import ai.learning.reinforcement.DiscreteIntAction;
import ai.learning.reinforcement.ReinforcementLearner;
import ai.learning.reinforcement.DQN.DQNAgent;
import ai.learning.reinforcement.DTdLambdaN.DTdLambdaNAgent;


public class BoxStateDiscreteActionAIService {
	public static int PORT = 9876;
	public static ReinforcementLearner agent = null;
	public static String lastRecievedMsg = null;
	public static String lastSentMsg = null;
	public static Map<String, String> settings = new HashMap<>();
	public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(PORT);
		boolean exit = false;

		while (!exit) {
			byte[] receiveData = new byte[1024 * 5];
			byte[] sendData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String command = new String(receivePacket.getData()).trim();
			lastRecievedMsg = command;
			//System.out.println("RECEIVED: " + command);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String returnData = "Bad Command";
			String[] tokens = command.split(" ");

			switch (tokens[0]) {
			case "INIT": {
				String algo = tokens[1];
				int boxLength = Integer.parseInt(tokens[2]);
				int actionSpaceLength = Integer.parseInt(tokens[3]);
				if ("DQN".equalsIgnoreCase(algo)) {
					agent = new DQNAgent(boxLength, DiscreteIntAction.createNewDiscreteActionSpace(actionSpaceLength));
				} else if ("DTdLambdaN".equalsIgnoreCase(algo)) {
					agent = new DTdLambdaNAgent(boxLength, DiscreteIntAction.createNewDiscreteActionSpace(actionSpaceLength));
				}
				
				agent.initialize();
				returnData = "INITIALIZED";
				break;
			}
			case "ACT": {
				int boxLength = Integer.parseInt(tokens[1]);
				double[] stateTuple = new double[boxLength];
				for (int i = 0; i < boxLength ; i++) {
					stateTuple[i] = Double.parseDouble(tokens[2 + i]);
				}
				BoxState state = new BoxState(stateTuple);
				returnData = agent.act(state).toString();
				break;
			}
			case "ENDFRAME": {
				double reward = Double.parseDouble(tokens[1]);
				boolean episodeEnd = Boolean.parseBoolean(tokens[2]);
				int boxLength = Integer.parseInt(tokens[3]);
				double[] stateTuple = new double[boxLength];
				for (int i = 0; i < boxLength ; i++) {
					stateTuple[i] = Double.parseDouble(tokens[4 + i]);
				}
				BoxState state = new BoxState(stateTuple);
				if (episodeEnd) {
					agent.endFrameAndEpisode(reward);
				} else {
					agent.endFrame(state, reward);
				}
				returnData = "GOT IT";
				break;
			}
			case "GETSETTING": {
				returnData = settings.get(tokens[1]);
				if (returnData == null) {
					returnData = "SETTING NOT FOUND";
				}
				break;
			}
			case "EXIT": {
				returnData = "OK JI";
				agent = null;
				break;
			}
			default:
				returnData = "Bad Command";
				break;
			}

			lastSentMsg = returnData;
			sendData = returnData.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			//System.out.println("SENT: " + returnData);
		}

		serverSocket.close();
	}
}

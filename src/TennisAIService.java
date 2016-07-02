import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TennisAIService {
	public static int PORT = 9876;

	public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(PORT);
		TennisAI ai = new TennisAI();

		boolean exit = false;

		while (!exit) {
			byte[] receiveData = new byte[1024 * 5];
			byte[] sendData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String command = new String(receivePacket.getData()).trim();
			System.out.println("RECEIVED: " + command);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String returnData = "Bad Command";
			String[] tokens = command.split(" ");

			switch (tokens[0]) {
			case "INIT": {
				ai.init();
				returnData = "INITIALIZED";
				break;
			}
			case "ACT": {
				TennisState state = new TennisState();
				double[] stateTuple = new double[13];
				for (int i = 1; i <= 13; i++) {
					stateTuple[i - 1] = Double.parseDouble(tokens[i]);
				}
				state.setFromTuple(stateTuple);
				TennisAction action = ai.act(state);
				returnData = action.toString();
				break;
			}
			case "RALLY": {
				if ("WON".equals(tokens[1])) {
					returnData = "YAAY";
					ai.rallyEnded(true);
				} else {
					ai.rallyEnded(false);
					returnData = "I AM SAD";
				}
				break;
			}
			case "EXIT": {
				returnData = "OK JI";
				exit = true;
				break;
			}
			default:
				returnData = "Bad Command";
				break;
			}

			sendData = returnData.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			System.out.println("SENT: " + returnData);
		}

		serverSocket.close();
	}
}

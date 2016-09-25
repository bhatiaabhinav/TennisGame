package ai.general;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ai.learning.reinforcement.ReinforcementLearner;

/**
 * Servlet implementation class GeneralUiServlet
 */
@WebServlet("/GeneralAI")
public class GeneralUiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	Thread t = new Thread(new Runnable() {
		
		@Override
		public void run() {
			try {
				BoxStateDiscreteActionAIService.main(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	});
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeneralUiServlet() {
        super();
        t.start();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject obj = new JSONObject();
		ReinforcementLearner agent = BoxStateDiscreteActionAIService.agent;
		
		obj.put("lastReceivedMsg", BoxStateDiscreteActionAIService.lastRecievedMsg);
		obj.put("lastSentMsg", BoxStateDiscreteActionAIService.lastSentMsg);
		
		if (agent != null) {
			obj.put("frameNumber", agent.getFrameIndex());
			obj.put("averageTDError", agent.getAverageTDError());
			obj.put("averageEpisodeDiscountedReturn", agent.getAverageEpisodeDiscountedReturn());
			obj.put("episodeDiscountedReturn", agent.getEpisodeDiscountedReturn());
			obj.put("totalReward", agent.getTotalReward());
			obj.put("q", agent.getAverageValue());
			obj.put("explore", agent.getExplorationProbability());
		}
		
		String responseString = obj.toString();
		byte[] responseBytes = responseString.getBytes();

		response.setContentType("application/json");
		response.setContentLength(responseBytes.length);
		response.getOutputStream().write(responseBytes);
		response.getOutputStream().close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> map = request.getParameterMap();
		
		for (String key : map.keySet()) {
			BoxStateDiscreteActionAIService.settings.put(key, map.get(key)[0]);
			System.out.println("setting change: " + key + "=" + map.get(key)[0]);
		}
		
	}

}

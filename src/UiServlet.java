

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet implementation class UiServlet
 */
@WebServlet("/TennisGame")
public class UiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	
    /**
     * Default constructor. 
     */
    public UiServlet() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject obj = new JSONObject();
		
		Game g = Game.instance;
		
		obj.put("frameNumber", g.frameNumber);
		obj.put("gameFps", g.gameFps);
		obj.put("gameTime", g.gameTime);
		obj.put("simulationFps", g.simulationFps);
		obj.put("actualTime", g.actualTime);
		obj.put("averageRallyLength", g.averageRallyLength);
		obj.put("averageTDError", g.player1.ai.agent.getAverageTDError());
		obj.put("averageEpisodeDiscountedReturn", g.player1.ai.agent.getAverageEpisodeDiscountedReturn());
		obj.put("episodeDiscountedReturn", g.player1.ai.agent.getEpisodeDiscountedReturn());
		obj.put("totalReward", g.player1.ai.agent.getTotalReward());
		
		JSONObject player1 = new JSONObject();
		player1.put("x", g.player1.x);
		player1.put("y", g.player1.y);
		player1.put("q", ((AIPlayer)g.player1).ai.agent.getAverageValue());
		player1.put("explore", ((AIPlayer)g.player1).ai.agent.getExplorationProbability());
		obj.put("player1", player1);
		
		JSONObject player2 = new JSONObject();
		player2.put("x", g.player2.x);
		player2.put("y", g.player2.y);
		//player2.put("q", ((AIPlayer)g.player2).ai.agent.getAverageValue());
		//player2.put("explore", ((AIPlayer)g.player2).ai.agent.getExplorationProbability());
		obj.put("player2", player2);
		
		JSONObject ball = new JSONObject();
		ball.put("x", g.ball.x);
		ball.put("y", g.ball.y);
		ball.put("z", g.ball.z);
		obj.put("ball", ball);
		
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
		// TODO Auto-generated method stub
	}

}

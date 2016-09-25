import java.util.Date;
import java.util.Random;

import ai.learning.reinforcement.DQN.DQNAgent;
import ai.learning.reinforcement.DTdLambdaN.*;

public class Game {
	public AIPlayer player1;
	public Player player2;
	public Court court;
	public Ball ball;
	public Player hitter;
	public Player receiver;

	public double animationRateMultiplier = 1;
	public int equalizePlayersFrequency = 20000;
	public int frameNumber = 0;
	public double actualTime = 0;
	public double gameTime = 0;
	public int gameFps = 60;
	public int simulationFps = 0;
	public double averageRallyLength = 0;
	
	int rallyLength = 0;
	
	boolean stop = false;
	int nBounce = 0;
	Random random = new Random();

	public static Game instance = new Game();
	
	static {
		instance.StartGameLoopAsync();
	}
	
	public Game() {
		court = new Court();
		ball = new Ball();
		player1 = new AIPlayer();
		player2 = new SimpleAIPlayer();
		//((DTdLambdaNAgent)player2.ai.agent).Q = ((DTdLambdaNAgent)player1.ai.agent).Q;
		//((DQNAgent)player2.ai.agent).Q = ((DQNAgent)player1.ai.agent).Q;
		player1.ball = player2.ball = ball;
		player1.court = player2.court = court;
		player1.courtSide = 1;
		player2.courtSide = 2;
		player1.oppn = player2;
		player2.oppn = player1;
	}

	private void Initialise() {
		ball.x = court.width / 2;
		ball.y = court.length / 2;
		ball.z = court.netHeight + 1;
		ball.vz = ball.vx = 0;

		if (random.nextDouble() > 0.5) {
			hitter = player2;
			receiver = player1;
			ball.vy = -42;
		} else {
			hitter = player1;
			receiver = player2;
			ball.vy = 42;
		}

		player1.x = court.width / 2;
		player1.y = 0;
		player1.vx = player1.vy = 0;

		player2.x = court.width / 2;
		player2.y = court.length;
		player2.vx = player2.vy = 0;

		nBounce = 0;
		averageRallyLength += 0.5 * (rallyLength - averageRallyLength);
		rallyLength = 0;
	}

	public void StartGameLoopAsync() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				StartGameLoop();
			}
		});

		t.setDaemon(true);
		t.start();
	}

	public void StartGameLoop() {
		Date startTime = new Date();
		Date lastTime = new Date();
		int lastFrameNumber = 0;
		Initialise();
		player1.calculateState();
		player2.calculateState();
		stop = false;
		
		while (!stop) {
			Date curTime = new Date();
			actualTime = (curTime.getTime() - startTime.getTime()) / 1000.0;
			
			if (curTime.getTime() - lastTime.getTime() > 1000)
			{
				int curSimFps = (int)(1000.0 * (frameNumber - lastFrameNumber) / ((double)(curTime.getTime() - lastTime.getTime())));
				lastFrameNumber = frameNumber;
				lastTime = curTime;
				simulationFps = (curSimFps + simulationFps) / 2;
			}
			
			double dt = 1.0/gameFps;
			gameTime += dt;
			
			Player rallyWinner = moveTheBallAndCheckIfPointOver(dt);

			if (rallyWinner != null) {
				// Inform the players & restart rally
				if (rallyWinner == player1) {
					player1.RallyEnded(true);
					player2.RallyEnded(false);
				} else {
					player1.RallyEnded(false);
					player2.RallyEnded(true);
				}
				Initialise();
			} else {
				TennisAction player1Decision = player1.act();
				TennisAction player2Decision = player2.act();

				TennisAction receiverDecision = (receiver == player1 ? player1Decision
						: player2Decision);
				TennisAction hitterDecision = (hitter == player1 ? player1Decision
						: player2Decision);

				movePlayer(hitter, hitterDecision, dt);

				boolean playShot = receiver.ballWithinReach() && receiverDecision.shotInput == ShotInput.PRESSED;
				
				if (playShot) {
					receiver.hitBall(receiverDecision);
					Player temp = receiver;
					receiver = hitter;
					hitter = temp;
					nBounce = 0;
					rallyLength++;
				} else {
					movePlayer(receiver, receiverDecision, dt);
				}

			}

			player1.calculateState();
			player2.calculateState();

//			try {
//				Thread.sleep(0);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			frameNumber++;
		}
	}

	private Player moveTheBallAndCheckIfPointOver(double dt) {

		double oldZ = ball.z;
		double oldY = ball.y;
		double oldX = ball.x;

		ball.vz += -court.g * dt;
		ball.x += ball.vx * dt;
		ball.y += ball.vy * dt;
		ball.z += ball.vz * dt;

		// check for bounce:
		if (ball.z <= 0) {
			nBounce++;
			ball.vz = -0.8 * ball.vz;
			ball.z = -ball.z;

			if (nBounce > 1) {
				// second bounce happened. The receiver missed the shot.
				return hitter;
			} else {
				// did this bounce happen outside court or in own court?

				if (ball.x < 0 || ball.x > court.width || ball.y < 0
						|| ball.y > court.length) {
					// Hitter hits out of court
					return receiver;
				}

				if (hitter.courtSide == 1 && ball.y < court.length / 2
						|| hitter.courtSide == 2 && ball.y > court.length / 2) {
					// Hitter hits to own court
					return receiver;
				}
			}
		}

		// check for net:
		if (oldZ < court.netHeight && ball.z < court.netHeight
				&& (oldY - court.length / 2) * (ball.y - court.length / 2) <= 0
				&& oldX >= 0 && ball.x >= 0 && oldX <= court.width
				&& ball.x <= court.width) {
			ball.vy = 0;
			ball.vx = 0;
			ball.vz = 0;
			return receiver;
		}

		return null;

	}

	public void StopGameLoop() {
		stop = true;
	}

	private void movePlayerTowards(Player player, double targetX,
			double targetY, double dt) {
		double relativeX = targetX - player.x;
		double relativeY = targetY - player.y;
		double targetXSpeed = targetX == player.x ? 0 : (player.maxXSpeed * relativeX/Math.abs(relativeX));
		double targetYSpeed = targetY == player.y ? 0 : (player.maxYSpeed * relativeY/Math.abs(relativeY));
		
		player.vx += (targetXSpeed- player.vx) * dt;
		player.vy += (targetYSpeed - player.vy) * dt;

		if (player == player1) {
			// player 1 position update:
			player1.x += player1.vx * dt;
			player1.y += player1.vy * dt;

			if (player1.x <= -court.extraSide) {
				player1.x = -court.extraSide;
				player1.vx = 0;
			} else if (player1.x >= court.width + court.extraSide) {
				player1.x = court.width + court.extraSide;
				player1.vx = 0;
			}
			if (player1.y <= -court.extraSide) {
				player1.y = -court.extraSide;
				player1.vy = 0;
			} else if (player1.y >= court.length / 2) {
				player1.y = court.length / 2;
				player1.vy = 0;
			}
		} else {
			// player 2 position update:
			player2.x += player2.vx * dt;
			player2.y += player2.vy * dt;
			if (player2.x <= -court.extraSide) {
				player2.x = -court.extraSide;
				player2.vx = 0;
			} else if (player2.x >= court.width + court.extraSide) {
				player2.x = court.width + court.extraSide;
				player2.vx = 0;
			}
			if (player2.y >= court.length + court.extraSide) {
				player2.y = court.length + court.extraSide;
				player2.vy = 0;
			} else if (player2.y <= court.length / 2) {
				player2.y = court.length / 2;
				player2.vy = 0;
			}
		}
	}

	private void movePlayer(Player player, TennisAction decision, double dt) {

		if (player == player1) {

			player1.vx += (player1.maxXSpeed * decision.xInput.value - player1.vx)
					* dt;
			player1.vy += (player1.maxYSpeed * decision.yInput.value - player1.vy)
					* dt;

			// player 1 position update:
			player1.x += player1.vx * dt;
			player1.y += player1.vy * dt;

			if (player1.x <= -court.extraSide) {
				player1.x = -court.extraSide;
				player1.vx = 0;
			} else if (player1.x >= court.width + court.extraSide) {
				player1.x = court.width + court.extraSide;
				player1.vx = 0;
			}
			if (player1.y <= -court.extraSide) {
				player1.y = -court.extraSide;
				player1.vy = 0;
			} else if (player1.y >= court.length / 2) {
				player1.y = court.length / 2;
				player1.vy = 0;
			}
		} else {
			player2.vx += (player2.maxXSpeed * (-decision.xInput.value) - player2.vx)
					* dt;
			player2.vy += (player2.maxYSpeed * (-decision.yInput.value) - player2.vy)
					* dt;

			// player 2 position update:
			player2.x += player2.vx * dt;
			player2.y += player2.vy * dt;
			if (player2.x <= -court.extraSide) {
				player2.x = -court.extraSide;
				player2.vx = 0;
			} else if (player2.x >= court.width + court.extraSide) {
				player2.x = court.width + court.extraSide;
				player2.vx = 0;
			}
			if (player2.y >= court.length + court.extraSide) {
				player2.y = court.length + court.extraSide;
				player2.vy = 0;
			} else if (player2.y <= court.length / 2) {
				player2.y = court.length / 2;
				player2.vy = 0;
			}
		}
	}
}

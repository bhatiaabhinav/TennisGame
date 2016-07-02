import java.util.Random;


public abstract class Player {
	public final double maxXSpeed = 30;
	public final double maxYSpeed = 30;
	
	public final double xReach = 4;
	public final double yReach = 3;
	public final double zReach = 8;
	
	public final double shotSelectionTime = 1;
	
	public Ball ball;
	public Court court;
	public Player oppn;
	
	public double x;
    public double y;
    public double vx;
    public double vy;
    
    public double shotComfort;
    public int courtSide; //1 or 2
    public double maxAngleDeviation = 10;
    public double maxSpeedPercentageDeviation = 10;
    private double shotIntensity;
    private double shotSelectionX;
    private double shotSelectionY;
	
    Random random = new Random();
    
    TennisState state = new TennisState();
    
    public final void calculateState() {
    	double halfCourtLength = court.length / 2;
    	if (courtSide == 1) {
    		state.playerDepth = 2 * (y/halfCourtLength) - 1;
    		state.playerWing = 2 * (x/court.width) - 1;
    		state.playerVx = vx/maxXSpeed;
    		state.playerVy = vy/maxYSpeed;
    		state.oppnDepth = 2 * ((oppn.y - halfCourtLength)/(halfCourtLength)) - 1;
    		state.oppnWing = 2 * (oppn.x/court.width) - 1;
    		state.oppnVx = oppn.vx/oppn.maxXSpeed;
    		state.oppnVy = oppn.vy/oppn.maxYSpeed;
    		state.ballDepth = 2 * (ball.y / court.length) - 1;
    		state.ballWing = 2 * (ball.x/court.width) - 1;
    		state.ballVx = ball.vx / ball.maxXSpeed;
    		state.ballVy = ball.vy / ball.maxYSpeed;
    		state.ballVz = ball.vz / ball.maxZSpeed;
    	} else {
    		state.playerDepth = 2 * (1 - ((y-halfCourtLength)/halfCourtLength)) - 1;
    		state.playerWing = 2 * ((court.width - x)/court.width) - 1;
    		state.playerVx = -vx/maxXSpeed;
    		state.playerVy = -vy/maxYSpeed;
    		state.oppnDepth = 2 * ((halfCourtLength-oppn.y)/halfCourtLength) - 1;
    		state.oppnWing = 2 * ((court.width - oppn.x)/court.width) - 1;
    		state.oppnVx = -oppn.vx/oppn.maxXSpeed;
    		state.oppnVy = -oppn.vy/oppn.maxYSpeed;
    		state.ballDepth = 2 * ((court.length - ball.y)/court.length) - 1;
    		state.ballWing = 2 * ((court.width - ball.x)/court.width) - 1;
    		state.ballVx = -ball.vx / ball.maxXSpeed;
    		state.ballVy = - ball.vy / ball.maxYSpeed;
    		state.ballVz = ball.vz / ball.maxZSpeed;
    	}
    }
    
    public final void resetShotSelectionAndIntensity() {
    	shotIntensity = 0;
    	shotSelectionX = 0;
    	shotSelectionY = 0;
    }
    
    public final void reinforceShotSelection(int selectionX, int selectionY, double dt) {
    	shotSelectionX += (selectionX - shotSelectionX) * 2* dt;
    	shotSelectionY += (selectionY - shotSelectionY) * 2*dt;
    }
    
    public final void reinforceShotIntensity(double dt) {
    	shotIntensity += (1 - shotIntensity) * dt;
    }
    
    public final boolean ballReachingSoon() {
    	return Math.abs((ball.y - y)/(ball.vy-vy)) < shotSelectionTime;
    }
    
    public final boolean ballWithinReach() {
    	return Math.abs(ball.y - y) < yReach && Math.abs(ball.x - x) < xReach && ball.z < zReach;
    }
    
    public final void hitBall() {
    	double targetX;
    	double targetY;
    	
    	if (courtSide == 1) {
    		targetX = ((shotSelectionX + 1) / 2) * court.width;
    		targetY = ((shotSelectionY + 1) / 2) * court.length/2 + court.length/2;
    	} else {
    		targetX = ((-shotSelectionX + 1) / 2) * court.width;
    		targetY = ((-shotSelectionY + 1) / 2) * court.length / 2;
    	}
    	
    	double yRange = Math.abs(targetY - ball.y);
    	//double xRange = Math.sqrt(targetX - ball.x);
    	//double range = Math.sqrt(xRange * xRange + yRange * yRange);
    	double clearanceInOppnCourt = Math.abs(targetY - court.length/2);
    	
    	double minTargetVz;
    	double maxHeight = 20;
    	double maxTargetVz = Math.sqrt(2*court.g * (maxHeight - ball.z));
    	
    	if (court.netHeight > ball.z) {
    		//find minVz such that height at highest point is greater than net height
    		minTargetVz = Math.sqrt(2 * court.g * (court.netHeight - ball.z));
    	} else {
    		//we can hit downwards. find the max speed with which this can be done
    		//we must hit slower than had we opportunity to hit a smash at the target
    		double zIfStraightLineShot = court.netHeight * yRange / clearanceInOppnCourt;
    		minTargetVz = - court.g * zIfStraightLineShot / Math.sqrt(2*(court.g + zIfStraightLineShot));
    	}
    	
    	//choose a more flat shot if shot intensity is higher.
    	double vz = minTargetVz + (1-shotIntensity) * (maxTargetVz - minTargetVz);
    	
    	double timeOfFlight = (2*vz + Math.sqrt(4*vz*vz+8*ball.z))/(2*court.g);
    	
    	double vx = (targetX - ball.x)/timeOfFlight;
    	double vy = (targetY - ball.y)/timeOfFlight;
    	
    	ball.vx = vx;
    	ball.vy = vy;
    	ball.vz = vz;
    }
    
    public abstract TennisAction act();
    
    public abstract void RallyEnded(boolean won);
}
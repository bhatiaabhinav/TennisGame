class BaseReinforcementLearningAgent:

	def __init__(self):
		self.__frameIndex = 0
		self.__episodeIndex = 0;
		self.__episodeFrameIndex = 0;
		self.__currentState = None;
		self.__isCurrentStateTerminal = False;
		self.__currentAction = {'action': None, 'isGreedy': False, 'expectedReturn': 0};
		self.__averageBestActionValue = 0.0;
		self.__episodeDiscountedReturn = 0.0;
		self.__averageEpisodeDiscoutedReturn = 0.0;
		self.__totalReward = 0.0;

		self._onInitialize();

	def _onInitialize(self):
		pass

	def reset(self, startState):
		self.__startEpisode(startState);

	def act(self):
		self.__currentAction = self._act();
		if (self.__currentAction['isGreedy']) :
			self.__averageBestActionValue += 0.5 * (self.__currentAction.expectedReturn - self.__averageBestActionValue);

		return self.__currentAction.action;

	def _act(self) :
		pass

	def __startEpisode(self, startState) :
		self.__episodeFrameIndex = 0;
		self.__episodeDiscountedReturn = 0;
		self.__currentState = startState;
		self.__isCurrentStateTerminal = False;

	def collectReward(self, newState, reward, episodeEnded) :
		self.__episodeDiscountedReturn += (self.getDicountFactor() ** self.__episodeFrameIndex) * reward;
		self.__totalReward += reward;
		self.__frameIndex += 1;
		self.__episodeFrameIndex += 1;
		prevState = self.__currentState;
		prevAction = self.__currentAction;
		self.__currentState = newState;

		if (episodeEnded) :
			self.__isCurrentStateTerminal = True;
			self.__averageEpisodeDiscoutedReturn += 0.5 * (self.__episodeDiscountedReturn - self.__averageEpisodeDiscoutedReturn);
		else :
			self.__isCurrentStateTerminal = False;

		self._onFrameEnd(prevState, prevAction, reward);



	def _onFrameEnd(self, prevState, prevAction, reward):
		pass

	def getExplorationProbability(self):
		pass

	def getAverageValue(self):
		return self.__averageBestActionValue;

	def getCurrentState(self):
		return self.__currentState;

	def getFrameIndex(self):
		return self.__frameIndex;

	def getEpisodeIndex(self):
		return self.__episodeIndex;

	def getEpisodeFrameIndex(self):
		return self.__episodeFrameIndex;

	def getEpisodeDiscountedReturn(self):
		return self.__episodeDiscountedReturn;

	def getAverageEpisodeDiscountedReturn(self):
		return self.__averageEpisodeDiscoutedReturn;

	def getTotalReward(self):
		return self.__totalReward;

import BaseReinforcementLearningAgent;

class DQNAgent(BaseReinforcementLearningAgent):

	def __init__(self, hyperParams, actionSpace, observationSpace):
		super(BaseReinforcementLearningAgent, self).__init__();
		self.hyperParams = hyperParams;
		self.__replayMemory = [];
		self.actionSpace = actionSpace;
		self.observationSpace = observationSpace;

	def _onInitialize(self):
		self.__replayMemory = [];
		#initialise neural networks here

	def _act(self):
		if (self.getEpisodeFrameIndex() % self.hyperParams['actionRepeat'] != 0) :
			

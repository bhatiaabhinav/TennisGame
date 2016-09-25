genHyperParams = {
	'miniBatchSize' : 32,
	'replayMemorySize': 1000000,
	'agentHistoryLength': 4,
	'targetNetworkUpdateFrequency': 10000,
	'discountFactor': 0.99,
	'actionRepeat' : 4,
	'updateFrequency' : 4,
	'learningRate' : 0.1,
	'gradientMomentum' : 0.95,
	'squaredGradientMomentum' : 0.95,
	'minSquaredGradient' : 0.01,
	'initialExploration': 1,
	'finalExploration' : 0.1,
	'finalExplorationFrame': 300000,
	'replayStartSize': 50000,
	'noOpMax': 30,
	'onPolicy': False,
	'lambda' : 0.7
}

settings = {
	'render': True
}
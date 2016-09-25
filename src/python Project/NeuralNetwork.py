class NeuralNetwork:

	def __init__(self, numOfInputs, numOfOutputs, conf):
		self.numOfInputs = numOfInputs
		self.numOfOutputs = numOfOutputs
		self.conf = conf

	def reset(self):
		
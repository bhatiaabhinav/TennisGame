import gym
import socket
import sys

render = False

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server_address = ('localhost', 9876)
algo = 'DQN'
algo = 'DTdLambdaN'


def takeAction(env, obs) :
	boxLength = env.observation_space.shape[0]
	msg = "ACT " + str(boxLength) + " " + " ".join(map(str, obs))
	sock.sendto(msg.encode(), server_address)
	data, server = sock.recvfrom(4096)
	stringdata = data.decode()
	#print('Agent says: ' + stringdata)
	return int(stringdata)

def initializeAgent(env) :
	boxLength = env.observation_space.shape[0]
	actionSpaceLength = env.action_space.n
	msg = 'INIT ' + algo + ' ' + str(boxLength) + " " + str(actionSpaceLength)
	sock.sendto(msg.encode(), server_address)
	data, server = sock.recvfrom(4096)
	stringdata = data.decode()
	print('Agent says: ' + stringdata)
	return stringdata == 'INITIALIZED'

def endFrame(env, obs, reward, done) :
	boxLength = env.observation_space.shape[0]
	msg = "ENDFRAME " + str(reward) + " " + str(done) + " " + str(boxLength) + " " + " ".join(map(str, obs))
	sock.sendto(msg.encode(), server_address)
	data, server = sock.recvfrom(4096)
	stringdata = data.decode()
	#print('Agent says: ' + stringdata)


def exitAgent() :
	msg = "EXIT"
	sock.sendto(msg.encode(), server_address)
	data, server = sock.recvfrom(4096)
	stringdata = data.decode()
	print('Agent says: ' + stringdata)

def shouldRender() :
	msg = 'GETSETTING render'
	sock.sendto(msg.encode(), server_address)
	data, server = sock.recvfrom(4096)
	stringdata = data.decode()
	return stringdata=='true'

env = gym.make('CartPole-v0')
#env = gym.make('MountainCar-v0')
#env = gym.make('Tennis-v0')
#env = gym.make('MsPacman-v0')
#env = gym.make('Breakout-v0')
#env = gym.make('Pendulum-v0')

if initializeAgent(env) :
	obs = env.reset()
	render = True
	for t in range(10000000):

		if t % 100 == 0 :
			if shouldRender():
				render = True
			else:
				render = False
		if render:
			env.render()
		action = takeAction(env, obs)
		obs, reward, done, info = env.step(action) # take a random action
		endFrame(env, obs, reward, done)
		if done:
			obs = env.reset()

	exitAgent()


else :
	print('Could not initialize agent')



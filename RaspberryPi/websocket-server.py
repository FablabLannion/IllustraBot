#!/usr/bin/python

import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import re
import math
import socket
import spi

return_text = 1
botname = 'testSPI'
sourceMaxX = 465
sourceMaxY = 780
config = {		'drawbot': {
				'type': 'dev',
				'arduinoDev': '/dev/ttyUSB0',
				'arduinoDevSpeed': 115200,
				'sizeX': 50,
				'sizeY': 30,
				'inverseAxes': 0,
				'trapezeFactor': 0,
				'floor': 1,
				'moveToLenght': 1,
				'extraLenght': 5,
				},
			'reprap': {
				'type': 'dev',
				'arduinoDev': '/dev/ttyUSB0',
				'arduinoDevSpeed': 115200,
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 0,
				'trapezeFactor': 0,
				'floor': 0,
				'moveToLenght': 0,
				},
			'lightbot': {
				'type': 'socket',
				'socketIP': '127.0.0.1',
				'socketPort': 9999,
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 1,
				'trapezeFactor': 2,
				'floor': 0,
				'moveToLenght': 0,
				},
			'test': {
				'type': 'socket',
				'socketIP': '127.0.0.1',
				'socketPort': 9999,
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 0,
				'trapezeFactor': 0,
				'floor': 0,
				'moveToLenght': 0,
				},
			'testFile': {
				'type': 'file',
				'file': '/tmp/gcode',
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 0,
				'trapezeFactor': 0,
				'floor': 0,
				'moveToLenght': 0,
				},
			'testSPI': {
				'type': 'spi',
				'sizeX': 500,
				'sizeY': 700,
				'inverseAxes': 0,
				'trapezeFactor': 0,
				'floor': 1,
				'moveToLenght': 0,
				},
		}

class GCodeHGandler():
	# Parse the raw GCode and do some stuff before sending to robot
	def parseRaw(self, rawLine):
		self.raw = rawLine
		matchObj = re.match( r'.*g0x(.*)y(.*)z(.*)', rawLine, re.M|re.I)
		self.x = float(matchObj.group(1))
		self.y = float(matchObj.group(2))
		self.z = float(matchObj.group(3))
		self.scale()
		if config[botname]['inverseAxes']:
			self.inverseAxes()
		if config[botname]['floor']:
			self.floor()
		self.prepareGCode()
		self.debugGCode()
			
	# Prepare the GCode depending on the target (DrawBot or Reprap)
	def prepareGCode(self):
		self.gcode = 'G0X' + str(self.x) + 'Y'+ str(self.y) + 'Z' + str(self.z) + "\n"
	
	# inverse axes
	def inverseAxes(self):
		tmp = self.z
		self.z = self.y
		self.y = tmp

	# Floor values
	def floor(self):
		self.x = int(math.floor(self.x))
		self.y = int(math.floor(self.y))
		self.z = int(math.floor(self.z))
	
	# Prepare DrawBot length
	def toDrawBotLength(self):
		self.l1 = math.sqrt(math.pow(self.x,2)+math.pow(self.y,2)) + config[botname]['extraLenght']
		self.l2 = math.sqrt(math.pow(config[botname]['sizeX']-self.x,2)+math.pow(self.y,2)) + config[botname]['extraLenght']

	# Scale the gcode to the target
	def scale(self):
		# Scale
		self.x = self.x*config[botname]['sizeX']/sourceMaxX
		self.y = self.y*config[botname]['sizeY']/sourceMaxY
		# Keep in bounds
		if self.x > config[botname]['sizeX']:
			self.x = config[botname]['sizeX']
		if self.y > config[botname]['sizeY']:
			self.y = config[botname]['sizeY']
		#~ # Projection to trapeze
		if config[botname]['trapezeFactor']:
			self.x = (self.x-config[botname]['sizeX']/2)\
						*(1+(config[botname]['trapezeFactor']-1)*self.y/config[botname]['sizeY'])\
						+config[botname]['trapezeFactor'] * config[botname]['sizeX']/2
		self.x = round(self.x,2)
		self.y = round(self.y,2)

	# Print out
	def debugGCode(self):
		print(self.gcode)

class Robot():
	# init communication with the robot
	def init(self):
		if config[botname]['type'] == 'dev':
			self.dev = serial.Serial(config[botname]['arduinoDev'], config[botname]['arduinoDevSpeed'])
		elif config[botname]['type'] == 'file':
			self.f = open(config[botname]['file'], 'w')
		elif config[botname]['type'] == 'spi':
			spi.initialize(0,32,250000,0)
		elif config[botname]['type'] == 'socket':
			self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
			self.socket.connect((config[botname]['socketIP'],config[botname]['socketPort']))
	
	# send GCode and wait for ack
	def write(self, gcode_h):
		if config[botname]['type'] == 'dev':
			self.dev.write(gcode_h.gcode)
			self.readLine = self.dev.readline()
		elif config[botname]['type'] == 'file':
			self.f.write(gcode_h.gcode+"\n")
			self.f.flush()
			self.readLine = 'this is a file, can not read robot return'
		elif config[botname]['type'] == 'spi':
			self.readLine = "\n"
			list_c = tuple(map(ord,list(gcode_h.gcode)))
			print list_c
			spi.transfer(list_c)
		elif config[botname]['type'] == 'socket':
			self.socket.sendall(bytes(gcode_h.gcode+"\n", 'UTF-8'))
			self.readLine = self.socket.recv(1024).decode('UTF-8')
	
	# Close communication with the robot
	def close(self):
		if config[botname]['type'] == 'dev':
			self.dev.close()
		elif config[botname]['type'] == 'file':
			self.f.close()
		elif config[botname]['type'] == 'spi':
			spi.end()
		elif config[botname]['type'] == 'socket':
			self.socket.close()
	
class WSHandler(tornado.websocket.WebSocketHandler):
	# When the sebsocket is opened
	def open(self):
		print('New connection was opened')
		self.write_message("Welcome to my websocket!")
		robot.init()

	# When the websocket receive a message
	#   TODO: redirect gcode to /dev/spidev0.0 ou 0.1
	def on_message(self, message):
		print('Incoming message:', message)
		gcode_h.parseRaw(message)
		robot.write(gcode_h)
		print("I send: " + gcode_h.gcode)
		print("I get: " + robot.readLine)
		# Write GCode to TTYUSB
		if return_text:
			self.write_message("You said: " + message)
			self.write_message("I send: " + gcode_h.gcode)
			self.write_message("I get: " + robot.readLine)
	
	# When the websocket is closed
	def on_close(self):
		print('Connection was closed...')
		robot.close()

# MAIN
application = tornado.web.Application([
	(r'/ws', WSHandler),
])

if __name__ == "__main__":
	robot = Robot()
	gcode_h = GCodeHGandler()
	http_server = tornado.httpserver.HTTPServer(application)
	http_server.listen(22300)
	tornado.ioloop.IOLoop.instance().start()

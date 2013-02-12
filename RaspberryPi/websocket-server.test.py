#!/usr/bin/python

import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import re
import math
import serial

return_text = 1
botname = 'test'
sourceMaxX = 500
sourceMaxY = 700
config = { 'drawbot': {
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
				'parrotSocket': '192.168.1.100:22300',
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 1,
				'trapezeFactor': 2,
				'floor': 0,
				'moveToLenght': 0,
				},
			'test': {
				'type': 'file',
				'file': '/tmp/robot',
				'sizeX': 20,
				'sizeY': 20,
				'inverseAxes': 1,
				'trapezeFactor': 2,
				'floor': 0,
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
		if config[botname]['moveToLenght']:
			self.toDrawBotLength()
			self.gcode = 'l ' + str(self.l1) + ' '+ str(self.l2)
		else:
			self.gcode = 'G0X' + str(self.x) + 'Y'+ str(self.y) + 'Z' + str(self.z)
	
	# inverse axes
	def inverseAxes(self):
		tmp = self.z
		self.z = self.y
		self.y = tmp

	# Floor values
	def floor(self):
		if config[botname]['moveToLenght']:
			self.l1 = int(math.floor(self.l1))
			self.l2 = int(math.floor(self.l2))
	
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
		# Projection to trapeze
		if config[botname]['trapezeFactor']:
			if self.x/config[botname]['sizeY'] > 1/2:
				self.x = self.x+(config[botname]['sizeX'](config[botname]['trapezeFactor']-1)/2*(1-self.y/config[botname]['sizeY']))
			else:
				self.x = self.x+(config[botname]['sizeX'](config[botname]['trapezeFactor']-1)/2*(1+self.y/config[botname]['sizeY']))

	# Print out
	def debugGCode(self):
		print(self.gcode)

class Robot():
	def init(self):
		if config[botname]['type'] == 'dev':
			self.dev = serial.Serial(config[botname]['arduinoDev'], config[botname]['arduinoDevSpeed'])
		elif config[botname]['type'] == 'file':
			self.file = open(config[botname]['file'], 'w')
	
	def write(self, gcode):
		if config[botname]['type'] == 'dev':
			self.dev.write(gcode)
			self.readLine = self.dev.readline()
		elif config[botname]['type'] == 'file':
			self.file.write(gcode)
			self.readLine = 'this is a file, can not read robot return'
	
	def close(self):
		if config[botname]['type'] == 'dev':
			self.dev.close()
		elif config[botname]['type'] == 'file':
			self.file.close()
	
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
		robot.write(gcode_h.gcode)
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

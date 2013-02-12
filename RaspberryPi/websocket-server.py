#!/usr/bin/python

import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
import re
import math
import serial

return_text = 0
drawBot = 1
arduino_dev = '/dev/ttyUSB0'
arduino_dev_speed = 115200
source_maxX = 500
source_maxY = 700
target_maxX = 50
target_maxY = 30

class GCodeHGandler():
	# Parse the raw GCode and do some stuff before sending to robot
	def parseRaw(self, rawLine):
		self.raw = rawLine
		matchObj = re.match( r'.*g0x(.*)y(.*)z(.*)', rawLine)
		self.x = float(matchObj.group(1))
		self.y = float(matchObj.group(2))
		self.z = float(matchObj.group(3))
		self.scale()
		if drawBot:
			self.toDrawBotLength()
		self.prepareGCode()
		self.debugGCode()
			
	# Prepare the GCode depending on the target (DrawBot or Reprap)
	def prepareGCode(self):
		if drawBot:
			self.gcode = 'l ' + str(int(math.floor(self.l1))) + ' '+ str(int(math.floor(self.l2)))
		else:
			self.gcode = 'G0X' + str(self.x) + 'Y'+ str(self.y) + 'Z' + str(self.z)
	
	# Prepare DrawBot length
	def toDrawBotLength(self):
		self.l1 = math.sqrt(math.pow(self.x,2)+math.pow(self.y,2))
		self.l2 = math.sqrt(math.pow(target_maxX-self.x,2)+math.pow(self.y,2))

	# Scale the gcode to the target
	def scale(self):
		self.x = self.x*target_maxX/source_maxX
		self.y = self.y*target_maxY/source_maxY

	# Print out
	def debugGCode(self):
		print(self.gcode)


class WSHandler(tornado.websocket.WebSocketHandler):
	# When the sebsocket is opened
	def open(self):
		print('New connection was opened')
		self.write_message("Welcome to my websocket!")
		self.dev = serial.Serial(arduino_dev, arduino_dev_speed)

	# When the websocket receive a message
	#   TODO: redirect gcode to /dev/spidev0.0 ou 0.1
	def on_message(self, message):
		gcode_h.parseRaw(message)
		self.dev.write(gcode_h.gcode)
		print('Incoming message:', message)
		print("I send: " + gcode_h.gcode)
		print("I get: " + self.dev.readline())
		# Write GCode to TTYUSB
		if return_text:
			self.write_message("You said: " + message)
			self.write_message("I send: " + gcode_h.gcode)
			self.write_message("I get: " + self.dev.readline())
	
	# When the websocket is closed
	def on_close(self):
		print('Connection was closed...')
		self.dev.close

# MAIN
application = tornado.web.Application([
	(r'/ws', WSHandler),
])

if __name__ == "__main__":
	gcode_h = GCodeHGandler()
	http_server = tornado.httpserver.HTTPServer(application)
	http_server.listen(22300)
	tornado.ioloop.IOLoop.instance().start()

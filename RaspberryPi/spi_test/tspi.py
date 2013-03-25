import sys
import spi

spi.initialize(0,32,250000,0)
str=sys.argv[1]
crc = 0
for b in sys.argv[1]:
	crc += ord(b)
crc &= 0xFF
print("CRC:",crc)
list_c = tuple(map(ord,list(str))+[crc,10])
print list_c
spi.transfer(list_c)
spi.end()

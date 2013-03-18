import sys
import spi

spi.initialize(0,32,250000,0)
str=sys.argv[1] + '\n'
crc = 0
for b in sys.argv[1][-7:]:
	crc += ord(b)
crc &= 0xF
print("CRC:",crc)
list_c = tuple(map(ord,list(str))+[crc])
print list_c
spi.transfer(list_c)
spi.end()

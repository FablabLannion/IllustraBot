//
// spi.c - Low level functions for sending and recieving bytes via the spi
//

#include <avr/io.h>
#include <string.h>
#include "arduino.h"
#include "spi.h"
#include "serial.h"


void spi_initslave(void)
{

  // Configuration pin for SPI slave
  pinMode(SS, INPUT);
  pinMode(MOSI, INPUT);
  pinMode(SCK, INPUT);
  pinMode(MISO, OUTPUT);
  
  // turn on SPI in slave mode
  // MSB transmit first
  // SPI enable
  // Mode 0 (CPOL = 0 / CPHA = 0)
  SPCR |= _BV(SPE);
  
  // now turn on interrupts
  spi_attachInterrupt();
  
  ptr_rd_buffer_spi = 0;
  ptr_wr_buffer_spi = 0;
 
}  // end of setup
 
void spi_attachInterrupt(void) {
  SPCR |= _BV(SPIE);
}


//********************************************
// SPI interrupt routine
//********************************************
ISR (SPI_STC_vect)
{
	// Get character in internal buffer
    BUFFER_SPI[ptr_wr_buffer_spi] = SPDR;
	

	if(ptr_wr_buffer_spi == TAILLE_BUFFER_SPI)
	{
		ptr_wr_buffer_spi = 0;
	}
	else
	{
		ptr_wr_buffer_spi++;
	}
}
// end of interrupt routine SPI_STC_vect

 




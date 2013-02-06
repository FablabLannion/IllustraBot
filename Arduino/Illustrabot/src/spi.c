//
// spi.c - Low level functions for sending and recieving bytes via the spi
//

#include <avr/io.h>
#include <string.h>
#include "arduino.h"
#include "spi.h"

// variable
uint8_t BUFFER_SPI[TAILLE_BUFFER_SPI];
uint8_t *ptr_wr_buffer_spi = BUFFER_SPI;

void spi_initslave(void)
{
 // Configuration of UART to debug by RS232 link
 //Serial.begin (115200);

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
 
}  // end of setup
 
void spi_attachInterrupt(void) {
  SPCR |= _BV(SPIE);
}

// SPI interrupt routine
ISR (SPI_STC_vect)
{
  // add to buffer if room
 *(ptr_wr_buffer_spi++) = SPDR;// grab byte from SPI Data Register
  if((ptr_wr_buffer_spi-BUFFER_SPI) == TAILLE_BUFFER_SPI) ptr_wr_buffer_spi=BUFFER_SPI;
}
// end of interrupt routine SPI_STC_vect

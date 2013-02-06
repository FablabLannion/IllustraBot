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



/* // SPI interrupt routine
ISR (SPI_STC_vect)
{
byte c = SPDR;  // grab byte from SPI Data Register
  
  // add to buffer if room
  if (pos < sizeof buf)
    {
    buf [pos++] = c;

    // example: newline means time to process buffer
    if (c == '\n')
      process_it = true;
      
    }  // end of room available
}  // end of interrupt routine SPI_STC_vect
 
// main loop - wait for flag set in interrupt routine
void loop (void)
{
  if (process_it)
    {
    buf [pos] = 0;  
    Serial.println (buf);
    pos = 0;
    process_it = false;
    }  // end of flag set
    
}  // end of loop






byte command[10];
byte index;


// SPI interrupt routine
ISR (SPI_STC_vect)
{
  // add to buffer if room
 *(ptr_wr_buffer_spi++) = SPDR;// grab byte from SPI Data Register
  if((ptr_wr_buffer_spi-BUFFER_SPI) == TAILLE_BUFFER_SPI) ptr_wr_buffer_spi=BUFFER_SPI;
}
  // end of interrupt routine SPI_STC_vect

// main loop - wait for flag set in interrupt routine
void loop (void)
{
  
  if(ptr_rd_buffer_spi != ptr_wr_buffer_spi)
  {
    command[index++]=*(ptr_rd_buffer_spi++);
    if((ptr_rd_buffer_spi-BUFFER_SPI) == TAILLE_BUFFER_SPI) ptr_rd_buffer_spi=BUFFER_SPI;
    
    //End of command
    if(command[index]==0x0d) 
    {
      index=0;
      if (strcmp((char *)command, "fablab")==0) Serial.println ("commande connu\n");
      else Serial.println ("commande inconnu\n");
    }
  }    
}  // end of loop */
/*
  main.c - An embedded CNC Controller with rs274/ngc (g-code) support
  Part of Grbl

  Copyright (c) 2009-2011 Simen Svale Skogsrud
  Copyright (c) 2011-2012 Sungeun K. Jeon

  Grbl is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Grbl is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Grbl.  If not, see <http://www.gnu.org/licenses/>.
*/

/* A big thanks to Alden Hart of Synthetos, supplier of grblshield and TinyG, who has
   been integral throughout the development of the higher level details of Grbl, as well
   as being a consistent sounding board for the future of accessible and free CNC. */

#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include "config.h"
#include "serial.h"
#include "spi.h"

// Global variable
extern uint8_t BUFFER_SPI[];
extern uint8_t *ptr_wr_buffer_spi;
uint8_t *ptr_rd_buffer_spi = BUFFER_SPI;

system_t sys;

int main(void)
{
  // decalration of variable
  uint8_t byte_data;
  
  // Initialize system
  serial_init(); // Setup serial baud rate and interrupts
  spi_initslave(); // Setup spi rate and interrupts (slave mode)
  sei(); // Enable interrupts
  
  // Erase all system data
  memset(&sys, 0, sizeof(sys));  // Clear all system variables
  sys.abort = true;   // Set abort to complete initialization
  sys.state = STATE_INIT;  // Set alarm state to indicate unknown initial position
  
  while(1)
  {
  
	if(ptr_rd_buffer_spi != ptr_wr_buffer_spi)
	{
		byte_data=*(ptr_rd_buffer_spi++);
		if((ptr_rd_buffer_spi-BUFFER_SPI) == TAILLE_BUFFER_SPI) ptr_rd_buffer_spi=BUFFER_SPI;
		serial_write(byte_data);
		
	}    
  }
  return 0;   /* never reached */
}

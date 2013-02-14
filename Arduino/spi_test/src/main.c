/*
    14-02-2013

    
    Main to test SPI between RaspBerry / Arduino
*/
 
#include <avr/io.h>    		// including the avr IO lib
#include <util/delay.h>    	// including the avr delay lib

#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include "config.h"
#include "serial.h"
#include "arduino.h"

#include "spi.h"    // including the avr delay lib


//Define functions
//======================

// Global variable

system_t sys;

extern volatile uint8_t ptr_wr_buffer_spi;


int main (void)
{
	
	// Initialize system
	serial_init(); // Setup serial baud rate and interrupts
	spi_initslave(); // Setup spi rate and interrupts (slave mode)
	sei(); // Enable interrupts
  
	// Erase all system data
	memset(&sys, 0, sizeof(sys));  // Clear all system variables
	sys.abort = true;   // Set abort to complete initialization
	sys.state = STATE_INIT;  // Set alarm state to indicate unknown initial position
  
  
	while(1) // Do forever
    {
		if(ptr_wr_buffer_spi != ptr_rd_buffer_spi)
		{
			serial_write (BUFFER_SPI[ptr_rd_buffer_spi]);
			
			if(ptr_rd_buffer_spi == TAILLE_BUFFER_SPI)
				ptr_rd_buffer_spi = 0;
			else
				ptr_rd_buffer_spi++;	
		}
    }
   
    return(0);
}

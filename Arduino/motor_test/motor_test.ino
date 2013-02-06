/******
 * IllustraBot : 1er essai moteurs
 * 
 * 2 shield L298 utilisés:
 * - DFrobot : http://www.dfrobot.com/index.php?route=product/product&product_id=69#.URLaDOBRthF
 * - emartee : http://emartee.com/product/42001/Motor%20drive%20shield%20L298N%20V3.0
 * 
 */

#include <Stepper.h>

// motor speed in RPM
#define SPEED 150

// number of steps for one revolution
#define STEPS_PER_REVOL 200


Stepper motorRight (STEPS_PER_REVOL, 7,4); // dfrobot
Stepper motorLeft  (STEPS_PER_REVOL, 13,12,11,8); //L298

void movePen();

void setup() {
   // set enables high
   // dfrobot shield
   pinMode(5, OUTPUT);
   pinMode(6, OUTPUT);
   digitalWrite (5, HIGH);
   digitalWrite (6, HIGH);
   // L298N shield
   pinMode(9, OUTPUT);
   pinMode(10, OUTPUT);
   digitalWrite (9, HIGH);
   digitalWrite (10, HIGH);
   // set the motor speed:
   motorLeft.setSpeed(SPEED);
   motorRight.setSpeed(SPEED);
}

void loop() {
   movePen ();
   delay(500);
}

/** turn the 2 motors
 */
void movePen() {
   for (int c=0;c<100;c++) {
      motorLeft.step(-STEPS_PER_REVOL/100);
      motorRight.step(STEPS_PER_REVOL/100);
   }
} //move





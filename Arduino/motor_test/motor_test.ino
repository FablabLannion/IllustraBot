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
#define INITIAL_SPEED 100

// number of steps for one revolution
#define STEPS_PER_REVOL 200


Stepper motorRight (STEPS_PER_REVOL, 7,4); // dfrobot
Stepper motorLeft  (STEPS_PER_REVOL, 13,12,11,8); //L298

void setup() {
   // set enables high
   // dfrobot shield
   pinMode (5, OUTPUT);
   pinMode (6, OUTPUT);
   digitalWrite (5, HIGH);
   digitalWrite (6, HIGH);
   // L298N shield
   pinMode ( 9, OUTPUT);
   pinMode (10, OUTPUT);
   digitalWrite ( 9, HIGH);
   digitalWrite (10, HIGH);
   // set the motor speed:
   setSpeed (INITIAL_SPEED);
   // init serial
   Serial.begin(9600);
}

void loop() {
   char c=0;
   int p1,p2;
   
   while ( (Serial.available() > 0) && (c != '\n') )
   {
      c = Serial.read();
      switch (c)
      {
         case 'D': // move right motor (rotations)
         case 'd': // move right motor (steps)
            p1 = Serial.parseInt();
            p1 = (c=='d')? p1 : STEPS_PER_REVOL*p1;
            moveRight (p1);
            break;
         case 'G': // move left motor (rotations)
         case 'g': // move left motor (steps)
            p1 = Serial.parseInt();
            p1 = (c=='g')? p1 : STEPS_PER_REVOL*p1;
            moveLeft (p1);
            break;
         case 's': // set Motors speed
            p1 = Serial.parseInt();
            setSpeed (p1);
            break;
         case 'T': // move both motors (rotations)
         case 't': // move both motors (steps)
            p1 = Serial.parseInt();
            p2 = Serial.parseInt();
            p1 = (c=='t')? p1 : STEPS_PER_REVOL*p1;
            p2 = (c=='t')? p2 : STEPS_PER_REVOL*p2;
            movePen (p1, p2);
            break;
         case '\n':
            break;
         default:
            Serial.println("?");
            break;
      }
//       Serial.println ("");
   }
} /* loop */

/** turn the 2 motors following a line.
 * The 2 motors rotation time will be the same no matter what the parameters are.
 * @param rSteps : nb of steps for right motor
 * @param lSteps : nb of steps for left motor
 */
void movePen(int rSteps, int lSteps) {
   int max=0,  // nombre total d'iteration
       rPas=0, // nombre de steps par itération
       lPas=0; // nombre de steps par itération
   
   if (abs(rSteps) < abs(lSteps)) {
      max = abs(rSteps);
      rPas = (rSteps<0)? -1 : 1;
      lPas = lSteps / abs(rSteps);
   }
   else {
      max = abs(lSteps);
      lPas = (lSteps<0)? -1 : 1;
      rPas = rSteps / abs(lSteps);
   }
   // DEBUG: affiche le ratio
   Serial.print (rPas); Serial.print("/");Serial.println(lPas);
   for (int c=0;c<max;c++) {
      motorLeft.step(lPas);
      motorRight.step(rPas);
   }
} //movePen

/** turn the right motor
 * @param steps : number of steps
 */
void moveRight (int steps) {
   for (int c=0;c<abs(steps);c++) {
      motorRight.step((steps<0)?-1:1);
   }   
}

/** turn the left motor
 * @param steps : number of steps
 */
void moveLeft (int steps) {
   for (int c=0;c<abs(steps);c++) {
      motorLeft.step((steps<0)?-1:1);
   }   
}

/** set the 2 motors speed
 * @param sp the speed in RPM
 */
void setSpeed (int sp) {
   motorLeft.setSpeed  (sp);
   motorRight.setSpeed (sp);
}


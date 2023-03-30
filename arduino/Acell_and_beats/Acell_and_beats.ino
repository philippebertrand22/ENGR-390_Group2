hw_timer_t * sampleTimer = NULL;
portMUX_TYPE sampleTimerMux = portMUX_INITIALIZER_UNLOCKED;

#define USE_ARDUINO_INTERRUPTS true    // Set-up low-level interrupts for most acurate BPM math.

#include <Wire.h>
#include <SPI.h>
#include <SparkFunLSM9DS1.h>
#include <PulseSensorPlayground.h>     // Includes the PulseSensorPlayground Library.   

/*
    This is the interrupt service routine.
    We need to declare it after the PulseSensor Playground
    library is compiled, so that the onSampleTime
    function is known.
*/
void IRAM_ATTR onSampleTime() {
  portENTER_CRITICAL_ISR(&sampleTimerMux);
    PulseSensorPlayground::OurThis->onSampleTime();
  portEXIT_CRITICAL_ISR(&sampleTimerMux);
}


LSM9DS1 imu;

///////////////////////
// Example I2C Setup //
///////////////////////
// SDO_XM and SDO_G are both pulled high, so our addresses are:
// #define LSM9DS1_M	0x1E // Would be 0x1C if SDO_M is LOW
// #define LSM9DS1_AG	0x6B // Would be 0x6A if SDO_AG is LOW

#define PRINT_SPEED 10 // miliseconds
static unsigned long lastPrint = 0; // Keep track of print time

void printAccel();

//  Variables
const int PulseWire = 34;       // PulseSensor PURPLE WIRE connected to ANALOG PIN 0
int Threshold = 550;           // Determine which Signal to "count as a beat" and which to ignore.

PulseSensorPlayground pulseSensor;  // Creates an instance of the PulseSensorPlayground object called "pulseSensor"


void setup(){
  Serial.begin(115200);
  Wire.begin();

  if (imu.begin() == false) // with no arguments, this uses default addresses (AG:0x6B, M:0x1E) and i2c port (Wire).
  {
    Serial.println("Failed to communicate with LSM9DS1.");
    while (1);
  }

  /*
   ESP32 analogRead defaults to 13 bit resolution
   PulseSensor Playground library works with 10 bit
*/
  analogReadResolution(10);

  // Configure the PulseSensor object, by assigning our variables to it. 
  pulseSensor.analogInput(PulseWire);   
  pulseSensor.setThreshold(Threshold);   

  // Double-check the "pulseSensor" object was created and "began" seeing a signal. 
   if (pulseSensor.begin()) {
    Serial.println("We created a pulseSensor Object !");  //This prints one time at Arduino power-up,  or on Arduino reset.  
  }

  /*
    This will set up and start the timer interrupt on ESP32.
    The interrupt will occur every 2000uS or 500Hz.
*/
  sampleTimer = timerBegin(0, 80, true);                
  timerAttachInterrupt(sampleTimer, &onSampleTime, true);  
  timerAlarmWrite(sampleTimer, 2000, true);      
  timerAlarmEnable(sampleTimer);


}



void loop()
{

    imu.readAccel();
  int myBPM;
  if (pulseSensor.sawStartOfBeat()) {            // Constantly test to see if "a beat happened".
  myBPM = pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
  }

  if ((lastPrint + PRINT_SPEED) < millis())
  {
    printAccel(); // Print "A: ax, ay, az"
    Serial.println();
    Serial.print("BPM: ");                        // Print phrase "BPM: " 
    Serial.println(myBPM);                        // Print the value inside of myBPM. 

    lastPrint = millis(); // Update lastPrint time
  }
}


void printAccel()
{
  Serial.print("X: ");
  Serial.print(imu.calcAccel(imu.ax), 2);
  Serial.print(",");
  Serial.print("Y: ");
  Serial.print(imu.calcAccel(imu.ay), 2);
  Serial.print(",");
  Serial.print("Z: ");
  Serial.print(imu.calcAccel(imu.az), 2);
}

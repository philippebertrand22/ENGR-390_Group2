/*
Wiki:
### If your computer does not recognize the esp32 when you plug it in

**Install drivers**

Check if: 
Found in device manager/ports/Silicon Labs CP210x...

Otherwise:
device manager/right click on the device that wasn't recognized/update driver and manually direct it to the files I downloaded:
https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers?tab=downloads

### In Arduino IDE
* File / Preferences
* Additional boards manager URL

And add
```
https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
```

* Go to Tools > Board > Boards Manager
* Search **esp32**
* Install **esp32 by Espressif Systems**
* Go to Tools > Board > esp32 > **ESP32-WROOM-DA Module**

### Libraries

1. Tools > Manage Libraries
2. Search & install **sparkfun LSM9DS1**
3. Search & install **TinyGPSPlus**
4. Search & install **EspSoftwareSerial**
5. Search & install **ESP32_Pinoo** <--- Might cause errors
6. Search & install **firebase arduino client library esp32**
7. Search & install **PulseSensor**
*/

// Heartbeat Sensor Library
#include <PulseSensorPlayground.h>

// Accelerometer Libraries
#include <Wire.h>
#include <SparkFunLSM9DS1.h>

// GPS Libraries
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

// Firebase Libraries
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

hw_timer_t * sampleTimer = NULL;
portMUX_TYPE sampleTimerMux = portMUX_INITIALIZER_UNLOCKED;
#define USE_ARDUINO_INTERRUPTS true    // Set-up low-level interrupts for most acurate BPM math.


// Wifi Information
#define WIFI_SSID "zwaky"   // Wifi Name
#define WIFI_PASSWORD "kj19cs81" // Wifi Password

// Firebase Database API Key & URL
#define API_KEY "AIzaSyA6KZ8mN_LCGyCs7N-WdnBDtY7nfDIeuvM"                     // Firebase Project API Key
#define DATABASE_URL "https://group2-b2cc0-default-rtdb.firebaseio.com/" // FireBase Database URL

// GPIO PIN for the Pulse Heart Rate Sensor
#define PULSE_PIN 34

// GPIO Pins for the NEO-7M GPS Module
#define RX_GPIO_PIN 34
#define TX_GPIO_PIN 35

// Using software serial to read GPS coordinates
SoftwareSerial gpsSerial(RX_GPIO_PIN, TX_GPIO_PIN);

// TinyGPS object for the GPS sensor
TinyGPSPlus gps;

// Specifying the GPS Baud Rate for the software serial
static const uint32_t GPSBaud = 9600;

// SparkFunk LSM9DS1 Accelerometer object
LSM9DS1 imu;

// Firebase Data Json Object
FirebaseData fbdo;

// Firebase Authentication - Anonynous User was defined for now
FirebaseAuth auth;

// Firebase Configuration to specify URL and API Key
FirebaseConfig config;

// PulseSensor object
PulseSensorPlayground pulseSensor;

// Variables
bool signupisOk = false;

unsigned long sendDataPrevMillis = 0;

float accelX = 1;
float accelY = 1;
float accelZ = 1;
float accelgravityX;
float accelgravityY;
float accelgravityZ;

int steps = 0;
int steps_walking = 0;
bool step_toggle = true;
bool step_toggle_walking = true;

int PulseThreshold = 600;
int bpm = 0;


// PulseSensor stuff
void IRAM_ATTR onSampleTime() {
  portENTER_CRITICAL_ISR(&sampleTimerMux);
    PulseSensorPlayground::OurThis->onSampleTime();
  portEXIT_CRITICAL_ISR(&sampleTimerMux);
}

void setup() {
  Serial.begin(115200);

    /*
   ESP32 analogRead defaults to 13 bit resolution
   PulseSensor Playground library works with 10 bit
    */
  analogReadResolution(10);

  // Configure the PulseSensor
  pinMode(PULSE_PIN, INPUT); // Reading Input from IR_Sensor through GPIO 34
  pulseSensor.analogInput(PULSE_PIN); // Set which pin is the analog input
  pulseSensor.setThreshold(PulseThreshold);

  /*
    This will set up and start the timer interrupt on ESP32.
    The interrupt will occur every 2000uS or 500Hz.
  */
  sampleTimer = timerBegin(0, 80, true);                
  timerAttachInterrupt(sampleTimer, &onSampleTime, true);  
  timerAlarmWrite(sampleTimer, 2000, true);      
  timerAlarmEnable(sampleTimer);



  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while(WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(200);
  }
  Serial.println();
  Serial.print("Connecting with IP address:");
  Serial.println(WiFi.localIP());
  Serial.println();

  Wire.begin(); // Using Wire for the Accelerometer conncetions
  
  imu.settings.gyro.enabled = false; //We don't need gyroscope or mag sensor
  imu.settings.mag.enabled = false;
  imu.begin(); 

// Setup Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
 
  if(Firebase.signUp(&config, &auth, "", "")){
    Serial.println("SignUp is Ok");
    signupisOk = true; 
   } else {
    Serial.println(config.signer.signupError.message.c_str());
   }
   config.token_status_callback = tokenStatusCallback;
   Firebase.begin(&config,&auth);
   Firebase.reconnectWiFi(true); 

}

void loop(){

    if(Firebase.ready() && signupisOk && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0) ){
      stepCount();
      bpm = getPulse();

      displayPulse(bpm);
      //displaySteps();


      writePulseSensorDatatoFirebase(bpm);
      writeAccelerometerDatatoFirebase();
      }    
}

void writePulseSensorDatatoFirebase(int beats){  
  Firebase.RTDB.setInt(&fbdo, "Pulse Sensor/State", beats);
}

void writeAccelerometerDatatoFirebase(){
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Step", steps);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Step_walking", steps_walking);
}

void stepCount(){
  /*
  https://www.aosabook.org/en/500L/a-pedometer-in-the-real-world.html
  Rolling array of accel data
  3 filters:
   1. Low-pass to remove higher than 5hz jiggles
   2. High pass to remove lower than 1hz. No one walks slower than that.
   3. Have a threshold that needs to be met. (++ in the positive direction & after crossing a 0. Avoids over counting)


  Steps:
  Create an array that holds all the data
  0Hz low pass filter to split user acceleration and gravitational acceleration
  Filter the data for low pass and high pass
  Dot product user and gravitational data. Get clean sine wave
  If threshold is met, increment step
  */

  int sampleSize = 100; //Can change the sample size and delays to improve the algorithm
  float accel[sampleSize][3];
  float accel_along_gravity[sampleSize][3];
  float dot_product[sampleSize]; 
  float filtered_accel[sampleSize] = {0.0}; //Final array that will hold the data after two filters
  float filtered_accel_low_pass[sampleSize] = {0.0};

  float threshold = 0.09;
  float threshold_walking = 0.05;

  // Initialize first two rows of new array to 0;
  for (int i = 0; i <= 1 ; i++){ 
      for(int j = 0; j < 3; j++){  
      accel_along_gravity[i][j] = 0;
      }    
    }

  float coefficients_low_pass_0Hz[] = {-1.979133761292768, 0.979521463540373, 0.000086384997973502, 0.000172769995947004, 0.000086384997973502};
  float coefficients_low_pass_5Hz[] = {0.095465967120306, -0.172688631608676, 0.095465967120306, -1.80898117793047, 0.827224480562408};
  float coefficients_high_pass_1Hz[] = {0.953986986993339, -1.907503180919730, 0.953986986993339, -1.905384612118461, 0.910092542787947};

  // Get array of acceleration data
  for (int i=0; i < sampleSize; i++){

     imu.readAccel();

    accel[i][0] = (imu.calcAccel(imu.ax));
    accel[i][1] = (imu.calcAccel(imu.ay));
    accel[i][2] = (imu.calcAccel(imu.az)); 

    delay(10);
  }
 
  // Infinite Impulse Response filter
  // Filter out the gravitational acceleration and store it in new array
  for (int i = 2; i < sampleSize ; i++){ 
    for(int j = 0; j < 3; j++){ // Nested for loop for each axis
  
      accel_along_gravity[i][j] = 
      accel[i][j]                    * coefficients_low_pass_0Hz[0] +
      accel[i-1][j]                  * coefficients_low_pass_0Hz[1] +
      accel[i-2][j]                  * coefficients_low_pass_0Hz[2] -
      accel_along_gravity[i-1][j]    * coefficients_low_pass_0Hz[3] -
      accel_along_gravity[i-2][j]    * coefficients_low_pass_0Hz[4];
      }
  }

  // Combine gravity from raw acceleration to get user acceleration
  for (int i = 2; i < sampleSize ; i++){ 
    for(int j = 0; j < 3; j++){
      accel[i][j] += accel_along_gravity[i][j];     
    }
  }

  // Dot product between accel and grav-accel to get a single value that is along the gravitaional axis regardless of the sensor's orientation
  for (int i = 0; i < sampleSize ; i++)
    {         
      dot_product[i] =  
      accel[i][0] * accel_along_gravity[i][0] + 
      accel[i][1] * accel_along_gravity[i][1] + 
      accel[i][2] * accel_along_gravity[i][2];
    }
    
  // Filter low pass below 5Hz
  for (int i = 2; i < sampleSize ; i++){
    filtered_accel_low_pass[i] = (
      dot_product[i]                  * coefficients_low_pass_5Hz[0] +
      dot_product[i-1]                * coefficients_low_pass_5Hz[1] +
      dot_product[i-2]                * coefficients_low_pass_5Hz[2] -
      filtered_accel_low_pass[i-1]    * coefficients_low_pass_5Hz[3] -
      filtered_accel_low_pass[i-2]    * coefficients_low_pass_5Hz[4]
      );}

  // Second Filter now high pass above 1Hz
  for (int i = 2; i < sampleSize ; i++){
    filtered_accel[i] = (
      filtered_accel_low_pass[i]      * coefficients_high_pass_1Hz[0] +
      filtered_accel_low_pass[i-1]    * coefficients_high_pass_1Hz[1] +
      filtered_accel_low_pass[i-2]    * coefficients_high_pass_1Hz[2] -
      filtered_accel[i-1]             * coefficients_high_pass_1Hz[3] -
      filtered_accel[i-2]             * coefficients_high_pass_1Hz[4]
      );}

  // Count steps for running
  for (int i = 1; i < sampleSize; i++){

  // Only increments if crossing the threshold in the positive direction
  if ((filtered_accel[i] >= threshold) && (filtered_accel[i-1] < threshold) && (step_toggle == true) ){
    steps++;
    step_toggle = false;
  }

  // Can only reset the toggle if the acceleration crossed the 0 in the negative direction
  if ((filtered_accel[i] < 0) && (filtered_accel[i-1] >=0)) step_toggle = true;
  }

  // Count steps for walking
  for (int i = 1; i < sampleSize; i++){

  // Only increments if crossing the threshold in the positive direction
  if ((filtered_accel[i] >= threshold_walking) && (filtered_accel[i-1] < threshold_walking) && (step_toggle_walking == true) ){
    steps_walking++;
    step_toggle_walking = false;
  }

  // Can only reset the toggle if the acceleration crossed the 0 in the negative direction
  if ((filtered_accel[i] < 0) && (filtered_accel[i-1] >=0)) step_toggle_walking = true;
  }


}

int getPulse(){

if (pulseSensor.sawStartOfBeat()) {           // Constantly test to see if "a beat happened".
return pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
}
return bpm; //If there was no new beat, return the current bpm
}

void displayPulse(int beats){
  Serial.print("BPM: "); 
  Serial.println(beats); // Print the value to the serial monitor either HIGH or LOW
}

void displayGPSInfo()


{
  if (gps.location.isValid())
  {
    Serial.print("Latitude: ");
    Serial.println(gps.location.lat(), 6);
    Serial.print("Longitude: ");
    Serial.println(gps.location.lng(), 6);
    Serial.print("Altitude: ");
    Serial.println(gps.altitude.meters());
  }
  else
  {
    Serial.println();
    Serial.println("Location: Not Available");
    Serial.print("Latitude: ");
    Serial.println(gps.location.lat(), 6);
    Serial.print("Longitude: ");
    Serial.println(gps.location.lng(), 6);
    Serial.print("Altitude: ");
    Serial.println(gps.altitude.meters());
  }

  Serial.print("Date: ");
  if (gps.date.isValid())
  {
    Serial.print(gps.date.month());
    Serial.print("/");
    Serial.print(gps.date.day());
    Serial.print("/");
    Serial.println(gps.date.year());
  }
  else
  {
    Serial.println("Not Available");
  }

  Serial.print("Time: ");
  if (gps.time.isValid())
  {
    if (gps.time.hour() < 10) Serial.print(F("0"));
    Serial.print(gps.time.hour());
    Serial.print(":");
    if (gps.time.minute() < 10) Serial.print(F("0"));
    Serial.print(gps.time.minute());
    Serial.print(":");
    if (gps.time.second() < 10) Serial.print(F("0"));
    Serial.print(gps.time.second());
    Serial.print(".");
    if (gps.time.centisecond() < 10) Serial.print(F("0"));
    Serial.println(gps.time.centisecond());
  }
  else
  {
    Serial.println("Not Available");
  }
}

void displaySteps(){

  Serial.print("Steps Walking: ");   
  Serial.print(steps_walking); 
  Serial.print("\t Steps Running: ");   
  Serial.println(steps); 
}
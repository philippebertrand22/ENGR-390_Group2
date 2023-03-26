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
5. Search & install **ESP32_Pinoo**
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

// Wifi Information
#define WIFI_SSID "Helix.B590"   // Wifi Name
#define WIFI_PASSWORD "11111111" // Wifi Password

// Firebase Database API Key & URL
#define API_KEY "AIzaSyA6KZ8mN_LCGyCs7N-WdnBDtY7nfDIeuvM"                     // Firebase Project API Key
#define DATABASE_URL "https://group2-b2cc0-default-rtdb.firebaseio.com/" // FireBase Database URL

// GPIO PIN for the Pulse Heart Rate Sensor
#define PULSE_PIN 13

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
bool step_toggle = true;
int PulseThreshold = 550;





void setup() {
  Serial.begin(115200);

  gpsSerial.begin(GPSBaud); // Software serial initialisation

  // Configure the PulseSensor
  pinMode(PULSE_PIN, INPUT); // Reading Input from IR_Sensor through GPIO 13
  pulseSensor.analogInput(PULSE_PIN); // Set which pin is the analog input (default 0)
  pulseSensor.setThreshold(PulseThreshold);

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
 while (gpsSerial.available() > 0) 
   if(gps.encode(gpsSerial.read())){
    if(Firebase.ready() && signupisOk && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0) ){
      stepCount();
      int bpm = getPulse();

      displayAccelInfo();
    //displayGPSInfo();
      displayPulse(bpm);    

      writeGPSDatatoFirebase();
      writePulseSensorDatatoFirebase(bpm);
      stepCount();

      Serial.println("DATA SUCCESSFULY STORED IN JSON FILE");
      delay(100);
      } else {
           Serial.println("Error: Firebase is not ready or SignUp failed!");
      }
    }
  delay(800);

}

void writePulseSensorDatatoFirebase(int beats){  
  Firebase.RTDB.setInt(&fbdo, "Pulse Sensor/State", beats);
}

void writeGPSDatatoFirebase(){
     double lat = gps.location.lat();
     double lng = gps.location.lng();
     double alt = gps.altitude.meters();

     uint8_t month = gps.date.month();
     uint8_t day = gps.date.day();
     uint16_t year = gps.date.year();

     uint8_t hour = gps.time.hour();
     uint8_t min = gps.time.minute();
     uint8_t sec = gps.time.second();
     uint8_t centis = gps.time.centisecond();

     String date = "";
     date += String(month) + "/" + String(day) + "/" + String(year);

     String time = "";
     time +=  String(hour) + ":" + String(min) + ":" + String(sec) + ":" + String(centis);
    
     Firebase.RTDB.setDouble(&fbdo, "GPSData/Latitude", lat);
     Firebase.RTDB.setDouble(&fbdo, "GPSData/Longitude", lng);
     Firebase.RTDB.setDouble(&fbdo, "GPSData/Altitude", alt);
     Firebase.RTDB.setString(&fbdo, "GPSData/Date", date);
     Firebase.RTDB.setString(&fbdo, "GPSData/Time", time);
}

void writeAccelerometerDatatoFirebase(float* filter){

        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/x", accelX);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/y", accelY);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/z", accelZ);

        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter0", filter[0]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter1", filter[1]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter2", filter[2]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter3", filter[3]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter4", filter[4]);

        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Step", steps);


        delay(10);
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

  int sampleSize = 50; //Can change the sample size and delays to improve the algorithm
  float accel[sampleSize][3];
  float accel_along_gravity[sampleSize][3];
  float dot_product[sampleSize]; 
  float filtered_accel[sampleSize] = {0.0}; //Final array that will hold the data after two filters
  float filtered_accel_low_pass[sampleSize] = {0.0};

  float threshold = 0.09;

  // Initialize first two rows of new array to 0;
  for (int i = 0; i <= 1 ; i++){ 
      for(int j = 0; j < 3; j++){  
      accel_along_gravity[i][j] = 0;
      }    
    }

  // IIR alpha and beta coefficients from the internet
  float coefficients_low_pass_0Hz[] = {-1.979133761292768, 0.979521463540373, 0.000086384997973502, 0.000172769995947004, 0.000086384997973502};
  float coefficients_low_pass_5Hz[] = {0.095465967120306, -0.172688631608676, 0.095465967120306, -1.80898117793047, 0.827224480562408};
  float coefficients_high_pass_1Hz[] = {0.953986986993339, -1.907503180919730, 0.953986986993339, -1.905384612118461, 0.910092542787947};

  // Get array of acceleration data
  for (int i=0; i < sampleSize; i++){

     imu.readAccel();

    accel[i][0] = accelX = imu.ax;  
    accel[i][1] = accelY = imu.ay;  
    accel[i][2] = accelZ = imu.az + 1.0;  // Undoing the calibration in readAccel()

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

  // Subtract gravity from raw acceleration to get user acceleration
  for (int i = 2; i < sampleSize ; i++){ 
    for(int j = 0; j < 3; j++){
      accel[i][j] -= accel_along_gravity[i][j];     
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

  // Count steps
  for (int i = 1; i < sampleSize; i++){

  // Only increments if crossing the threshold in the positive direction
  if ((filtered_accel[i] >= threshold) && (filtered_accel[i-1] < threshold) && (step_toggle == true) ){
    steps++;
    step_toggle = false;
  }

  // Can only reset the toggle if the acceleration crossed the 0 in the negative direction
  if ((filtered_accel[i] < 0) && (filtered_accel[i-1] >=0)) step_toggle = true;
  }

  writeAccelerometerDatatoFirebase(filtered_accel);

Serial.println("Raw Acceleration pointing down");
Serial.print(
  String(dot_product[0]) + ", " +
  String(dot_product[1]) + ", " +
  String(dot_product[2]) + ", " +
  String(dot_product[3]) + ", " +
  String(dot_product[4]));


Serial.println("\nFiltered Acceleration pointing down");
Serial.print(
  String(filtered_accel[0]) + ", " +
  String(filtered_accel[1]) + ", " +
  String(filtered_accel[2]) + ", " +
  String(filtered_accel[3]) + ", " +
  String(filtered_accel[4]));
}


int getPulse(){

if (pulseSensor.sawStartOfBeat()) {           // Constantly test to see if "a beat happened".
int myBPM = pulseSensor.getBeatsPerMinute();  // Calls function on our pulseSensor object that returns BPM as an "int".
return myBPM;
}

}

void displayPulse(int beats){
  Serial.print("BPM: "); 
  Serial.println(beats); // Print the value to the serial monitor either HIGH or LOW
}

void displayAccelInfo() {
 
  Serial.println();
  Serial.println("Acceleration:");
  Serial.print("X: ");
  Serial.print(accelX);
  Serial.print("||");
  Serial.print("Y: ");
  Serial.print(accelY);
  Serial.print("||");
  Serial.print("Z: ");
  Serial.print(accelZ);
  Serial.print("||");
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
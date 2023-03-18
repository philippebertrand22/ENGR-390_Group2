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
#define API_KEY "AIzaSyCSK8XFtg3bPjTbOkvPT2JsBXwBTIRDWl8"                     // Firebase Project API Key
#define DATABASE_URL "https://fitnessdata-6cc1e-default-rtdb.firebaseio.com/" // FireBase Database URL

// GPIO PIN for the Pulse Heart Rate Sensor
#define IR_SENSOR_GPIO_Input_PIN 13

// GPIO Pins for the NEO-7M GPS Module
#define RX_GPIO_PIN 34
#define TX_GPIO_PIN 35

// Using software serial to read GPS coordinates
SoftwareSerial gpsSerial(RX_GPIO_PIN, TX_GPIO_PIN);

// TinyGPS object for the GPS sensor
TinyGPSPlus gps;

// Specifying the GPS Baud Rate for the software serial
static const uint32_t GPSBaud = 9600;

unsigned long sendDataPrevMillis = 0;

// SparkFunk LSM9DS1 Accelerometer object
LSM9DS1 imu;

// Firebase Data Json Object
FirebaseData fbdo;

// Firebase Authentication - Anonynous User was defined for now
FirebaseAuth auth;

// Firebase Configuration to specify URL and API Key
FirebaseConfig config;

bool signupisOk = false;

float accelX = 1;
float accelY = 1;
float accelZ = 1;
float accelXRaw = 1;
float accelYRaw = 1;
float accelZRaw = 1;


void setup() {
  Serial.begin(115200);

  gpsSerial.begin(GPSBaud); // Software serial initialisation

  pinMode(IR_SENSOR_GPIO_Input_PIN, INPUT); // Reading Input from IR_Sensor through GPIO 13

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
      displayAccelInfo();
      // displayGPSInfo();
      // displayPulse();
      writeGPSDatatoFirebase();
      writePulseSensorDatatoFirebase();
      stepCount();

      Serial.println("DATA SUCCESSFULY STORED IN JSON FILE");
      delay(100);
      } else {
           Serial.println("Error: Firebase is not ready or SignUp failed!");
      }
    }
  delay(800);

}

void writePulseSensorDatatoFirebase(){
  int irValue = digitalRead(IR_SENSOR_GPIO_Input_PIN); // Read the IR sensor value HIGH or LOW
  Firebase.RTDB.setInt(&fbdo, "Pulse Sensor/State", irValue);
  delay(10);
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

        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/x_grav", accelXRaw);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/y_grav", accelYRaw);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/z_grav", accelZRaw);

        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter0", filter[0]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter1", filter[1]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter2", filter[2]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter3", filter[3]);
        Firebase.RTDB.setFloat(&fbdo, "AccelerometerData/Acceleration/Filter4", filter[4]);


        delay(10);
}

void stepCount(){
  /*
  https://www.aosabook.org/en/500L/a-pedometer-in-the-real-world.html
  Rolling array of accel data
  3 filters:
   1. Low-passto remove higher than 5hz jiggles
   2. High pass to remove lower than 1hz. No one walks slower than that.
   3. Have a threshold that needs to be met. (++ in the positive direction & after crossing a 0. Avoids over counting)


  Steps:
  Create an array that holds all the data
  In that array, have an array of datapoints
  In the datapoints, have the x,y,z info

  Filter the data for low pass and high pass
  Dot product the data in the direction of gravity
  If threshold is met, increment step
  */

  int arraysize = 5;
  float accel_along_gravity[arraysize] = {0.0};
  float filtered_accel[arraysize] = {0.0}; //Final array that will hold the data after two filters
  float filtered_accel_low_pass[arraysize] = {0.0};

  // IIR alpha and beta coefficients from the internet
  float coefficients_low_pass_5Hz[] = {0.095465967120306, -0.172688631608676, 0.095465967120306, -1.80898117793047, 0.827224480562408};
  float coefficients_high_pass_1Hz[] = {0.953986986993339, -1.907503180919730, 0.953986986993339, -1.905384612118461, 0.910092542787947};

  // Get accel values along gravity axis
  for (int i = 0; i < arraysize ; i++)
  {
    // Get the accel data
    imu.readAccel();

    // accel data with the gravity's accel removed
    accelX = imu.ax;
    accelY = imu.ay;
    accelZ = imu.az;

    // accel data of just gravity
    accelXRaw = imu.aBiasRaw[X_AXIS];
    accelYRaw = imu.aBiasRaw[Y_AXIS];
    accelZRaw = imu.aBiasRaw[Z_AXIS];

    // Dot product of accel and gravity accel
    // Calculates the user-acceleration in the direction of gravity regardless of the sensor's orientation
    float dot_product =
        accelX * accelXRaw + accelY * accelYRaw + accelZ * accelZRaw;

    accel_along_gravity[i] = dot_product;

    delay(10);
  }

  // Infinite Impulse Response filter
  // Filter low pass below 5Hz
  for (int i = 2; i < arraysize ; i++){
    filtered_accel_low_pass[i] = (
      accel_along_gravity[i]          * coefficients_low_pass_5Hz[0] +
      accel_along_gravity[i-1]        * coefficients_low_pass_5Hz[1] +
      accel_along_gravity[i-2]        * coefficients_low_pass_5Hz[2] -
      filtered_accel_low_pass[i-1]    * coefficients_low_pass_5Hz[3] -
      filtered_accel_low_pass[i-2]    * coefficients_low_pass_5Hz[4]
      );}

  // Second Filter now high pass above 1Hz
  for (int i = 2; i < arraysize ; i++){
    filtered_accel[i] = (
      filtered_accel_low_pass[i]      * coefficients_high_pass_1Hz[0] +
      filtered_accel_low_pass[i-1]    * coefficients_high_pass_1Hz[1] +
      filtered_accel_low_pass[i-2]    * coefficients_high_pass_1Hz[2] -
      filtered_accel[i-1]             * coefficients_high_pass_1Hz[3] -
      filtered_accel[i-2]             * coefficients_high_pass_1Hz[4]
      );}


  writeAccelerometerDatatoFirebase(filtered_accel);  

}

// void displayPulse(){
//   int irValue = digitalRead(IR_SENSOR_PIN); // Read the IR sensor value
//   Serial.print("Pulse detected:"); 
//   Serial.println(irValue); // Print the value to the serial monitor either HIGH or LOW
//   delay(1000);
// }

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

  Serial.println();
  Serial.println("Acceleration along gravity:");
  Serial.print("X: ");
  Serial.print(accelXRaw);
  Serial.print("||");
  Serial.print("Y: ");
  Serial.print(accelYRaw);
  Serial.print("||");
  Serial.print("Z: ");
  Serial.print(accelZRaw);
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


// Accelerometer Libraries
#include <Wire.h>
#include <SparkFunLSM9DS1.h>

// GPS Libraries
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

//Firebase Libraries
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// Wifi Information
#define WIFI_SSID "Helix.B590" // Wifi Name
#define WIFI_PASSWORD "11111111"   // Wifi Password

// Firebase Database API Key & URL
#define API_KEY "AIzaSyCSK8XFtg3bPjTbOkvPT2JsBXwBTIRDWl8" // Firebase Project API Key
#define DATABASE_URL "https://fitnessdata-6cc1e-default-rtdb.firebaseio.com/" // FireBase Database URL

// GPIO PIN for the Pulse Heart Rate Sensor
#define IR_SENSOR_GPIO_Input_PIN 13

// GPIO Pins for the NEO-7M GPS Module
#define RX_GPIO_PIN 34
#define TX_GPIO_PIN 35

// // Using software serial to read GPS coordinates
SoftwareSerial gpsSerial(RX_GPIO_PIN,TX_GPIO_PIN);

// // TinyGPS object for the GPS sensor
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

bool signupisOk = false;

void setup() {
  Serial.begin(115200);

  gpsSerial.begin(GPSBaud); // Software serial initialisation

  pinMode(IR_SENSOR_GPIO_Input_PIN, INPUT); // Reading Input from IR_Sensor through GPIO 13

  Wire.begin(); // Using Wire for the Accelerometer conncetions
  imu.begin(); 

}

void setup(){
 while (gpsSerial.available() > 0) 
   if(gps.encode(gpsSerial.read())){
      displayAccelInfo();
      displayGPSInfo();
      displayPulse();
   }

}

void displayPulse(){
  int irValue = digitalRead(IR_SENSOR_PIN); // Read the IR sensor value
  Serial.print("Pulse detected:"); 
  Serial.println(irValue); // Print the value to the serial monitor either HIGH or LOW
  delay(1000);
}

void displayAccelInfo() {
  imu.readAccel();
  imu.readGyro();
  imu.readMag();
  
  float accelX = imu.ax;
  float accelY = imu.ay;
  float accelZ = imu.az;
  
  float gyroX = imu.gx;
  float gyroY = imu.gy;
  float gyroZ = imu.gz;
  
  float magX = imu.mx;
  float magY = imu.my;
  float magZ = imu.mz;


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
  Serial.println("Gyroscope:");
  Serial.print("X: ");
  Serial.print(gyroX);
  Serial.print("||");
  Serial.print("Y: ");
  Serial.print(gyroY);
  Serial.print("||");
  Serial.print("Z: ");
  Serial.print(gyroZ);
  Serial.print("||");

  Serial.println(); 
  Serial.println("Magnitude:");
  Serial.print("X: ");
  Serial.print(magX);
  Serial.print("||");
  Serial.print("Y: ");
  Serial.print(magY);
  Serial.print("||");
  Serial.print("Z: ");
  Serial.print(magZ);
  Serial.print("||");
  Serial.println(); 
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


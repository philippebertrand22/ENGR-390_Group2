// Setup Required:
// 1. Install the Firebase Arduino Client Library for ESP8266 and ESP 32 Vesrsion 4.7.0 or the latest available. 
//    (Steps: In your Arduino IDE, click Tools> Manage Libraries...> Search for Firebase ESP Client > Install Firebase
//      Arduino Client Library for ESP8266 and ESP32).
// 2.  Download the Tiny GPS Plus Library (Tools> Manage Libraries > Search "Tiny GPS Plus").

#include <WiFi.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

#define WIFI_SSID "Helix.B590" // Wifi Name
#define WIFI_PASSWORD "11111111"   // Wifi Password

#define API_KEY "AIzaSyCSK8XFtg3bPjTbOkvPT2JsBXwBTIRDWl8" // Firebase Project API Key
#define DATABASE_URL "https://fitnessdata-6cc1e-default-rtdb.firebaseio.com/" // FireBase Database URL

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

bool signupisOk = false;

// GPS Sensor GPIO Pins Setup:
// - Connect the RX pin of the NEO7M GPS module to the RX pin of the ESP 32 board
// - Connect the TX pin of the NEO7M GPS module to the TX pin of the ESP 32 board
// - Connect the VCC and GND connections as appropriate

static const int RXGPIOPin = 3 , TXGPIOPin = 1;
static const uint32_t GPSBaudRate = 9600;

// The TinyGPS++ object
TinyGPSPlus gps;

// Initialize The serial connection 
SoftwareSerial ss(RXGPIOPin,TXGPIOPin);

void setup()
{
  Serial.begin(9600);
  ss.begin(GPSBaudRate);
  
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
  
  

  Serial.println(F("DeviceExample.ino"));
  Serial.println(F("A simple demonstration of TinyGPS++ with an attached GPS module"));
  Serial.print(F("Testing TinyGPS++ library v. ")); Serial.println(TinyGPSPlus::libraryVersion());
  Serial.println(F("by Mikal Hart"));
  Serial.println();

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

void loop()
{
  // This sketch displays information every time a new sentence is correctly encoded.
  while(ss.available() > 0)
    if (gps.encode(ss.read()) && signupisOk)
      if (gps.location.isValid()) {
        displayGPSInfo();
        double lat = gps.location.lat();
        double lng = gps.location.lng();
        double alt = gps.altitude.meters();
        int speed = gps.speed.mps();
        int satCount = gps.satellites.value();
        String dateTime = String(gps.date.day()) + "/" + String(gps.date.month()) + "/" + String(gps.date.year()) + " " + String(gps.time.hour()) + ":" + String(gps.time.minute()) + ":" + String(gps.time.second());
       
        String jsonString = "{";
        jsonString += "\"latitude\":" + String(lat, 6) + ",";
        jsonString += "\"longitude\":" + String(lng, 6) + ",";
        jsonString += "\"altitude\":" + String(alt, 2) + ",";
        jsonString += "\"speed\":" + String(speed) + ",";
        jsonString += "\"satellite_count\":" + String(satCount) + ",";
        jsonString += "\"datetime\":\"" + dateTime + "\"";
        jsonString += "}";
        Firebase.RTDB.pushString(&fbdo, "GPSHardware/gps_data", jsonString);
        Serial.println("GPS DATA SUCCESSFULY STORED IN JSON FILE");
        delay(3000);
      } else {
         Serial.println("Error: JSON FILE NOT STORED "); 
       }
    
  }
    
  if (millis() > 5000 && gps.charsProcessed() < 10)
  {
    Serial.println(F("No GPS detected: check wiring."));
    while(true);
  }
}

void displayGPSInfo()
{
  Serial.print(F("Location: ")); 
  if (gps.location.isValid())
  {
    Serial.print(gps.location.lat(), 6);
    Serial.print(F(","));
    Serial.print(gps.location.lng(), 6);
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.print(F("  Date/Time: "));
  if (gps.date.isValid())
  {
    Serial.print(gps.date.month());
    Serial.print(F("/"));
    Serial.print(gps.date.day());
    Serial.print(F("/"));
    Serial.print(gps.date.year());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.print(F(" "));
  if (gps.time.isValid())
  {
    if (gps.time.hour() < 10) Serial.print(F("0"));
    Serial.print(gps.time.hour());
    Serial.print(F(":"));
    if (gps.time.minute() < 10) Serial.print(F("0"));
    Serial.print(gps.time.minute());
    Serial.print(F(":"));
    if (gps.time.second() < 10) Serial.print(F("0"));
    Serial.print(gps.time.second());
    Serial.print(F("."));
    if (gps.time.centisecond() < 10) Serial.print(F("0"));
    Serial.print(gps.time.centisecond());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.println();  }

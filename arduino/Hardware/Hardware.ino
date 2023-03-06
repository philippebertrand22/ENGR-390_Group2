// GPS Libraries
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

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

void setup() {
  Serial.begin(115200);

  gpsSerial.begin(GPSBaud); // Software serial initialisation

  pinMode(IR_SENSOR_GPIO_Input_PIN, INPUT); // Reading Input from IR_Sensor through GPIO 13

}

void setup(){
 while (gpsSerial.available() > 0) 
   if(gps.encode(gpsSerial.read())){
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


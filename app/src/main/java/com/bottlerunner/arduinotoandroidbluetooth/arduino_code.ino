#include <SoftwareSerial.h>
SoftwareSerial BTserial(10, 11); // RX | TX
int sensorPin = A0;
int sensorValue = 0;
int fuckUpLED=12;
int allIsWellLED=2;
int waitingLED=3;

void setup() {
  BTserial.begin(9600);
  Serial.begin(9600);
  }

void loop() {
  digitalWrite(waitingLED,HIGH);
  //necesssary to wait else, the arduino will keep on taking in values and put it in the
  //output buffer. From where they will flood into the android
  //on connection
  while(BTserial.available() == 0){
    Serial.println("Waiting for serial");
      }
  digitalWrite(waitingLED,LOW);
//  int btSerialStuff =BTserial.read();
//  Serial.print(btSerialStuff);
//    if(btSerialStuff==210){
//      digitalWrite(allIsWellLED,HIGH);
//    }
//    else{
//      digitalWrite(fuckUpLED,HIGH);
//      while(true){}
//    }
  sensorValue = analogRead(sensorPin);
  BTserial.println(sensorValue);
  Serial.println(sensorValue);
  delay(1000);
}
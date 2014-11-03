//--- 
// Move an RC car sending commands to the remote control
//  1  Forward  to pin 2
//  2  Backward to pin 3
//  3  Right    to pin 4
//  4  Left     to pin 5
//--- 
  #include <SoftwareSerial.h>// import the serial library
String readString;
int fwd = 2; // Azul 13; //2;
int bkw = 3; // Blanco
int rht = 4; // Gris
int lft = 5; // Cafe
SoftwareSerial Genotronex(8, 9); // RX, TX
//int ledpin=13; // led on D13 will show blink on / off
int BluetoothData; // the data given from Computer
void setup() {
    Genotronex.begin(9600);
    pinMode(fwd, OUTPUT);  digitalWrite(fwd, HIGH);
    pinMode(bkw, OUTPUT);  digitalWrite(bkw, HIGH);
    pinMode(rht, OUTPUT);  digitalWrite(rht, HIGH);
    pinMode(lft, OUTPUT);  digitalWrite(lft, HIGH);
     
    Genotronex.println("Bluetooth On please press 1 or 0 blink LED ..");
 // pinMode(ledpin,OUTPUT);
}

void loop() {
    if (Genotronex.available()){
BluetoothData=Genotronex.read();
   Genotronex.println(BluetoothData);


        if(BluetoothData == 'a') {
             digitalWrite(lft,LOW);
            digitalWrite(fwd, LOW);
           
        }
        if(BluetoothData == 'b') {
            digitalWrite(fwd, LOW);
            digitalWrite(lft,LOW);
        }
        if(BluetoothData == 'c') {
          digitalWrite(lft,LOW);
          delay(500);
          digitalWrite(lft,HIGH);
          digitalWrite(fwd, LOW);
        }
        if(BluetoothData == 'd') {
           digitalWrite(fwd,HIGH);
           digitalWrite(lft,LOW);
          
           digitalWrite(bkw, LOW);
           
        }
        if(BluetoothData == 'e') {
         digitalWrite(lft,LOW);
          
           digitalWrite(bkw, LOW);
        }
        if(BluetoothData == 'f') {
           digitalWrite(fwd,LOW);
           delay(1000);
          
        }
        if(BluetoothData == 'g') {
           digitalWrite(fwd,LOW);
           delay(500);
        }
        if(BluetoothData == 'h') {
          digitalWrite(fwd,HIGH);
          digitalWrite(lft,HIGH);
          digitalWrite(bkw,HIGH);
          digitalWrite(rht,HIGH);

        }
        if(BluetoothData == 'i') {
            digitalWrite(bkw,LOW);
      
        }
        if(BluetoothData == 'j') {
          digitalWrite(bkw,LOW);
          delay(1000);
        }
        if(BluetoothData == 'k') {
         digitalWrite(fwd,LOW);
         digitalWrite(rht,LOW);
         
        }
        if(BluetoothData == 'l') {
         digitalWrite(fwd,LOW);
         digitalWrite(rht,LOW);
        }
        if(BluetoothData == 'n') {
         digitalWrite(fwd,HIGH);
         digitalWrite(rht,LOW);
        }
        if(BluetoothData == 'm') {
         digitalWrite(bkw,LOW);
         digitalWrite(rht,LOW);
        }
        if(BluetoothData == 'o') {
         digitalWrite(bkw,LOW);
         digitalWrite(rht,LOW);
        }
        readString="";
    }

}

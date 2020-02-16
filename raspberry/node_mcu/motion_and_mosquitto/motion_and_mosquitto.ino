#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define ssid  "PizzaPixels"
#define pswd  "ethanspassword"

WiFiClient client;
PubSubClient mqttClient(client);

const int PIR = 13;
int pirState = LOW;
int val = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Starting...");

  WiFi.begin(ssid, pswd);
  Serial.println("Connecting...");

  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();

  Serial.print("Connected! IP: ");
  Serial.println(WiFi.localIP());

  mqttClient.setServer("ec2-18-206-127-80.compute-1.amazonaws.com", 1883);
  if (mqttClient.connect("dining_room")) {
    Serial.println("Connected to Mosquitto!");
  } else {
    Serial.println("Failed to connect to Mosquitto");
  }

  pinMode(PIR, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  mqttClient.loop();

  mqttClient.subscribe("presence");

  val = digitalRead(PIR);
  if (val == HIGH) {
    if (mqttClient.publish("motion", "Motion in Dining Room")) {
      Serial.println("Successfully published message");
    } else {
      Serial.println("Failed to publish message");
    }
    
    if (pirState == LOW) {
      pirState = HIGH;
    }

    delay(5000);
  } else {
    if (pirState == HIGH) {
      pirState = LOW;
    }

    delay(500);
  }
  val = LOW;
}

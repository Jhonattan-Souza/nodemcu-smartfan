#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ArduinoJson.h>

ESP8266WebServer server(5001);

const char *ssid = "*/******";
const char *password = "**********";

const int speedOnePin = D6;
const int speedTwoPin = D7;
const int speedThreePin = D8;

void HandleSpeed(bool one, bool two, bool three)
{
  digitalWrite(speedOnePin, LOW);
  digitalWrite(speedTwoPin, LOW);
  digitalWrite(speedThreePin, LOW);

  digitalWrite(speedOnePin, one);
  digitalWrite(speedTwoPin, two);
  digitalWrite(speedThreePin, three);
}

void handleSpeedApi()
{
  DynamicJsonDocument doc(2048);
  deserializeJson(doc, server.arg("plain"));

  int speedValue = doc["value"].as<int>();
  String valueResponse  = doc["value"].as<String>();

  if (speedValue == 0)
    HandleSpeed(LOW, LOW, LOW);
  
  if (speedValue == 1)
    HandleSpeed(HIGH, LOW, LOW);

  if (speedValue == 2)
    HandleSpeed(LOW, HIGH, LOW);

  if (speedValue == 3)
    HandleSpeed(LOW, LOW, HIGH);

  server.send(200, "application/json", "{\"speed\":"+valueResponse+"}");
}

String getStatus()
{
  if (digitalRead(speedOnePin))
    return "1";

  if (digitalRead(speedTwoPin))
    return "2";

  if (digitalRead(speedThreePin))
    return "3";

  return "0";
}

void handleStatus()
{
  String response = "{\"speed\":" + getStatus() + "}";

  server.send(200, "application/json", response);
}

void setup(void)
{
  Serial.begin(9600);

  pinMode(speedOnePin, OUTPUT);
  pinMode(speedTwoPin, OUTPUT);
  pinMode(speedThreePin, OUTPUT);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.println("Waiting to connect...");
  }

  Serial.print("IP address: ");

  Serial.println(WiFi.localIP());

  server.on("/status/", handleStatus);
  server.on("/speed/", HTTP_POST, handleSpeedApi);

  server.begin();

  Serial.println("HTTP server started");
}

void loop(void)
{
  server.handleClient();
}
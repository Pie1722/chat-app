#include <ESP8266WiFi.h>

// WiFi credentials
const char* ssid = "";
const char* password = "";

// Server listening on port 8080
WiFiServer server(8080);

// Maximum number of clients that can connect
const int maxClients = 6;
WiFiClient clients[maxClients];  // Array to store up to 6 clients

IPAddress local_IP();  // Static IP
IPAddress gateway();   // router gateway ip
IPAddress subnet();    // Subnet Mask

void setup() {
  // Start serial communication
  Serial.begin(115200);

  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.println("");

  WiFi.config(local_IP, gateway, subnet);
  
  while (WiFi.status() != WL_CONNECTED){
    Serial.println("Connecting");
    delay(1000);  
  }
  
  Serial.println("Connected to WiFi!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // Start the server
  server.begin();
  Serial.println("Server started...");
}

void loop() {

  if (WiFi.status() != WL_CONNECTED) { 
    while (WiFi.status() != WL_CONNECTED) {
      delay(1000);  
      Serial.println("Reconnecting.."); 
    }
    Serial.println("Connected");
  }
  // Task 1: Handle new client connections
  WiFiClient newClient = server.available();
  if (newClient) {
    // Find an empty slot for a new client
    bool clientAdded = false;
    for (int i = 0; i < maxClients; i++) {
      if (!clients[i]) {
        clients[i] = newClient;  // Add the new client to the array
        Serial.println("New client connected!");
        clientAdded = true;
        break;
      }
    }
    if (!clientAdded) {
      // If there is no space for a new client
      Serial.println("Server is full, rejecting client.");
      newClient.stop();
    }
  }

  // Task 2: Handle client communication (non-blocking)
  for (int i = 0; i < maxClients; i++) {
    if (clients[i]) {
      if (clients[i].available()) {
        String message = clients[i].readStringUntil('\n');
        Serial.print("Received from client: ");
        Serial.println(message);

        // Broadcast the message to all other connected clients
        for (int j = 0; j < maxClients; j++) {
          if (clients[j] && j != i) {  // Avoid sending the message to the client who sent it
            clients[j].println(message);
          }
        }
      }

      // If the client disconnects, stop the connection
      if (!clients[i].connected()) {
        clients[i].stop();
        Serial.println("Client disconnected");
        clients[i] = WiFiClient();  // Reset the client slot
      }
    }
  }
  
}

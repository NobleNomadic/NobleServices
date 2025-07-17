// Main file for Noble HTTP Server
#include "../header/httplib.h"
#include "../header/programStates.h"
#include "../header/socketlib.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Constants
#define MIN_CMD_LINE_ARGS 2 // Program name + port

// Entry point
int main(int argc, char *argv[]) {
  if (argc < MIN_CMD_LINE_ARGS) {
    printf("Usage: ./nobleserver <Port>\n");
    return STATE_BAD_USAGE;
  }

  // Main variables
  int programState;         // Track the program state
  int port = atoi(argv[1]); // Port number to listen on

  // Create server socket
  int serverSocketFD = TCPServerSetup(&programState, port);
  if (serverSocketFD < 0) {
    printf("Server setup failed with state: %d\n", programState);
    return programState;
  }

  // Listen for connections and process requests in a loop
  while (1) {
    printf("[*] Server listening on port %d", port);
    int clientSocketFD = TCPServerAccept(&programState, serverSocketFD);
    if (clientSocketFD < 0) {
      printf("Failed to accept client connection with state: %d\n",
             programState);
      continue; // Continue listening for new clients
    }

    // Allocate memory for receiving data
    char buffer[4096]; // or dynamically allocate as needed
    ssize_t bytesRecv =
        TCPRecv(&programState, clientSocketFD, buffer, sizeof(buffer));

    if (bytesRecv <= 0) {
      printf("Error receiving data or connection closed.\n");
      TCPClose(&programState, clientSocketFD);
      continue;
    }

    // Process the buffer using HTTPLib functions
    char *requestMethod = HTTPFindRequestMethod(&programState, buffer);
    if (requestMethod == NULL || strcmp(requestMethod, "GET") != 0) {
      printf("[-] Received non-GET request\n");
      TCPClose(&programState, clientSocketFD);
      continue;
    }

    char *requestPath = HTTPFindRequestPath(&programState, buffer);
    if (requestPath == NULL) {
      printf("[-] Failed to find request path\n");
      TCPClose(&programState, clientSocketFD);
      continue;
    }

    char *content = HTTPFindContent(&programState, requestPath);
    HTTPResponseData *responseData =
        HTTPGenerateData(&programState, requestMethod, requestPath, content);

    // Generate HTTP response
    char *response = HTTPCreateResponse(&programState, *responseData);

    // Send the response
    ssize_t bytesSent =
        TCPSend(&programState, clientSocketFD, response, strlen(response));
    if (bytesSent <= 0) {
      printf("Failed to send response.\n");
    }

    // Close the client connection
    TCPClose(&programState, clientSocketFD);
  }

  return STATE_GOOD;
}

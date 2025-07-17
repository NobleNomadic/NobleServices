#include "socketlib.h"     // Related header file
#include "programStates.h" // Global program states

// Needed headers for socket connections
#include <arpa/inet.h>
#include <asm-generic/socket.h>
#include <bits/types/struct_timeval.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

// Create and return a listening TCP server socket
int TCPServerSetup(int *programState, int port) {
  // Create main socket file descriptor
  int socketFD;
  struct sockaddr_in addr;

  socketFD = socket(AF_INET, SOCK_STREAM, 0);
  if (socketFD < 0) {
    *programState = STATE_SOCKET_CREATION_FAIL;
    return -1;
  }

  // Allow socket reuse
  int option = 1;
  setsockopt(socketFD, SOL_SOCKET, SO_REUSEADDR, &option, sizeof(option));

  // Setup the structure for the address
  memset(&addr, 0, sizeof(addr));
  addr.sin_family = AF_INET;
  addr.sin_addr.s_addr = INADDR_ANY;
  addr.sin_port = htons(port);

  // Bind socket
  if (bind(socketFD, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
    *programState = STATE_SOCKET_BIND_FAIL;
    return -1;
  }

  // Start listening
  if (listen(socketFD, 5) < 0) {
    *programState = STATE_SOCKET_LISTEN_FAIL;
    return -1;
  }

  // Return the file descriptor of listening socket
  *programState = STATE_GOOD;
  return socketFD;
}

// Using a listening socket, listen for connections and return client socket FD
int TCPServerAccept(int *programState, int serverSocketFD) {
  // Get client structure ready
  int clientFD;
  struct sockaddr_in client_addr;
  socklen_t len = sizeof(client_addr);

  // Begin accepting connections
  clientFD = accept(serverSocketFD, (struct sockaddr *)&client_addr, &len);
  if (clientFD < 0) {
    *programState = STATE_SOCKET_ACCEPT_FAIL;
    return -1;
  }

  // Return the client socket
  *programState = STATE_GOOD;
  return clientFD;
}

// Make a TCP client connection and return the connected file descriptor
int TCPClientConnect(int *programState, const char *targetIP, int port,
                     int timeoutSeconds) {
  // Setup socket data
  int socketFD;
  struct sockaddr_in addr;

  // Structure for timeout
  struct timeval timeout;

  // Make the socket
  socketFD = socket(AF_INET, SOCK_STREAM, 0);
  if (socketFD < 0) {
    *programState = STATE_SOCKET_CREATION_FAIL;
    return -1;
  }

  // Add timeout to the socket
  timeout.tv_sec = timeoutSeconds;
  timeout.tv_usec = 0;

  setsockopt(socketFD, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
  setsockopt(socketFD, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof(timeout));

  // Setup the address structure
  addr.sin_family = AF_INET;
  addr.sin_port = htons(port);

  // Initialise the socket
  if (inet_pton(AF_INET, targetIP, &addr.sin_addr) <= 0) {
    *programState = STATE_CLIENT_SOCKET_INIT_FAIL;
    return -1;
  }

  // Make the connection
  if (connect(socketFD, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
    *programState = STATE_CLIENT_SOCKET_CONNECTION_FAIL;
    return -1;
  }

  // Return the connected socket file descriptor
  *programState = STATE_GOOD;
  return socketFD;
}

// Send data to a connected file descripter
// Return the amount of bytes received or -1
ssize_t TCPSend(int *programState, int socketFD, const char *data, size_t len) {
  ssize_t bytesSent = send(socketFD, data, len, 0);
  if (bytesSent < 0) {
    *programState = STATE_SOCKET_SEND_FAIL;
    return -1;
  }
  *programState = STATE_GOOD;
  return bytesSent;
}

// Recv data into a buffer from a socket
// Return number of bytes received or -1
ssize_t TCPRecv(int *programState, int socketFD, char *buffer, size_t len) {
  ssize_t bytesRecv = recv(socketFD, buffer, len, 0);
  if (bytesRecv < 0) {
    *programState = STATE_SOCKET_RECV_FAIL;
    return -1;
  }
  *programState = STATE_GOOD;
  return bytesRecv;
}

// Close a socket connection and end connection
void TCPClose(int *programState, int socketFD) {
  *programState = STATE_GOOD;
  close(socketFD);
}

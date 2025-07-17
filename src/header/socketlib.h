// Definitions for socketlib
#ifndef socketlib_h
#define socketlib_h

#include <stdio.h>

int TCPServerSetup(int *programState, int port); // Setup a socket to listen for connections
int TCPServerAccept(int *programState, int serverSocketFD); // Accept connections and return the client file descriptor
int TCPClientConnect(int *programState, const char *targetIP, int port, int timeoutSeconds); // Make a connection and return the connected file descriptor
ssize_t TCPSend(int *programState, int socketFD, const char *data, size_t len); // Send data to a connected socket
ssize_t TCPRecv(int *programState, int socketFD, char *buffer, size_t len); // Receive data into the file descriptor
void TCPClose(int *programState, int socketFD); // Close a connection, return nothing

#endif // socketlib_h

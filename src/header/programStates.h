// Definitions for main program state variable
#ifndef programStates_h
#define programStates_h

// Good state. Used for when socket connections are successful or maintained
#define STATE_GOOD 0
// Socket creation failure
#define STATE_SOCKET_CREATION_FAIL 1
// Socket binding failure
#define STATE_SOCKET_BIND_FAIL 2
// Socket listening failure
#define STATE_SOCKET_LISTEN_FAIL 3
// Listening socket could not accept client socket connection
#define STATE_SOCKET_ACCEPT_FAIL 4
// Client socket initalisation failed
#define STATE_CLIENT_SOCKET_INIT_FAIL 5
// Client socket connection failure
#define STATE_CLIENT_SOCKET_CONNECTION_FAIL 6
// Failed to send data over socket
#define STATE_SOCKET_SEND_FAIL 7
// Failed to recv data over socket
#define STATE_SOCKET_RECV_FAIL 8
// Bad usage error
#define STATE_BAD_USAGE 9
// Failed to process request
#define STATE_MALFORMED_REQUEST 400
// 404 Page not found
#define STATE_NOT_FOUND 404

#endif // programStates_h

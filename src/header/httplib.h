// Header with definitions for HTTP handling
#ifndef httplib_h
#define httplib_h

// Contains needed data for building a full response
typedef struct {
  int statusCode;
  char contentType[64];
  int contentLength;
  char bodyContent[8192];
} HTTPResponseData;

// HTTP Handling functions
char *HTTPFindRequestMethod(int *programState, char *requestData); // Take the data from the request and extract and return the method
char *HTTPFindRequestPath(int *programState, char *requestData, char *homePage); // Use request data to find a path to the requested file
char *HTTPFindContent(int *programState, char *path); // Return the contents of a file
HTTPResponseData *HTTPGenerateData(int *programState, char *method, char *path, char *content); // Using provided information, generate a struct for response
char *HTTPCreateResponse(int *programState, HTTPResponseData responseData); // Build and return a response to send by generating one from provided data

#endif // httplib_h

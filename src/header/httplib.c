#include "httplib.h"
#include "programStates.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Find the request method (GET, POST, etc.)
char *HTTPFindRequestMethod(int *programState, char *requestData) {
  static char method[16];

  // Extract the first word; The method
  int requestFound = sscanf(requestData, "%s", method);

  if (requestFound == 1) {
    *programState = STATE_GOOD;
    return method;
  }
  // Unable to process request method
  *programState = STATE_MALFORMED_REQUEST;
  return NULL;
}

// Find the requested path (e.g., /index.html) and convert it to a local path
char *HTTPFindRequestPath(int *programState, char *requestData,
                          char *homePage) {
  char path[256];

  // Extract the requested path
  int pathFound = sscanf(requestData, "%*s %s", path);

  // Convert to a local path
  // Here you can add multiple web pages for various request paths
  // For now, just send back the page chosen by the server owner when /
  // is requested
  if (pathFound == 1 && strcmp(path, "/") == 0) {
    *programState = STATE_GOOD;
    return homePage;
  }
  // Unable to find the path
  *programState = STATE_MALFORMED_REQUEST;
  return NULL;
}

// Generate a struct for creating responses with provided information
HTTPResponseData *HTTPGenerateData(int *programState, char *method, char *path,
                                   char *content) {
  // Allocate memory for the response data struct
  static HTTPResponseData responseData;

  // Set the status code based on the content
  if (strcmp(content, "404") == 0) {
    responseData.statusCode = STATE_NOT_FOUND;
  } else {
    responseData.statusCode = STATE_GOOD;
  }

  // Set content type based on file type (this is just an example)
  if (strstr(path, ".html") != NULL) {
    strcpy(responseData.contentType, "text/html");
  } else {
    strcpy(responseData.contentType, "text/plain");
  }

  // Set the body content
  strcpy(responseData.bodyContent, content);

  // Set the content length (length of the body content)
  responseData.contentLength = strlen(content);

  *programState = STATE_GOOD;
  return &responseData;
}

// Read file contents into useable string
char *HTTPFindContent(int *programState, char *path) {
  static char fileContent[8192]; // Buffer to hold file content

  // Open the file
  FILE *file = fopen(path, "r");
  if (!file) {
    // If the file doesn't exist, return 404 content
    *programState = STATE_NOT_FOUND;
    return "404"; // Return a simple 404 error page
  }

  // Read the content from the file
  size_t bytesRead = fread(fileContent, 1, sizeof(fileContent) - 1, file);
  fileContent[bytesRead] = '\0'; // Null-terminate the string

  fclose(file);

  *programState = STATE_GOOD;
  return fileContent;
}

// Build out the full HTTP response
char *HTTPCreateResponse(int *programState, HTTPResponseData responseData) {
  static char HTTPResponse[10000]; // Static buffer for the HTTP response

  char *responsePtr =
      HTTPResponse; // Pointer to track where we are in the buffer

  // Add the status line to the response
  responsePtr +=
      snprintf(responsePtr, sizeof(HTTPResponse) - (responsePtr - HTTPResponse),
               "HTTP/1.1 %d OK\r\n", responseData.statusCode);

  // Add the Content-Type header
  responsePtr +=
      snprintf(responsePtr, sizeof(HTTPResponse) - (responsePtr - HTTPResponse),
               "Content-Type: %s\r\n", responseData.contentType);

  // Add the Content-Length header
  responsePtr +=
      snprintf(responsePtr, sizeof(HTTPResponse) - (responsePtr - HTTPResponse),
               "Content-Length: %d\r\n", responseData.contentLength);

  // Add the Connection header
  responsePtr +=
      snprintf(responsePtr, sizeof(HTTPResponse) - (responsePtr - HTTPResponse),
               "Connection: close\r\n\r\n");

  // Add the body content
  responsePtr +=
      snprintf(responsePtr, sizeof(HTTPResponse) - (responsePtr - HTTPResponse),
               "%s", responseData.bodyContent);

  return HTTPResponse; // Return the complete HTTP response
}

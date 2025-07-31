import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

// NobleServer.java - Main server object
// Usage: java NobleServer (port)
public class NobleServer {
  // Properties
  ServerSocket serverSocket;
  int serverPort;
  String[] routingData;

  // Data structure for a client socket and data streams
  class ClientConnection {
    Socket clientSocket; // Client socket object
    BufferedReader in;   // Input stream
    PrintWriter out;     // Output stream

    // Constructor - Setup data streams
    public ClientConnection(Socket clientSocket) {
      try {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
      }
      // Catch IO errors
      catch (IOException e) {
        System.out.println("[-] Error creating data streams for client: " + e);
      }
    }

  } // End ClientConnection


  // Entry point
  public static void main(String[] args) {
    // Bad argument usage
    if (args.length < 2) {
      System.out.println("Usage: java NobleServer (Server port) (Routing file)");
      System.exit(1);
    }

    // Extract arguments
    int port = Integer.parseInt(args[0]);
    String routerFilename = args[1];

    // Main instance of the server
    NobleServer nobleServer = new NobleServer(port, routerFilename);

    // Main loop
    while (true) {
      try {
        // Create a new client object
        ClientConnection clientConnection = nobleServer.getClientConnection();
        if (clientConnection == null) {
          continue;
        }

        System.out.println("[+] New connection");

        // Receive and process a request
        String request = clientConnection.in.readLine();
        if (request == null) {
          nobleServer.closeClientConnection(clientConnection);
          continue;
        }

        // Process the request
        String response = nobleServer.processRequest(request);

        // Send the response back
        System.out.println("[*] Sending response: " + response);
        clientConnection.out.println(response);

        // Finish connection
        nobleServer.closeClientConnection(clientConnection);
      }

      // Catch socket failures
      catch (IOException e) {
        System.out.println("[-] Error: " + e);
        continue;
      }
    }
  }

  // Constructor for class
  public NobleServer(int port, String routingFilename) {
    try {
      // Set properties
      this.serverPort = port;
      this.serverSocket = new ServerSocket(this.serverPort);

      System.out.println("[*] HTTP Server starting");

      // Read contents of the routing file
      String routeFileString = Files.readString(Path.of(routingFilename));

      this.routingData = routeFileString.split("\n");
    }
    catch (IOException e) {
      System.out.println("[-] Error setting up server: " + e);
      System.exit(1);
    }
  }

  // Accept and return client connections as a data structure
  public ClientConnection getClientConnection() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    try {
      Socket clientSocket = this.serverSocket.accept();

      // Create the data structure
      ClientConnection clientConnection = new ClientConnection(clientSocket);
      return clientConnection;
    }
    // Catch socket errors
    catch (IOException e) {
      System.out.println("[-] Failed to accept client connection: " + e);
      return null;
    }
  }

  // Process a request and return a raw string response to send back
  public String processRequest(String request) {
    System.out.println("[*] Processing: " + request);

    if (request == null) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nMissing or empty request.";
    }

    String[] tokens = request.split(" ");
    if (tokens.length < 3) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nMalformed HTTP request line.";
    }

    String method = tokens[0];
    String path = tokens[1];

    if (!method.equals("GET")) {
      return "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET\r\n\r\nOnly GET is supported.";
    }

    for (String route : routingData) {
      String[] parts = route.strip().split(":", 2);
      if (parts.length < 2) continue;

      String routePath = parts[0];
      String filePath = parts[1];

      if (routePath.equals(path)) {
        try {
          // Get content type
          String contentType = getContentType(filePath);
          // Send back the body
          String body = Files.readString(Path.of(filePath));
          return "HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n" + body;
        }
        // IOException
        catch (IOException e) {
          return "HTTP/1.1 500 Internal Server Error\r\n\r\nCould not read file: " + filePath;
        }
      }
    }

    // Could not find the file to return
    return "HTTP/1.1 404 Not Found\r\n\r\nThe requested path was not found.";
  }

  // Check the ending of the file path and set the correct content type
  private String getContentType(String file) {
    // Check file extension and return the appropriate MIME type
    if (file.endsWith(".html") || file.endsWith(".htm")) {
      return "text/html";
    } else if (file.endsWith(".css")) {
        return "text/css";
    } else if (file.endsWith(".js")) {
        return "application/javascript";
    } else if (file.endsWith(".json")) {
        return "application/json";
    } else if (file.endsWith(".png")) {
        return "image/png";
    } else if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
        return "image/jpeg";
    } else if (file.endsWith(".gif")) {
      return "image/gif";
    } else if (file.endsWith(".txt")) {
      return "text/plain";
    } else if (file.endsWith(".xml")) {
      return "application/xml";
    } else if (file.endsWith(".pdf")) {
      return "application/pdf";
    } else {
        // Default to plain
        return "text/plain";
    }
}

  // Handle closing the client connection
  public int closeClientConnection(ClientConnection clientConnection) {
    try {
      clientConnection.clientSocket.close();
      clientConnection.in.close();
      clientConnection.out.close();

      System.out.println("[*] Closed connection");
      return 0;
    }
    // Catch socket errors
    catch (IOException e) {
      System.out.println("[-] Error closing connection");
      return -1;
    }
  }
}

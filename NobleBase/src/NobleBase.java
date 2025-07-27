import java.net.*;
import java.io.*;
import java.io.FileWriter;

public class NobleBase {
  // Properties
  ServerSocket serverSocket;
  int serverPort;
  String databaseFilename;

  // Data structure to handle a client socket and data streams
  class ClientConnection {
    Socket clientSocket; // Client socket object
    BufferedReader in;   // Input stream
    PrintWriter out;     // Output stream

    // Constructor to setup data streams
    public ClientConnection(Socket clientSocket) {
      try {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
      }
      // Catch IO errors while setting up socket
      catch (IOException e) {
        System.out.println("[-] Error creating data streams for client: " + e);
      }
    }
  }

  // Entry point
  public static void main(String[] args) {
    // Handle bad argument usage
    if (args.length < 2) {
      System.out.println("Usage: java NobleBase (Server port) (Database file)");
      System.exit(1);
    }

    // Extract arguments
    int port = Integer.parseInt(args[0]);
    String databaseName = args[1];

    // Main instance of the server
    NobleBase nobleBase = new NobleBase(port, databaseName);

    // Enter the main loop
    while (true) {
      try {
        // Accept a client connection
        ClientConnection clientConnection = nobleBase.getClientConnection();
        if (clientConnection == null) {
          continue;
        }
        System.out.println("[+] New connection");


        // Receive request and process
        String request = clientConnection.in.readLine();
        String response = nobleBase.processRequest(request);

        // Send response back
        System.out.println("[*] Sending response");
        clientConnection.out.println(response);

        // Finish connection
        nobleBase.closeClientConnection(clientConnection);
      }
      catch (IOException e) {
        System.out.println("[-] Error: " + e);
        continue;
      }
    }
  }

  // Contstructor
  public NobleBase(int port, String databaseName) {
    // Set properties
    this.serverPort = port;
    this.databaseFilename = databaseName;

    System.out.println("[*] Database Server starting");

    // Create main server socket
    try {
      this.serverSocket = new ServerSocket(this.serverPort);
    }
    catch (IOException e) {
      System.out.println("[-] Error creating server socket: " + e);
      System.exit(1);
    }
  }

  // Using the main server socket, return a client connection
  public ClientConnection getClientConnection() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    try {
      // Accept socket connection
      Socket clientSocket = this.serverSocket.accept();

      // Create the data structure and return
      ClientConnection clientConnection = new ClientConnection(clientSocket);
      return clientConnection;
    }
    // Catch socket errors
    catch (IOException e) {
      System.out.println("[-] Failed to accept client connection: " + e);
      return null;
    }
  }

  // Process a request from a client, then peform the needed operation and send back HTTP response
  public String processRequest(String request) {
    // Process the request as a set of tokens
    String[] requestTokens = request.split(" ");

    // Validate tokens
    // Missing request, no tokens
    if (requestTokens.length == 0) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nMissing or empty request.";
    }
    // Malformed request, not enough tokens to process
    else if (requestTokens.length < 3) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nMalformed HTTP request.";
    }

    // Tokens good, extract request
    String method = requestTokens[0];

    // POST method - write new key value pair
    if (method.equals("POST")) {
      return databaseWrite(requestTokens);
    }
    // GET method - read a value from key
    else if (method.equals("GET")) {
      return databaseRead(requestTokens);
    }
    // PUT method - update a key's value
    else if (method.equals("PUT")) {
      return databaseUpdate(requestTokens);
    }
    // DELETE method - delete a database entry
    else if (method.equals("DELETE")) {
      return databaseDelete(requestTokens);
    }

    // No valid request method found
    return "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET POST PUT DELETE\r\n\r\n";
  }

  // ///////// CRUD operations to database /////////
  // Add a new key value pair (Usage: POST /(KEY)/(VALUE) HTTP/1.1\r\n(HEADERS)\r\n\r\n)
  private String databaseWrite(String[] requestTokens) {
    // Extract the data that needs to be written
    String[] newData = requestTokens[1].split("/");
    // Validate newData tokens
    if (newData.length != 3) {
      return "HTTP/1.1 400 Bad Request\r\n\r\nBad path for POST method";
    }
    String dataToWrite = newData[1] + ":" + newData[2] + "\n";

    // Append the new line to the bottom of the file with FileWriter
    try (FileWriter fileWriter = new FileWriter(this.databaseFilename, true)) {
      fileWriter.write(dataToWrite);
    }
    // Failed to write to file
    catch (IOException e) {
      System.out.println("[-] Error making POST to database: " + e);
      return "HTTP/1.1 500 Internal Server Error\r\n\r\nFailed to write to database.";
    }

    // No errors returned yet, function succeeded
    return "HTTP/1.1 200 OK\r\n\r\nPOST to database success.";
  }
  // Take a key, and return the value (Usage: GET /(KEY) HTTP/1.1\r\n(HEADERS)\r\n\r\n)
  private String databaseRead(String[] requestTokens) {
    return null;
  }
  // Take a key, and update the value (Usage: PUT /(KEY)/(VALUE) HTTP/1.1\r\n(HEADERS)\r\n\r\n)
  private String databaseUpdate(String[] requestTokens) {
    return null;
  }
  // Delete a key and value from the database (Usage: DELETE /(KEY) HTTP/1.1\r\n(HEADERS)\r\n\r\n)
  private String databaseDelete(String[] requestTokens) {
    return null;
  }

  // Handle closing a client connection
  public int closeClientConnection(ClientConnection clientConnection) {
    try {
      clientConnection.clientSocket.close();
      clientConnection.in.close();
      clientConnection.out.close();

      return 0;
    }
    // Catch IO errors
    catch (IOException e) {
      System.out.println("[-] Error closing connection: " + e);
      return -1;
    }
  }
}

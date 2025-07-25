import java.net.*;
import java.io.*;

// NobleServer.java - Main server object
// Usage: java NobleServer (port)
public class NobleServer {
  // Properties
  ServerSocket serverSocket;
  int serverPort;

  // Data structure for a client socket and data streams
  class ClientConnection {
    Socket clientSocket; // Client socket object
    BufferedReader in;   // Input stream
    PrintWriter out;     // Output stream

    // Constructor - Setup data streams
    public ClientConnection() {
      return;
    }
  }

  // Entry point
  public static void main(String[] args) {
    // Bad argument usage
    if (args.length < 1) {
      System.out.println("Usage: java NobleServer (Server port)");
      System.exit(1);
    }

    // Extract arguments
    int port = Integer.parseInt(args[0]);

    // Main instance of the server
    NobleServer nobleServer = new NobleServer(port);

    // Main loop
    while (true) {
      // Create a new client object
      ClientConnection clientConnection = nobleServer.getClientConnection();
      System.out.println("[+] New connection");

      // Finish connection
      nobleServer.closeClientConnection(clientConnection);

      if (false) {
        break;
      }
    }

    return;
  }

  // Constructor for class
  public NobleServer(int port) {
    try {
      // Set properties
      this.serverPort = port;
      this.serverSocket = new ServerSocket(this.serverPort);

      System.out.println("[*] Server starting");
      return;
    }
    catch (IOException e) {
      System.exit(1);
      return;
    }
  }

  // Accept and return client connections as a data structure
  public ClientConnection getClientConnection() {
    System.out.println("[*] Waiting for connection on port " + this.serverPort);

    try {
      Socket clientSocket = this.serverSocket.accept();

      // Create the data structure
      ClientConnection clientConnection = new ClientConnection();

      // Set the data
      clientConnection.clientSocket = clientSocket;
      clientConnection.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      clientConnection.out = new PrintWriter(clientSocket.getOutputStream(), true);

      return clientConnection;
    }
    // Catch socket errors
    catch (IOException e) {
      System.out.println("[-] Failed to accept client connection");
      return null;
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

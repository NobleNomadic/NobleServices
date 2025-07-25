import java.net.*;
import java.io.*;

// NobleServer.java - Main server object
// Usage: java NobleServer (port)
public class NobleServer {
  // Properties
  ServerSocket serverSocket;
  int serverPort;

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
    return;
  }

  // Constructor for class
  public NobleServer(int port) {
    try {
    // Set properties
    this.serverPort = port;
    this.serverSocket = new ServerSocket(this.serverPort);

    System.out.println("[*] Server starting on port " + this.serverPort);
    return;
    }
    catch (IOException e) {
      System.exit(1);
    }
  }
}

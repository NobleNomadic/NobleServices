import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

class NobleBase {
  // Properties
  int serverPort;
  String databaseFile;
  ServerSocket serverSocket;

  // Entry point
  public static void main(String[] args) {
    int commandLineArguments = 2;
    // Exit on bad usage
    if (args.length < commandLineArguments) {
      System.out.println("Usage: java NobleBase <Server port> <Database file>");
      return;
    }

    // Setup main instance of server object
    int port = Integer.parseInt(args[0]);
    String databaseFile = args[1];
    NobleBase nobleBase = new NobleBase(port, databaseFile);
    System.out.println("[*] Initialising server on port " + nobleBase.serverPort + " with " + nobleBase.databaseFile);

    // Enter the main database loop
    nobleBase.mainDatabaseLoop();
  }

  // Constructor
  public NobleBase(int port, String databaseFile) {
    // Set properties
    this.serverPort = port;
    this.databaseFile = databaseFile;

    // Create the socket on the chosen port
    try {
      this.serverSocket = new ServerSocket(this.serverPort);
    }
    // Failed to create socket
    catch (IOException e) {
      System.out.println("[-] Failed to create server socket: " + e);
      System.exit(1);
    }
  }

  // Main database loop for accepting and handling clients
  public void mainDatabaseLoop() {
    while (true) {
      try {
        // Accept a client connection
        Socket clientSocket = this.serverSocket.accept();
        System.out.println("[+] New connection");

        // Input/Output streams
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Receive the initial request
        String request = in.readLine();
        String[] requestTokens = request.split("\\{\\{\\{");

        if (requestTokens.length == 0) {
          in.close();
          out.close();
          clientSocket.close();
          continue;
        }

        // Check what type of request was made
        if (requestTokens.length == 4 && requestTokens[0].equals("write")) {
          databaseWrite(requestTokens);
          out.println("GOOD");
        } else if (requestTokens.length == 2 && requestTokens[0].equals("read")) {
          out.println(databaseRead(requestTokens));  // Output the result of the read operation
        } else if (requestTokens[0].equals("get")) {
          out.println(databaseGet());
        } else if (requestTokens.length == 3 && requestTokens[0].equals("delete")) {
          databaseDelete(requestTokens);
          out.println("GOOD");
        }

        // Close connection
        in.close();
        out.close();
        clientSocket.close();
    }
    // Catch socket failures
    catch (IOException e) {
        System.out.println("[-] Failed to process client connection: " + e.getMessage());
        // You can add more error handling here if needed
      }
    }
  }


  // Handle different requests and return a status code
  // Create subsection with data in a section. Usage: writedata (section) (subsection) (data)
  private int databaseWrite(String[] tokenList) {
    try {
      // Extract data from request
      String sectionName = tokenList[1];
      String subsectionName = tokenList[2];
      String data = tokenList[3];

      // This is the actual data you want to write
      String newDataString = sectionName + "{{{" + subsectionName + "{{{" + data + "\n";

      FileWriter fileWriter = new FileWriter(this.databaseFile, true);
      fileWriter.write(newDataString);
      fileWriter.close();

      System.out.println("[+] Wrote data: " + newDataString.trim());
      return 0;
    }
    catch (IOException e) {
      System.out.println("[-] Error writing to file: " + e);
      return -1;
    }
  }

  // Find and read data from database file then return found data. Usage: read (section)
  // Return all data with that section
  private String databaseRead(String[] tokenList) {
    try {
      Path path = Path.of(this.databaseFile);

      // Read the contents of the file as a single string, then convert to a list of lines
      String databaseContents = Files.readString(path);
      String[] databaseLines = databaseContents.split("\n");

      // String to send back
      StringBuilder returnData = new StringBuilder();

      // Check each line of the file for the correct section
      for (String line : databaseLines) {
        String[] lineTokens = line.split("\\{\\{\\{");
        if (lineTokens[0].equals(tokenList[1])) {  // Matching section
          returnData.append(line).append("\n");
        }
      }

      return returnData.toString();
    }
    catch (IOException e) {
      System.out.println("[-] Error reading file: " + e);
      return null;
    }
  }

  // Get the entire contents of the database and return them
  private String databaseGet() {
    try {
      Path path = Path.of(this.databaseFile);

      // Read the contents as a single string and return them
      String databaseContents = Files.readString(path);
      return databaseContents;
    }
    catch (IOException e) {
      System.out.println("[-] Error getting database: " + e);
      return null;
    }
  }

  // Delete data on the database file. Usage: delete (section) (subsection)
  private int databaseDelete(String[] tokenList) {
    try {
        // Read the entire file content
        Path path = Path.of(this.databaseFile);
        String databaseContents = Files.readString(path);
        String[] databaseLines = databaseContents.split("\n");

        // StringBuilder to hold updated content after deletion
        StringBuilder updatedData = new StringBuilder();

        // Iterate over lines, adding them to updated data if they don't match the section/subsection
        for (String line : databaseLines) {
            // Split line by the delimiter `{{{`
            String[] lineTokens = line.split("\\{\\{\\{");

            // Check if this line matches the section and subsection to be deleted
            if (lineTokens.length >= 3 &&
                lineTokens[0].equals(tokenList[1]) &&   // Match section
                lineTokens[1].equals(tokenList[2])) {  // Match subsection
                // Skip this line (it's the one we want to delete)
                continue;
            }

            // Otherwise, keep this line in the updated data
            updatedData.append(line).append("\n");
        }

        // Overwrite the file with the updated content
        Files.writeString(path, updatedData.toString());
        System.out.println("[+] Deleted data: " + tokenList[1] + " - " + tokenList[2]);
        return 0;
    }
    catch (IOException e) {
        System.out.println("[-] Error deleting data: " + e);
        return -1;
    }
  }
}

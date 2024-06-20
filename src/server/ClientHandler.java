package src.server;

import java.io.*;
import java.net.*;


public class ClientHandler extends Thread {
    Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/pm")) {
                    // Private message: /pm recipient message
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String message = parts[2];
                        server.privateMessage(this.username + " (private): " + message, recipient);
                    }
                } else if (inputLine.startsWith("/group")) {
                    // Group message: /group user1,user2 message
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String[] recipients = parts[1].split(",");
                        String message = parts[2];
                        for (String recipient : recipients) {
                            server.privateMessage(this.username + " (group): " + message, recipient);
                        }
                    }
                } else if (inputLine.startsWith("/file")) {
                    // File transfer: /file recipient filename
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String filename = parts[2];
                        sendFile(recipient, filename);
                    }
                } else {
                    server.broadcastMessage(this.username + ": " + inputLine, this);
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close resources: " + e.getMessage());
            }
            server.removeUser(this.username);
            server.updateClientUserList();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendFile(String recipient, String filename) {
        ClientHandler recipientHandler = server.userMap.get(recipient);
        if (recipientHandler != null) {
            try {
                File file = new File(filename);
                if (file.exists()) {
                    DataOutputStream dos = new DataOutputStream(recipientHandler.clientSocket.getOutputStream());
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    dos.writeUTF("File:" + file.getName());
                    int read;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                    }
                    dos.flush();
                    fis.close();
                } else {
                    sendMessage("File not found: " + filename);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessage("User not found: " + recipient);
        }
    }

    public String getUsername() {
        return username;
    }
}

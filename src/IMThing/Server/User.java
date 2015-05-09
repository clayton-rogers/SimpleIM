package IMThing.Server;

import IMThing.Configuration;

import java.io.*;
import java.net.Socket;

/**
 * The representation of a user. Listens for input from a user and pipes it to the server for
 * redistribution.
 *
 * Created by Clayton on 23/04/2015.
 */
public class User {
    private String username;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isConnected = false;

    Thread readThread;

    public User (Socket socket) {

        String version = "";
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            username = reader.readLine().trim();
            version = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!version.equals(Configuration.PROTOCOL_VERSION)) {
            try {
                writer.write("Disconnected due to mismatched version number.");
                writer.write("Server is version: " + Configuration.PROTOCOL_VERSION + " Client is version: " + version);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        isConnected = true;
        readThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String in = reader.readLine();
                        if (in == null) {
                            break;
                        }
                        String message = username + ": " + in;
                        IMServer.messages.offer(new Message(User.this, message));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isConnected = false;
            }
        });
        readThread.start();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void sendMessage (String message) {
        try {
            System.out.println("Sending message to user " + getUsername() + " |" + message + "|");
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}
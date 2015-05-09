package IMThing.Server;

import IMThing.Configuration;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
    private boolean isConnected;
    private Socket socket;

    Thread readThread;

    public User (Socket socket) {
        this.socket = socket;
        isConnected = true;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            username = Configuration.receiveHandshake(reader);
        } catch (IOException | Configuration.HandshakeException e) {
            e.printStackTrace();
            isConnected = false;
        }

        if (isConnected) {
            readThread = new Thread(new Runnable() {
                public void run() {
                    while (isConnected()) {
                        try {
                            String message = reader.readLine();  // This line blocks for input
                            if (message == null) {
                                break;
                            }
                            IMServer.messages.offer(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                            isConnected = false;
                        }
                    }
                    isConnected = false;
                }
            });
            readThread.start();
        }
    }

    public boolean isConnected() {
        if (isConnected) {
            isConnected = socket.isConnected();
        }
        return isConnected;
    }

    public void sendMessage (String message) {
        if (isConnected()) {
            try {
                System.out.println("Sending message to " + username + " |" + message + "|");
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void close() {
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
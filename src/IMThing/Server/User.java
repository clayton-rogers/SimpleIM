package IMThing.Server;

import IMThing.Configuration;
import IMThing.Configuration.HandshakeException;

import java.io.*;
import java.net.Socket;

/**
 * The representation of a user. Listens for input from a user and pipes it to the server for
 * redistribution. Also provides functionality to send messages to this user.
 *
 * Created by Clayton on 23/04/2015.
 */
class User {
    private String username;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isConnected;
    private final Socket socket;

    /**
     * Creates a new user which can be communicated with on the given socket.
     * @param socket The socket to communicate with this user.
     */
    User (Socket socket) {
        this.socket = socket;
        isConnected = true;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            username = Configuration.receiveHandshake(reader);
        } catch (IOException | HandshakeException e) {
            e.printStackTrace();
            isConnected = false;
        }

        if (isConnected) {
            Thread readThread = new Thread(new Runnable() {
                @Override
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

    /**
     * Checks whether this user is still connected.
     *
     * @return True when the user is still connected.
     */
    public boolean isConnected() {
        if (isConnected) {
            isConnected = socket.isConnected();
        }
        return isConnected;
    }

    /**
     * Sends a message to this user.
     *
     * @param message The message to be sent to this user.
     */
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

    /**
     * Gets the username of this user.
     *
     * @return The username of this user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Closes the connection with this user.
     */
    public void close() {
        isConnected = false;
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
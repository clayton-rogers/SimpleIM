package IMThing.Client;

import IMThing.Configuration;
import IMThing.Configuration.HandshakeException;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents the connection with the server. Allow the receipt and sending of messages.
 *
 * Created by Clayton on 09/05/2015.
 */
class Connection {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /** The list of messages that have been received on the socket, but have yet to be added to
     *  the GUI. */
    public final BlockingQueue<String> newMessages = new LinkedBlockingQueue<>();

    private boolean isConnected = true;

    /**
     * Creates a new connection with the given information.
     *
     * @param IP The host of the IM server.
     * @param username The username to connect with.
     */
    Connection(String IP, String username) {

        // Connect and send the username
        try {
            socket = new Socket(IP, Configuration.PORT_NUMBER);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            Configuration.sendHandshake(writer, username);
        } catch (IOException | HandshakeException e) {
            e.printStackTrace();
            isConnected = false;
        }

        // This thread constantly checks for new messages on the socket.
        if (isConnected) {
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isConnected()) {
                        try {
                            String message = reader.readLine();
                            if (message == null) {
                                break;
                            }
                            newMessages.offer(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    isConnected = false;
                }
            });
            readThread.start();
        }
    }

    /**
     * Allows the owner of the connection to send a message to the server.
     *
     * @param message The message to be sent.
     */
    public void sendMessage(String message) {
        if (isConnected()) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns whether the connection is connected.
     *
     * @return True when the connection is connected.
     */
    public boolean isConnected() {
        if (isConnected) {
            isConnected = socket.isConnected();
        }
        return isConnected;
    }
}

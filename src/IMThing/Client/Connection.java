package IMThing.Client;

import IMThing.Configuration;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Created by Clayton on 09/05/2015.
 */
public class Connection {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    public BlockingQueue<String> newMessages = new LinkedBlockingQueue<>();

    private boolean isConnected = true;

    /**
     * Creates a new connection with the given information.
     *
     * @param IP The host of the IM server.
     * @param username The username to connect with.
     */
    public Connection(String IP, String username) {

        // Connect and send the username
        try {
            socket = new Socket(IP, Configuration.PORT_NUMBER);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            Configuration.sendHandshake(writer, username);
        } catch (IOException | Configuration.HandshakeException e) {
            e.printStackTrace();
            isConnected = false;
        }

        final Thread readThread;
        if (isConnected) {
            readThread = new Thread(new Runnable() {
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

    public boolean isConnected() {
        isConnected = socket.isConnected();
        return isConnected;
    }
}

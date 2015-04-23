package IMThing.Server;

import java.io.*;
import java.net.Socket;

/**
 * Created by Clayton on 23/04/2015.
 */
public class User {
    String username;
    Socket socket;
    BufferedWriter writer;
    BufferedReader reader;

    Thread readThread;

    public User (Socket socket) {
        this.socket = socket;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            username = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String in = reader.readLine();
                        if (in == null) {
                            break;
                        }
                        System.out.println(username + ": " + in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();
    }

    public String getUsername() {
        return username;
    }
}
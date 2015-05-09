package IMThing.Client;

import IMThing.Configuration;

import java.io.*;
import java.net.*;

public class IMClient {
    public static void main(String argv[]) {


        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        // Get the username
        String username = "";
        System.out.println("Enter your username: ");
        try {
            username = console.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get IP
        String IP = "";
        System.out.println("Enter the IP: ");
        try {
            IP = console.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Connect and send the username
        Socket socket;
        final BufferedReader reader;
        BufferedWriter writer;
        try {
            socket = new Socket(IP,Configuration.PORT_NUMBER);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(username + "\n");
            writer.write(Configuration.PROTOCOL_VERSION + "\n");
            // TODO add a version string here, to prevent mismatched client and server
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not connect.");
            return;
        }

        System.out.println("Enter some text to chat (q to quit):");

        Thread receivingThread = new Thread(new Runnable() {
            private String username;
            @Override
            public void run() {
                while (true) {
                    try {
                        String messageReceived = reader.readLine();
                        System.out.println();
                        System.out.println(messageReceived);
                        printPrompt(username);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private Runnable init(String username) {
                this.username = username;
                return this;
            }
        }.init(username));
        receivingThread.start();

        for (;;) {
            String words = "";
            printPrompt(username);
            try {
                words = console.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (words.equals("q")) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                try {
                    writer.write(words + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.exit(0);
    }

    private static void printPrompt(String username) {
        System.out.print(username + ": ");
    }
}

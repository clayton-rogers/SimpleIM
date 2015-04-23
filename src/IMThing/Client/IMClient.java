package IMThing.Client;

import java.io.*;
import java.net.*;

public class IMClient {
    public static void main(String argv[]) {

        System.out.println("Enter your username: ");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        String username = "";
        try {
            username = console.readLine();
        } catch (IOException e) {
        }

        Socket socket;
        try {
            socket = new Socket("localhost",5555);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("Could not connect.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not connect.");
            return;
        }

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(username + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Enter some text to chat:");

        for (;;) {
            String words = "";
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
    }
}

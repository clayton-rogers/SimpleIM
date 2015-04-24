package IMThing.Server;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IMServer {
    public static Queue<String> messages = new ConcurrentLinkedQueue<>();
    private static List<User> userList = new ArrayList<>();
    private static boolean isRunning = true;

    public static void main(String argv[]) throws Exception {
        System.out.println("Server is running" );
        ServerSocket serverSocket = new ServerSocket(5555);

        Thread rebroadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    if (!messages.isEmpty()) {
                        String message = messages.poll();
                        for (User user : userList) {
                            if (!user.isConnected()) {
                                userList.remove(user);
                            } else {
                                user.sendMessage(message);
                            }
                        }
                        if (message.contains("kill")) {
                            isRunning = false;
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        rebroadcastThread.start();

        while(isRunning) {
            Socket socket = serverSocket.accept();
            System.out.println("New connection");
            User user = new User(socket);
            userList.add(user);
            System.out.println("New user connected: " + user.getUsername());
        }
    }
}

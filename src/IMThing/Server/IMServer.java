package IMThing.Server;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IMServer {
    public static Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private static List<User> userList = new ArrayList<>();
    private static boolean isRunning = true;

    public static void main(String argv[]) throws Exception {
        System.out.println("Server is running" );
        ServerSocket serverSocket = new ServerSocket(5555);

        Thread rebroadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message message = messages.poll();
                    if (message == null) {continue;}
                    Iterator<User> i = userList.iterator();
                    while (i.hasNext()) {
                        User user = i.next();
                        if (!user.isConnected()) {
                            i.remove();
                        } else {
                            if (!message.getSource().equals(user)) {
                                user.sendMessage(message.getMessage());
                            }
                        }
                    }
                    if (message.getMessage().contains("kill")) {
                        isRunning = false;
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

package IMThing.Server;

import IMThing.Configuration;

import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IMServer {
    public static final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private static final List<User> userList = new ArrayList<>();
    private static boolean isRunning = true;

    public static void main(String argv[]) throws Exception {
        System.out.println("Server is running" );
        ServerSocket serverSocket = new ServerSocket(Configuration.PORT_NUMBER);

        // This thread waits for there to be a message in the queue, then sends it
        // to all the connected users.
        Thread rebroadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    String message = null;
                    try {
                        message = messages.poll(100, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (message == null) {continue;}
                    synchronized (userList) {
                        for (User user : userList) {
                            user.sendMessage(message);
                        }
                    }
                    if (message.equals("kill")) {
                        isRunning = false;
                    }
                }
            }
        });
        rebroadcastThread.start();

        // This thread checks the user list every 100 ms
        // for any users that have disconnected.
        Thread userManagerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (userList) {
                        Iterator<User> i = userList.iterator();
                        while (i.hasNext()) {
                            User user = i.next();
                            if (!user.isConnected()) {
                                messages.offer("SERVER: " + user.getUsername() + " has disconnected.");
                                user.close();
                                i.remove();
                            }
                        }
                    }
                }
            }
        });
        userManagerThread.start();

        while(isRunning) {
            Socket socket = serverSocket.accept();
            System.out.println("New connection");
            User user = new User(socket);
            userList.add(user);
            System.out.println("New user connected: " + user.getUsername());
            user.sendMessage("Connected users are:");
            synchronized (userList) {
                for (User userInstance : userList) {
                    user.sendMessage(" * " + userInstance.getUsername());
                }
            }
            user.sendMessage("");
        }
    }
}

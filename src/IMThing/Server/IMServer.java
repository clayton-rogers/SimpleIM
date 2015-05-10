package IMThing.Server;

import IMThing.Configuration;

import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The IM server which listens for new connections, receives messages, and rebroadcasts those
 * messages to the users.
 *
 * Created by Clayton on 23/04/2015.
 */
public class IMServer {
    /** The queue of messages that need to be sent out to all the clients. */
    public static final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    /** The list of users currently connected to the server. */
    private static final List<User> userList = new ArrayList<>();
    /** True when the server is running. Used to stop the server. */
    private static boolean isRunning = true;

    public static void main(String argv[]) throws Exception {
        System.out.println("Server is running");
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
                    if (message.contains("/kill")) {
                        isRunning = false;
                        System.exit(0);
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

        // Finally the main thread just sits around waiting for clients to connect.
        while(isRunning) {
            Socket socket = serverSocket.accept();

            User user = new User(socket);
            userList.add(user);

            // Log the connection
            System.out.println("New user connected: " + user.getUsername());

            // Give the server welcome message to the new user.
            user.sendMessage("Connected users are:");
            synchronized (userList) {
                for (User userInstance : userList) {
                    user.sendMessage(" * " + userInstance.getUsername());
                }
            }
            user.sendMessage("");

            // Tell the other users that a new user has connected.
            messages.offer("SERVER: " + user.getUsername() + " connected!");
        }
    }
}

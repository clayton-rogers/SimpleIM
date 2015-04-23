package IMThing.Server;

import java.net.*;
import java.util.*;

public class IMServer {
    public static void main(String argv[]) throws Exception {
        System.out.println(" Server is Running  " );
        ServerSocket mysocket = new ServerSocket(5555);

        List<User> userList = new ArrayList<>();

        while(true) { 
            Socket socket = mysocket.accept();
            System.out.println("New connection");
            User user = new User(socket);
            userList.add(user);
            System.out.println("New user connected: " + user.getUsername());
        }
    }
}
//
//class HandleConnection extends Thread  {
//    Socket socket;
//
//    public HandleConnection(Socket socket) {
//        this.socket = socket;
//    }
//
//    @Override
//    public void run() {
//        try {
//            BufferedWriter writer =	new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            for (int i = 0; i < 100000; i++) {
//                writer.write("Text text text text " + i + "\n");
//            }
//            writer.flush();
//            socket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

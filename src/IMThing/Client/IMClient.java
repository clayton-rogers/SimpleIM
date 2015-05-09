package IMThing.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class IMClient extends JFrame implements ActionListener {

    private JButton sendButton = new JButton("Send");
    private JTextArea messageArea = new JTextArea("Welcome to IMClient!\n");
    private JTextField messageBar = new JTextField();
    private Connection connection;

    private static String username = JOptionPane.showInputDialog("Enter username:");
    private static String hostname = JOptionPane.showInputDialog("Enter server IP:");

    public IMClient(final Connection connection) {
        // *** Set up all the GUI stuff *** //
        messageArea.setEditable(false);
        messageBar.addActionListener(this);
        sendButton.addActionListener(this);

        Container c = getContentPane();
        c.setLayout(new BorderLayout(5, 5));

        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));

        bottomPane.add(messageBar);
        bottomPane.add(sendButton);

        c.add(messageArea, BorderLayout.CENTER);
        c.add(bottomPane, BorderLayout.PAGE_END);


        // Add a listener for the close button
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(500, 500);
        setVisible(true);


        // *** Set up other stuff *** //
        this.connection = connection;
        if (connection.isConnected()) {
            messageArea.append("You are connected!\n\n");
        } else {
            messageArea.append("Could not connect!\n\n");
        }

        Thread messageReader = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Started message reader thread.");
                while (connection.isConnected()) {
                    try {
                        String message = connection.newMessages.poll(100, TimeUnit.SECONDS);
                        messageArea.append(message + "\n");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        messageReader.start();

        messageBar.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == messageBar || e.getSource() == sendButton) {
            if (!messageBar.getText().equals("")) {
                String text = username + ": " + messageBar.getText();
                connection.sendMessage(text);
                messageBar.setText("");
            }
        }
    }

    public static void main(String argv[]) {
        new IMClient(new Connection(hostname, username));
    }
}

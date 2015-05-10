package IMThing.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

/**
 * The GUI client application.
 *
 * Created by Clayton on 09/05/2015.
 */
final class IMClient extends JFrame implements ActionListener {

    private final JButton sendButton = new JButton("Send");
    private final JTextArea messageArea = new JTextArea("Welcome to IMClient!\n");
    private final JTextField messageBar = new JTextField();
    private final Connection connection;
    private final String username;

    private IMClient(String hostname, String username) {
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


        // Set the program to exit when the X is clicked.
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(500, 700);
        setVisible(true);


        // *** Set up other stuff *** //
        this.username = username;
        connection = new Connection(hostname, username);
        if (connection.isConnected()) {
            messageArea.append("You are connected!\n\n");
        } else {
            messageArea.append("Could not connect!\n\n");
        }

        // There is no danger in starting this thread even when we are not connected,
        // so we always start it.
        Thread messageReader = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Started message reader thread.");
                while (connection.isConnected()) {
                    try {
                        String message = connection.newMessages.poll(100L, TimeUnit.SECONDS);
                        messageArea.append(message + "\n");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        messageReader.start();

        // Sets the focus to start on the message input field.
        messageBar.requestFocus();
    }

    /**
     * Handle the enter key and the button press.
     * @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(messageBar) || e.getSource().equals(sendButton)) {
            if (!messageBar.getText().equals("")) {
                String text = username + ": " + messageBar.getText();
                connection.sendMessage(text);
                messageBar.setText("");
            }
        }
    }

    /**
     * Start the program.
     * @param argv The arguments.
     */
    public static void main(String[] argv) {
        String username = JOptionPane.showInputDialog("Enter username:");
        String hostname = JOptionPane.showInputDialog("Enter server IP:");
        new IMClient(hostname, username);
    }
}

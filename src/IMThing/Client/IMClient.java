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


        // Set the program to exit when the X is clicked.
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(500, 700);
        setVisible(true);


        // *** Set up other stuff *** //
        this.connection = connection;
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
                        String message = connection.newMessages.poll(100, TimeUnit.SECONDS);
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
        if (e.getSource() == messageBar || e.getSource() == sendButton) {
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
    public static void main(String argv[]) {
        new IMClient(new Connection(hostname, username));
    }
}

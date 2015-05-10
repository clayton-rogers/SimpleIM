package IMThing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Configuration class to hold the common configuration information.
 *
 * Created by Clayton on 25/04/2015.
 */
public final class Configuration {
    /** The protocol version for the handshake */
    private static final String PROTOCOL_VERSION = "1.4";
    /** The default port number to use. */
    public static final int PORT_NUMBER = 60055;

    private Configuration() {
    }

    /**
     * HandshakeException is thrown whenever there is an issue with the handshake.
     * This is generally due to mismatched versions between the server and the client.
     */
    public static class HandshakeException extends Exception {
        public HandshakeException(Throwable cause) {
            super(cause);
        }
        public HandshakeException() {}
    }

    /**
     * Sends the handshake to the server.
     *
     * @param writer The socket to write to.
     * @param username The username to register with the server.
     * @throws HandshakeException When there is an issue completing the handshake.
     */
    public static void sendHandshake (BufferedWriter writer, String username) throws HandshakeException {
        try {
            writer.write(username + "\n");
            writer.write(PROTOCOL_VERSION + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new HandshakeException(e);
        }
    }

    /**
     * Receives the handshake from the client.
     *
     * @param reader The socket to read from.
     * @return The username of the new user.
     * @throws HandshakeException When there is an issue completing the handshake.
     */
    public static String receiveHandshake (BufferedReader reader) throws HandshakeException{
        String username;
        String version;
        try {
            username = reader.readLine().trim();
            version = reader.readLine().trim();
        } catch (IOException e) {
            throw new HandshakeException(e);
        }

        if (!version.equals(PROTOCOL_VERSION)) {
            throw new HandshakeException();
        }
        return username;
    }
}

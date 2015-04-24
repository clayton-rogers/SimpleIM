package IMThing.Server;

/**
 * Represents a message which needs to be distributed to everyone except the source.
 *
 * Created by Clayton on 23/04/2015.
 */
public class Message {

    private final User source;
    private final String message;

    public Message(User source, String message) {
        this.source = source;
        this.message = message;
    }

    public User getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }
}

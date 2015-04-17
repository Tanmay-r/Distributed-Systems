import java.io.Serializable;

public class Message implements Serializable {

	MessageType type;
	String msg;

	public Message(MessageType t, String m) {
		type = t;
		msg = m;
	}
}

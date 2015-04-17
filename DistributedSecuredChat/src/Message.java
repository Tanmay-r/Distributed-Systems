import java.io.Serializable;

public class Message implements Serializable {

	MessageType type;
	String msg;
	String key;
	public Message(MessageType t, String k, String m) {
		type = t;
		key = k;
		msg = m;
	}
}

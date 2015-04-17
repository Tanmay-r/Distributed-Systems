import java.io.Serializable;

public class Message implements Serializable {

	MessageType type;
	String msg;
	byte[] key;
	public Message(MessageType t, byte[] k, String m) {
		type = t;
		key = k;
		msg = m;
	}
}

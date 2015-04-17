import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MessageType type;
	byte[] msg;
	byte[] key;

	public Message(MessageType t, byte[] k, String m) {
		type = t;
		key = k;
		try {
			msg = m.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Message(MessageType t, byte[] k, byte[] m) {
		type = t;
		key = k;
		msg = m;
	}
}

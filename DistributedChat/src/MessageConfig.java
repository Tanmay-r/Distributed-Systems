import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageConfig {
	public String sender_id;
	public int seq_number;
	public Map<String, Integer> ack_received = new HashMap<String, Integer>();

	public boolean ackReceived(String id) {
		if (ack_received.get(id) == 0) {
			ack_received.put(id, 1);
		}
		return this.allReceived();
	}

	public boolean allReceived() {
		return !(ack_received.containsValue(0));
	}

	public void makeMap(Set<String> l) {
		for (String s : l) {
			ack_received.put(s, 0);
		}
	}
}

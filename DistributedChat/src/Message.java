public class Message {

	public String sender_id;
	public int seq_number;
	public String data;
	public int timestamp;
	public boolean deliverable;

	public Message(String sender_id, int seq_number, String data, int timestamp) {
		super();
		this.sender_id = sender_id;
		this.data = data;
		this.timestamp = timestamp;
		this.seq_number = seq_number;
		this.deliverable = false;
	}

	public String getSender_id() {
		return sender_id;
	}

	public String getData() {
		return data;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public int getSeq_number() {
		return seq_number;
	}

	public void setSender_id(String sender_id) {
		this.sender_id = sender_id;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public void setSeq_number(int seq_number) {
		this.seq_number = seq_number;
	}

	public boolean isDeliverable() {
		return deliverable;
	}

	public void setDeliverable(boolean deliverable) {
		this.deliverable = deliverable;
	}

	@Override
	public String toString() {
		return sender_id + " " + seq_number + " " + data + " " + timestamp;
	}

}

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.List;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {
	public static final String MESSAGE = "Hello World";
	public String client_id = "";
	public int number_of_clients = 0;
	public List<String> all_clients;
	public List<MessageConfig> all_message_configs;
	public int current_sequence_number = 0;
	public int global_timestamp = 0;
	public List<Message> message_queue;
	public boolean other_exist = false;

	public RmiServer(String client_id) throws RemoteException {
		super(0); // required to avoid the 'rmic' step, see below
		all_clients = new ArrayList<String>();
		all_message_configs = new ArrayList<MessageConfig>();
		message_queue = new ArrayList<Message>();
		this.client_id = client_id;
	}

	public void welcomeMessage(String client_id, int number_of_clients,
			int timestamp) {
		this.other_exist = true;
		if (timestamp > this.global_timestamp)
			this.global_timestamp = timestamp;
		this.number_of_clients = number_of_clients;
		if (!all_clients.contains(client_id)) {
			all_clients.add(client_id);
		}
		MessageConfig conf = getMessageConfig(this.client_id, 0);
		conf.ack_received.put(client_id, 1);
		if (this.number_of_clients == conf.ack_received.size()) {
			this.all_message_configs.remove(conf);
			this.message_queue.remove(this.getMessage(this.client_id, 0));
			this.checkPrint();
			for (String recv_id : all_clients) {
				try {
					RmiServerIntf obj = (RmiServerIntf) Naming
							.lookup("//localhost/" + recv_id);
					obj.globalMessage(this.client_id, 0, this.global_timestamp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println(this.client_id + " Joined Chat");
			Thread multicast_server_thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						DistributedChat.multicastServer();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});
			multicast_server_thread.run();

		}
	}

	private MessageConfig getMessageConfig(String sender_id, int seq_no) {
		for (MessageConfig conf : this.all_message_configs) {
			if (conf.sender_id == sender_id && conf.seq_number == seq_no) {
				return conf;
			}
		}
		return null;
	}

	private Message getMessage(String sender_id, int seq_no) {
		for (Message msg : this.message_queue) {
			if (msg.sender_id == sender_id && msg.seq_number == seq_no) {
				return msg;
			}
		}
		return null;
	}

	public void globalMessage(String client_id, int seq_no, int timestamp) {
		if (this.global_timestamp < timestamp)
			this.global_timestamp = timestamp;
		Message msg = this.getMessage(client_id, seq_no);
		msg.deliverable = true;
		msg.timestamp = timestamp;
		if (seq_no == 0)
			msg.data = "$join$";
		this.checkPrint();
	}

	public void checkPrint() {
		Message min = null;
		for (Message msg : this.message_queue) {
			if (min == null)
				min = msg;
			if (min.timestamp > msg.timestamp)
				min = msg;
			else if (min.timestamp == msg.timestamp) {
				if (min.deliverable && !msg.deliverable) {
					min = msg;
				} else if (min.deliverable && msg.deliverable) {
					if (min.sender_id.compareTo(msg.sender_id) > 0) {
						min = msg;
					}
				}
			}
		}
		if (min != null && min.deliverable) {
			if (min.data.compareTo("$join$") == 0) {
				System.out.println(min.sender_id + " Joined Chat");
				this.all_clients.add(min.sender_id);
			} else {
				System.out.println(min.sender_id + ": " + min.data);
			}
			this.message_queue.remove(min);
		}

	}

	public void normalMessage(Message msg) {
		try {
			if (this.global_timestamp > msg.timestamp) {
				msg.timestamp = this.global_timestamp;
			} else {
				this.global_timestamp = msg.timestamp;
			}
			this.message_queue.add(msg);
			this.checkPrint();
			RmiServerIntf obj = (RmiServerIntf) Naming.lookup("//localhost/"
					+ msg.sender_id);
			obj.ackMessage(client_id, msg.seq_number, this.global_timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ackMessage(String client_id, int seq_no, int timestamp) {
		if (timestamp > this.global_timestamp)
			this.global_timestamp = timestamp;

		MessageConfig conf = getMessageConfig(this.client_id, seq_no);
		Message msg = this.getMessage(this.client_id, seq_no);
		if (msg.timestamp < timestamp)
			msg.timestamp = timestamp;
		this.checkPrint();
		if (conf.ackReceived(client_id)) {
			msg.deliverable = true;
			for (String recv_id : all_clients) {
				try {
					RmiServerIntf obj = (RmiServerIntf) Naming
							.lookup("//localhost/" + recv_id);
					obj.globalMessage(this.client_id, msg.seq_number,
							this.global_timestamp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.checkPrint();
		}
	}
}
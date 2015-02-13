import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {
	public static final String MESSAGE = "Hello World";
	public String client_id = "";
	public String ip_address = "";
	public int number_of_clients = 0;
	public Map<String, String> all_clients;
	public List<MessageConfig> all_message_configs;
	public int current_sequence_number = 0;
	public int global_timestamp = 0;
	public List<Message> message_queue;
	public boolean other_exist = false;

	public RmiServer(String client_id, String ip_address)
			throws RemoteException {
		super(0); // required to avoid the 'rmic' step, see below
		all_clients = new HashMap<String, String>();
		all_message_configs = new ArrayList<MessageConfig>();
		message_queue = new ArrayList<Message>();
		this.client_id = client_id;
		this.ip_address = ip_address;
	}

	public void welcomeMessage(String client_id, int number_of_clients,
			int timestamp, String ip_address) {
		System.out.println(this.client_id + " " + "Welcome Message "
				+ client_id);
		this.other_exist = true;
		if (timestamp > this.global_timestamp)
			this.global_timestamp = timestamp;
		this.number_of_clients = number_of_clients + 1;
		if (!all_clients.containsKey(client_id)) {
			all_clients.put(client_id, ip_address);
		}
		System.out.println(this.client_id + " search ");
		MessageConfig conf = getMessageConfig(this.client_id, 0);
		System.out.println(this.client_id + " conf " + conf);
		conf.ack_received.put(client_id, 1);
		if (this.number_of_clients == conf.ack_received.size()) {
			this.all_message_configs.remove(conf);
			this.message_queue.remove(this.getMessage(this.client_id, 0));
			this.checkPrint();
			for (Map.Entry<String, String> recv : this.all_clients.entrySet()) {
				try {
					RmiServerIntf obj = (RmiServerIntf) Naming.lookup("//"
							+ recv.getValue() + "/" + recv.getKey());
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

	public MessageConfig getMessageConfig(String sender_id, int seq_no) {
		for (MessageConfig conf : this.all_message_configs) {
			System.out.println(sender_id + " " + seq_no + " " + conf.sender_id
					+ " " + conf.seq_number);
			if (conf.sender_id.compareTo(sender_id) == 0
					&& conf.seq_number == seq_no) {
				return conf;
			}
		}
		return null;
	}

	public Message getMessage(String sender_id, int seq_no) {

		for (Message msg : this.message_queue) {
			if (msg.sender_id.compareTo(sender_id) == 0
					&& msg.seq_number == seq_no) {
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
		System.out.println(this.client_id + " Global message" + " " + msg + " "
				+ msg.deliverable);
		System.out.println(this.client_id + " Global messagecheck" + " "
				+ this.getMessage(client_id, seq_no) + " "
				+ this.getMessage(client_id, seq_no).deliverable);
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
		System.out.println("Min aaya he " + min);
		if (min != null && min.deliverable) {
			System.out.println("Min aaya he dele " + min);
			if (min.seq_number == 0) {
				System.out.println(min.sender_id + " Joined Chat");
				this.all_clients.put(min.sender_id, min.data);
				this.number_of_clients = this.all_clients.size();
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
			RmiServerIntf obj = (RmiServerIntf) Naming
					.lookup("//" + this.all_clients.get(msg.sender_id) + "/"
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
			for (Map.Entry<String, String> recv : this.all_clients.entrySet()) {
				try {
					RmiServerIntf obj = (RmiServerIntf) Naming.lookup("//"
							+ recv.getValue() + "/" + recv.getKey());
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
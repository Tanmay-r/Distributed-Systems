import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.Scanner;

public class DistributedChat {
	static String client_id;
	static String ip_address;
	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 9999;
	static RmiServer rmi_obj = null;

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.print("Incorrect Use : Give client unique id");
			return;
		}
		InetAddress IP = InetAddress.getLocalHost();
		client_id = args[0];
		ip_address = IP.getHostAddress();

		Thread rmi_server_thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startRmiServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//rmi_server_thread.start(); // Rmi server start
		startRmiServer();
		multicastJoining(); // multicast message sent
		Thread.sleep(1000);
		if (!rmi_obj.other_exist) {
			System.out.println("Others not exist!! I'm lonely :P!!");
			rmi_obj.message_queue.remove(rmi_obj.getMessage(client_id, 0));
			rmi_obj.all_message_configs.remove(rmi_obj.getMessageConfig(client_id, 0));
			Thread multicast_server_thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						multicastServer();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});
			multicast_server_thread.run();
		}
		Scanner scn = new Scanner(System.in);
		while (true) {
			System.out.print(">");
			String s = scn.nextLine();
			if (rmi_obj.number_of_clients == 0) {
				System.out.println(rmi_obj.client_id + ": " + s);
			} else {
				if (s.charAt(5) == 'T') {
					sendReply(s.substring(8));
				} else if (s.charAt(5) == 'o') {
					// control
				} else {
					sendReply(s.substring(6));
				}
			}
		}

	}

	public static void startRmiServer() throws Exception {
		try { // special exception handler for registry creation
			LocateRegistry.createRegistry(1099);
			System.out.println("java RMI registry created.");
		} catch (RemoteException e) {
			// do nothing, error means registry already exists
			System.out.println("java RMI registry already exists.");
		}

		// Instantiate RmiServer

		rmi_obj = new RmiServer(client_id, ip_address);

		// Bind this object instance to the name "RmiServer"
		Naming.rebind("//" + ip_address + "/" + client_id, rmi_obj);
		System.out.println("PeerServer bound in registry : " + ip_address + " "
				+ client_id);
	}

	public static void multicastJoining() throws Exception {
		InetAddress addr = InetAddress.getByName(INET_ADDR);
		try (DatagramSocket serverSocket = new DatagramSocket()) {

			// Create a packet that will contain the data
			// (in the form of bytes) and send it.
			System.out.println("Server preparing to send multicast packet");
			MessageConfig msg_config = new MessageConfig();
			msg_config.sender_id = client_id;
			msg_config.seq_number = 0;
			rmi_obj.all_message_configs.add(msg_config);

			Message msg = new Message(client_id, 0, ip_address, 0);

			rmi_obj.message_queue.add(msg);

			DatagramPacket msgPacket = new DatagramPacket(msg.toString()
					.getBytes(), msg.toString().getBytes().length, addr, PORT);
			serverSocket.send(msgPacket);

			System.out.println("Server sent packet with msg: " + msg);
		}
	}

	public static void multicastServer() throws Exception {
		InetAddress address = InetAddress.getByName(INET_ADDR);
		byte[] buf = new byte[256];
		try (MulticastSocket clientSocket = new MulticastSocket(PORT)) {
			// Joint the Multicast group.
			clientSocket.joinGroup(address);
			while (true) {
				// Receive the information and print it.
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				clientSocket.receive(msgPacket);

				String m = new String(buf, 0, buf.length);
				// message using rmi
				String[] parts = m.split(" ");
				Message msg = new Message(parts[0], 0, parts[2], 0);
				System.out.println(client_id + " Received " + msg);
				welcomeClient(msg);
				
			}
		}
	}

	public static void welcomeClient(Message msg) {
		try {
			if (rmi_obj.global_timestamp > msg.timestamp) {
				msg.timestamp = rmi_obj.global_timestamp;
			} else {
				rmi_obj.global_timestamp = msg.timestamp;
			}
			rmi_obj.message_queue.add(msg);
			rmi_obj.checkPrint();
			System.out.println(client_id + " welcome_client " + msg );
			RmiServerIntf obj = (RmiServerIntf) Naming.lookup("//" + msg.data
					+ "/" + msg.sender_id);
			obj.welcomeMessage(client_id, rmi_obj.number_of_clients,
					msg.timestamp, ip_address);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sendReply(String m) {
		rmi_obj.global_timestamp += 1;
		rmi_obj.current_sequence_number += 1;
		MessageConfig msg_config = new MessageConfig();
		msg_config.sender_id = client_id;
		msg_config.seq_number = rmi_obj.current_sequence_number;
		msg_config.makeMap(rmi_obj.all_clients.keySet());
		rmi_obj.all_message_configs.add(msg_config);

		Message msg = new Message(client_id, rmi_obj.current_sequence_number,
				m, rmi_obj.global_timestamp);
		rmi_obj.message_queue.add(msg);

		for (Map.Entry<String, String> recv : rmi_obj.all_clients.entrySet()) {
			try {
				RmiServerIntf obj = (RmiServerIntf) Naming.lookup("//"
						+ recv.getValue() + "/" + recv.getKey());
				obj.normalMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

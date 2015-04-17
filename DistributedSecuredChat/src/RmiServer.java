import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {

	User me;

	public RmiServer() throws RemoteException {
		super(0);
		this.me = DistributedSecuredChat.me;
	}

	// Called in parent
	@Override
	public void joinMessage(User child) throws RemoteException {
		System.out.println(me.id + "> Adding child " + child.id);
		me.children.add(child);
		try {
			RmiServerIntf child_rmi_obj = DistributedSecuredChat
					.getRmiObject(child);
			child_rmi_obj.joinConfirmMessage(me);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Called in child
	@Override
	public void joinConfirmMessage(User parent) throws RemoteException {
		System.out
				.println(me.id + "> Adding parent after confirm " + parent.id);
		this.me.parent = parent;
	}

	// Called by sender
	@Override
	public void flood(User destination, User source, User sender,
			Message message) throws RemoteException {
		if (destination.equals(me)) {
			if (message.type == MessageType.Data) {
				String msg = "";
				try {
					msg = DistributedSecuredChat.decrypt(message.msg,
							me.private_key);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(source.id + ": " + msg);
			} else if (message.type == MessageType.PublicKeyRequest) {
				RmiServerIntf sender_rmi_obj;
				try {
					sender_rmi_obj = DistributedSecuredChat
							.getRmiObject(sender);
					message.type = MessageType.PublicKeyReply;
					System.out.println("My public key "
							+ me.public_key.toString());
					sender_rmi_obj.flood(source, me, me, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.PublicKeyReply
					&& message.msg.equals("Simple message")) {
				try {
					System.out.print("Message? ");
					String data = DistributedSecuredChat.scanner.nextLine();
					String encrypted_data = DistributedSecuredChat.encrypt(
							data, source.public_key);
					System.out.println("My public key "
							+ source.public_key.toString());
					// User destination = new User(destination_id, "", null);
					Message msg = new Message(MessageType.Data, encrypted_data);

					DistributedSecuredChat.rmi_obj.flood(source, me, me, msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.PublicKeyReply
					&& message.msg.equals("Add group")) {
				try {
					System.out.println("Group Name? ");
					String group_name = DistributedSecuredChat.scanner
							.nextLine();
					Group g = new Group(group_name, null, null);
					Group add_g = me.membership.get(me.membership.indexOf(g));
					MyKeyPair pair = new MyKeyPair(add_g.public_key,
							add_g.private_key, add_g.id);
					String data = DistributedSecuredChat.toString(pair);
					String encrypted_data = DistributedSecuredChat.encrypt(
							data, source.public_key);
					Message msg = new Message(MessageType.GroupKey,
							encrypted_data);
					DistributedSecuredChat.rmi_obj.flood(source, me, me, msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.GroupKey) {
				String msg = "";
				try {
					msg = DistributedSecuredChat.decrypt(message.msg,
							me.private_key);
					MyKeyPair pair = (MyKeyPair) DistributedSecuredChat
							.fromString(msg);
					Group new_group = new Group(pair.group_id, pair.public_key, pair.private_key);
					me.membership.add(new_group);
					System.out.println("Added to " + pair.group_id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} else {
			boolean destination_found = false;
			if ((me.parent != null) && (me.parent.equals(destination))) {
				RmiServerIntf parent_rmi_obj;
				try {
					parent_rmi_obj = DistributedSecuredChat
							.getRmiObject(me.parent);
					parent_rmi_obj.flood(destination, source, me, message);
					destination_found = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				for (User child : me.children) {
					if (child.equals(destination)) {
						RmiServerIntf child_rmi_obj;
						try {
							child_rmi_obj = DistributedSecuredChat
									.getRmiObject(child);
							child_rmi_obj.flood(destination, source, me,
									message);
							destination_found = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (!destination_found) {
				if ((me.parent != null) && (!me.parent.equals(sender))) {
					RmiServerIntf parent_rmi_obj;
					try {
						parent_rmi_obj = DistributedSecuredChat
								.getRmiObject(me.parent);
						parent_rmi_obj.flood(destination, source, me, message);
						destination_found = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				for (User child : me.children) {
					if (!child.equals(sender)) {
						RmiServerIntf child_rmi_obj;
						try {
							child_rmi_obj = DistributedSecuredChat
									.getRmiObject(child);
							child_rmi_obj.flood(destination, source, me,
									message);
							destination_found = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void group_flood(Group destination, User source, User sender,
			Message message) throws RemoteException {
		if (me.membership.contains(destination)) {
			if (message.type == MessageType.Data) {
				for (Group g : me.membership) {
					if (g.equals(destination)) {
						String msg = "";
						try {
							msg = DistributedSecuredChat.decrypt(message.msg,
									g.private_key);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println(source.id + " in " + g.id + ": "
								+ msg);
						break;
					}
				}
			}
		}

		if ((me.parent != null) && (!me.parent.equals(sender))) {
			RmiServerIntf parent_rmi_obj;
			try {
				parent_rmi_obj = DistributedSecuredChat.getRmiObject(me.parent);
				parent_rmi_obj.group_flood(destination, source, me, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (User child : me.children) {
			if (!child.equals(sender)) {
				RmiServerIntf child_rmi_obj;
				try {
					child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
					child_rmi_obj.group_flood(destination, source, me, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
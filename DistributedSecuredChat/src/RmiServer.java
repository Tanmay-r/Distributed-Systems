import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.crypto.spec.SecretKeySpec;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {

	private static final long serialVersionUID = 1L;
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
			RmiServerIntf child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
			child_rmi_obj.joinConfirmMessage(me);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Called in child
	@Override
	public void joinConfirmMessage(User parent) throws RemoteException {
		System.out.println(me.id + "> Adding parent after confirm " + parent.id);
		this.me.parent = parent;
	}

	// Called by sender
	@Override
	public void flood(User destination, User source, User sender, Message message)
			throws RemoteException {
		if (destination.equals(me)) {
			if (message.type == MessageType.Data) {
				String msg = "";
				try {
					byte[] aesKeyBytes = DistributedSecuredChat
							.decrypt(message.key, me.private_key);
					SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length,
							"AES");
					msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println(source.id + ": " + msg);
			} else if (message.type == MessageType.PublicKeyRequest) {
				RmiServerIntf sender_rmi_obj;
				try {
					sender_rmi_obj = DistributedSecuredChat.getRmiObject(sender);
					message.type = MessageType.PublicKeyReply;
					sender_rmi_obj.flood(source, me, me, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.PublicKeyReply) {
				try {
					String m = new String(message.msg, "UTF8");
					if (m.equals("Simple message")) {
						System.out.print("Message? ");
						String data = DistributedSecuredChat.scanner.nextLine();
						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(data, spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, source.public_key);
						Message msg = new Message(MessageType.Data, aesKey, encrypted_data);

						DistributedSecuredChat.rmi_obj.flood(source, me, me, msg);
					} else if (m.equals("Add group")) {
						System.out.println("Group Name? ");
						String group_name = DistributedSecuredChat.scanner.nextLine();
						Group g = new Group(group_name, null, null);
						Group add_g = me.membership.get(me.membership.indexOf(g));
						MyKeyPair pair = new MyKeyPair(add_g.public_key, add_g.private_key,
								add_g.id);
						String data = DistributedSecuredChat.toString(pair);

						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(data, spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, source.public_key);
						Message msg = new Message(MessageType.GroupKey, aesKey, encrypted_data);

						DistributedSecuredChat.rmi_obj.flood(source, me, me, msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.GroupKey) {
				String msg = "";
				try {
					byte[] aesKeyBytes = DistributedSecuredChat
							.decrypt(message.key, me.private_key);
					SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length,
							"AES");
					msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
					MyKeyPair pair = (MyKeyPair) DistributedSecuredChat.fromString(msg);
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
					parent_rmi_obj = DistributedSecuredChat.getRmiObject(me.parent);
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
							child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
							child_rmi_obj.flood(destination, source, me, message);
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
						parent_rmi_obj = DistributedSecuredChat.getRmiObject(me.parent);
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
							child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
							child_rmi_obj.flood(destination, source, me, message);
							destination_found = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void group_flood(Group destination, User source, User sender, Message message)
			throws RemoteException {
		if (me.membership.contains(destination)) {
			if (message.type == MessageType.Data) {
				for (Group g : me.membership) {
					if (g.equals(destination)) {
						String msg = "";
						try {
							byte[] aesKeyBytes = DistributedSecuredChat.decrypt(message.key,
									g.private_key);
							SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0,
									aesKeyBytes.length, "AES");
							msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println(source.id + " in " + g.id + ": " + msg);
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
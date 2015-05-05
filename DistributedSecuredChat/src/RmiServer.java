import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {

	private static final long serialVersionUID = 1L;

	public RmiServer() throws RemoteException {
		super(0);
	}

	// Called in parent
	@Override
	public void joinMessage(User child) throws RemoteException {
		System.out.println(DistributedSecuredChat.me.id + "> Adding child " + child.id);
		DistributedSecuredChat.me.children.add(child);
		try {
			RmiServerIntf child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
			child_rmi_obj.joinConfirmMessage(DistributedSecuredChat.me);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Called in child
	@Override
	public void joinConfirmMessage(User parent) throws RemoteException {
		System.out.println(DistributedSecuredChat.me.id + "> Adding parent after confirm "
				+ parent.id);
		DistributedSecuredChat.me.parent = parent;
	}

	// Called by sender
	@Override
	public void flood(User destination, User source, User sender, Message message)
			throws RemoteException {
		if (destination.equals(DistributedSecuredChat.me)) {
			if (message.type == MessageType.Data) {
				String msg = "";
				try {
					byte[] aesKeyBytes = DistributedSecuredChat.decrypt(message.key,
							DistributedSecuredChat.me.private_key);
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
					sender_rmi_obj.flood(source, DistributedSecuredChat.me,
							DistributedSecuredChat.me, message);
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

						DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
								DistributedSecuredChat.me, msg);
					} else if (m.equals("Add group")) {
						System.out.println("Group Name? ");
						String group_name = DistributedSecuredChat.scanner.nextLine();
						Group g = new Group(group_name, null, null);
						Group add_g = DistributedSecuredChat.me.membership
								.get(DistributedSecuredChat.me.membership.indexOf(g));
						MyKeyPair pair = new MyKeyPair(add_g.public_key, add_g.private_key,
								add_g.id);
						String data = DistributedSecuredChat.toString(pair);

						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(data, spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, source.public_key);
						Message msg = new Message(MessageType.GroupKey, aesKey, encrypted_data);

						DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
								DistributedSecuredChat.me, msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.GroupKey) {
				String msg = "";
				try {
					byte[] aesKeyBytes = DistributedSecuredChat.decrypt(message.key,
							DistributedSecuredChat.me.private_key);
					SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length,
							"AES");
					msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
					MyKeyPair pair = (MyKeyPair) DistributedSecuredChat.fromString(msg);
					Group new_group = new Group(pair.group_id, pair.public_key, pair.private_key);
					new_group.disabled = false;
					DistributedSecuredChat.me.membership.add(new_group);
					System.out.println("Added to " + pair.group_id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.type == MessageType.LeaderGroupReply) {
				try {
					String group_name = new String(message.msg, "UTF8");
					Group g = new Group(group_name, null, null);
					Group add_g = DistributedSecuredChat.me.membership
							.get(DistributedSecuredChat.me.membership.indexOf(g));
					MyKeyPair pair = new MyKeyPair(add_g.public_key, add_g.private_key, add_g.id);
					String data = DistributedSecuredChat.toString(pair);

					SecretKeySpec spec = DistributedSecuredChat.makeKey();

					byte[] encrypted_data = DistributedSecuredChat.encryptMessage(data, spec);
					byte[] aesKey = DistributedSecuredChat.encrypt(spec, source.public_key);
					Message msg = new Message(MessageType.GroupKey, aesKey, encrypted_data);
					System.out.println("I'm leader of group " + g.id + " resending group add");
					DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
							DistributedSecuredChat.me, msg);
				} catch (Exception e) {
				}
			} else if (message.type == MessageType.ChildLeavingNetwork) {
				try {
					byte[] aesKeyBytes = DistributedSecuredChat.decrypt(message.key,
							DistributedSecuredChat.me.private_key);
					SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length,
							"AES");
					String msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
					ArrayList<User> children = (ArrayList<User>) DistributedSecuredChat
							.fromString(msg);
					for (User child : children) {
						DistributedSecuredChat.me.children.add(child);
					}
					DistributedSecuredChat.me.children.remove(source);
				} catch (Exception e) {
				}
			} else if (message.type == MessageType.ParentLeavingNetwork) {
				try {
					byte[] aesKeyBytes = DistributedSecuredChat.decrypt(message.key,
							DistributedSecuredChat.me.private_key);
					SecretKeySpec spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length,
							"AES");
					String msg = DistributedSecuredChat.decryptMessage(message.msg, spec);
					User parent = (User) DistributedSecuredChat.fromString(msg);
					DistributedSecuredChat.me.parent = parent;
				} catch (Exception e) {
				}
			} else if (message.type == MessageType.TokenPollReply) {
				try {
					String group_name = new String(message.msg, "UTF8");
					Group g = new Group(group_name, null, null);
					if (DistributedSecuredChat.me.membership.indexOf(g) != -1) {
						Group add_g = DistributedSecuredChat.me.membership
								.get(DistributedSecuredChat.me.membership.indexOf(g));
						DistributedSecuredChat.me.membership.remove(add_g);

						Message msg = new Message(MessageType.TokenPass, null, add_g.id);
						System.out.println("I was leader of group " + g.id
								+ " but I'm leaving,sending group token");
						DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
								DistributedSecuredChat.me, msg);
					}
				} catch (Exception e) {
				}
			} else if (message.type == MessageType.TokenPass) {
				try {
					String group_name = new String(message.msg, "UTF8");
					Group g = new Group(group_name, null, null);
					if (DistributedSecuredChat.me.membership.indexOf(g) != -1) {
						Group add_g = DistributedSecuredChat.me.membership
								.get(DistributedSecuredChat.me.membership.indexOf(g));
						DistributedSecuredChat.me.membership.remove(add_g);
						add_g.token = true;

						KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
						kpg.initialize(1024); // 512 is the keysize.
						KeyPair kp = kpg.generateKeyPair();
						add_g.public_key = kp.getPublic();
						add_g.private_key = kp.getPrivate();

						DistributedSecuredChat.me.membership.add(add_g);
						Message msg = new Message(MessageType.LeaderAnnounce, null, add_g.id);
						System.out.println("I'm new leader of group " + g.id
								+ " resending group add");
						DistributedSecuredChat.rmi_obj.group_flood(add_g,
								DistributedSecuredChat.me, DistributedSecuredChat.me, msg);
					}
				} catch (Exception e) {
				}
			}

		} else {
			boolean destination_found = false;
			if ((DistributedSecuredChat.me.parent != null)
					&& (DistributedSecuredChat.me.parent.equals(destination))) {
				RmiServerIntf parent_rmi_obj;
				try {
					parent_rmi_obj = DistributedSecuredChat
							.getRmiObject(DistributedSecuredChat.me.parent);
					parent_rmi_obj.flood(destination, source, DistributedSecuredChat.me, message);
					destination_found = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				for (User child : DistributedSecuredChat.me.children) {
					if (child.equals(destination)) {
						RmiServerIntf child_rmi_obj;
						try {
							child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
							child_rmi_obj.flood(destination, source, DistributedSecuredChat.me,
									message);
							destination_found = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (!destination_found) {
				if ((DistributedSecuredChat.me.parent != null)
						&& (!DistributedSecuredChat.me.parent.equals(sender))) {
					RmiServerIntf parent_rmi_obj;
					try {
						parent_rmi_obj = DistributedSecuredChat
								.getRmiObject(DistributedSecuredChat.me.parent);
						parent_rmi_obj.flood(destination, source, DistributedSecuredChat.me,
								message);
						destination_found = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				for (User child : DistributedSecuredChat.me.children) {
					if (!child.equals(sender)) {
						RmiServerIntf child_rmi_obj;
						try {
							child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
							child_rmi_obj.flood(destination, source, DistributedSecuredChat.me,
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

	public void group_flood(Group destination, User source, User sender, Message message)
			throws RemoteException {
		if (DistributedSecuredChat.me.membership.contains(destination)) {
			if (message.type == MessageType.Data) {
				for (Group g : DistributedSecuredChat.me.membership) {
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
			} else if (message.type == MessageType.LeaveGroup) {
				for (Group g : DistributedSecuredChat.me.membership) {
					if (g.equals(destination)) {
						if (g.token) {
							try {
								System.out.println("I'm leader of group " + g.id
										+ " sending leader announce");
								Message msg = new Message(MessageType.LeaderAnnounce, null, "");
								KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
								kpg.initialize(1024); // 512 is the keysize.
								KeyPair kp = kpg.generateKeyPair();
								g.public_key = kp.getPublic();
								g.private_key = kp.getPrivate();

								DistributedSecuredChat.rmi_obj.group_flood(g,
										DistributedSecuredChat.me, DistributedSecuredChat.me, msg);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							g.disabled = true;
						}
						break;
					}
				}
			} else if (message.type == MessageType.LeaderAnnounce) {
				for (Group g : DistributedSecuredChat.me.membership) {
					if (g.equals(destination)) {
						if (!g.token) {
							System.out.println("I'm not leader of group " + g.id
									+ " receiving leader announce");
							Message msg = new Message(MessageType.LeaderGroupReply, null, g.id);
							DistributedSecuredChat.me.membership.remove(g);
							DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
									DistributedSecuredChat.me, msg);
						}
						break;
					}
				}
			} else if (message.type == MessageType.TokenPoll) {
				for (Group g : DistributedSecuredChat.me.membership) {
					if (g.equals(destination)) {
						if (!g.token) {
							System.out.println("I'm not leader of group " + g.id
									+ " receiving Token Poll");
							Message msg = new Message(MessageType.TokenPollReply, null, g.id);
							DistributedSecuredChat.rmi_obj.flood(source, DistributedSecuredChat.me,
									DistributedSecuredChat.me, msg);
						}
						break;
					}
				}
			}
		}

		if ((DistributedSecuredChat.me.parent != null)
				&& (!DistributedSecuredChat.me.parent.equals(sender))) {
			RmiServerIntf parent_rmi_obj;
			try {
				parent_rmi_obj = DistributedSecuredChat
						.getRmiObject(DistributedSecuredChat.me.parent);
				parent_rmi_obj.group_flood(destination, source, DistributedSecuredChat.me, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (User child : DistributedSecuredChat.me.children) {
			if (!child.equals(sender)) {
				RmiServerIntf child_rmi_obj;
				try {
					child_rmi_obj = DistributedSecuredChat.getRmiObject(child);
					child_rmi_obj.group_flood(destination, source, DistributedSecuredChat.me,
							message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
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
				String msg="";
				try {
					msg = DistributedSecuredChat.decrypt(message.msg, me.private_key);
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
					sender_rmi_obj.flood(source, me, me, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					System.out.print("Message? ");
					String data = DistributedSecuredChat.scanner.nextLine();
					String encrypted_data = DistributedSecuredChat.encrypt(
							data, source.public_key);
					// User destination = new User(destination_id, "", null);
					Message msg = new Message(MessageType.Data, encrypted_data);

					DistributedSecuredChat.rmi_obj.flood(source, me, me, msg);
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

}
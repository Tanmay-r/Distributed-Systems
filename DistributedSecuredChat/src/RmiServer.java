import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RmiServer extends UnicastRemoteObject implements RmiServerIntf {

	User me;

	public RmiServer()
			throws RemoteException {
		super(0);
		this.me = DistributedSecuredChat.me;
	}

	//Called in parent
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

	//Called in child
	@Override
	public void joinConfirmMessage(User parent) throws RemoteException {
		System.out.println(me.id + "> Adding parent after confirm " + parent.id);
		this.me.parent = parent;
	}

}
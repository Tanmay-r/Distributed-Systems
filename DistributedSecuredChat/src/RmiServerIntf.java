
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerIntf extends Remote {
	public void joinMessage(User child) throws RemoteException;
	public void joinConfirmMessage(User parent) throws RemoteException;
	public void flood(User destination, User source, User sender, Message message) throws RemoteException;
	public void group_flood(Group destination, User source, User sender, Message message) throws RemoteException;
}
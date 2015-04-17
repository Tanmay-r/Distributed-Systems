
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerIntf extends Remote {
	public void joinMessage(User child) throws RemoteException;
	public void joinConfirmMessage(User parent) throws RemoteException;
	

}
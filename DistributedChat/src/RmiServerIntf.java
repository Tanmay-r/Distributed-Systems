
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerIntf extends Remote {
	public void welcomeMessage(String client_id, int number_of_clients,
			int timestamp) throws RemoteException;

	public void globalMessage(String client_id, int seq_no, int timestamp)
			throws RemoteException;

	public void normalMessage(Message msg) throws RemoteException;

	public void ackMessage(String client_id, int seq_no, int timestamp)
			throws RemoteException;

}
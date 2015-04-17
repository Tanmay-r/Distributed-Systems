import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class DistributedSecuredChat {
	static User me;
	static RmiServer rmi_obj = null;
	public static boolean is_closed = false;
	static Scanner scanner;
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.print("Incorrect Use : Give client unique id");
			return;
		}

		me = new User();
		scanner = new Scanner(System.in);
		me.id = args[0];
		try {
			me.ip = chooseIP();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		startRmiServer();
		System.out.println("Hello!!" + me.id + " " + me.ip);
		System.out.println("To join chat");
		
		while (true) {
			System.out.print("Specify your role ([r]oot/[c]lient)");
			String input = scanner.nextLine();
			if (input.startsWith("r")) {
				break;
			} else if (input.startsWith("c")) {
				System.out.print("Specify your parent's username ip-address");
				input = scanner.nextLine();
				String[] brk_str = input.split(" ");
				if (brk_str.length == 2) {
					me.parent = new User();
					me.parent.id = brk_str[0];
					me.parent.ip = brk_str[1];
					RmiServerIntf rmi_obj = getRmiObject(me.parent);
					rmi_obj.joinMessage(me);
					break;
				}
			}
			System.out.println("Wrong input!!!");

		}
		while (true) {
			System.out.print("Destination? ");
			String destination_id = scanner.nextLine();
			System.out.print("Message? ");
			String message = scanner.nextLine();
			User destination = new User(destination_id, "", "");
			rmi_obj.flood(destination, me, me, message);
		}
		//scanner.close();

	}

	public static void startRmiServer() throws Exception {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
		}

		rmi_obj = new RmiServer();
		Naming.rebind("//" + me.ip + "/" + me.id, rmi_obj);
	}

	public static RmiServerIntf getRmiObject(User user) throws Exception {
		RmiServerIntf rmi_obj = (RmiServerIntf) Naming.lookup("//" + user.ip
				+ "/" + user.id);
		return rmi_obj;

	}

	public static String chooseIP() throws Exception {
		int index = 0;
		ArrayList<String> ips = new ArrayList<String>();
		Enumeration<NetworkInterface> e = NetworkInterface
				.getNetworkInterfaces();
		System.out.println("Choose one of the following ip addresses:");
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				System.out.println("[" + index + "] " + i.getHostAddress());
				ips.add(i.getHostAddress());
				index += 1;
			}
		}

		while (true) {
			System.out.print("Choose [0-" + (ips.size() - 1) + "]: ");
			int choice = scanner.nextInt();
			if (choice < ips.size() && choice > 0) {
				scanner.nextLine();
				return ips.get(choice);
			}
		}
	}
}

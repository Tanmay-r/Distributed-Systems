import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

import javax.crypto.Cipher;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

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
		
		String xform = "RSA";
	    // Generate a key-pair
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    kpg.initialize(512); // 512 is the keysize.
	    KeyPair kp = kpg.generateKeyPair();
	    PublicKey pubk = kp.getPublic();
	    PrivateKey prvk = kp.getPrivate();
	    
		me = new User();
		scanner = new Scanner(System.in);
		me.id = args[0];
		me.public_key = pubk;
		me.private_key = prvk;
		
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
			System.out.println("Choose Action: ");
			System.out.println("\t[0] Send Personal Message");
			System.out.println("\t[1] Send Group Message");
			System.out.println("\t[2] Create Group");
			System.out.println("\t[3] Add Member");
			System.out.println("\t[4] Leave Group");
			System.out.println("\t[5] Leave Network");	
			int action = scanner.nextInt();
			switch(action){
			case 0: {
				System.out.print("Destination? ");
				String destination_id = scanner.nextLine();
				User destination = new User(destination_id, "", null);
				Message message = new Message(MessageType.PublicKeyRequest,"Simple message");
				rmi_obj.flood(destination, me, me, message);
				break;
			}
			case 1: {
				System.out.print("Group? ");
				String destination_id = scanner.nextLine();
				Group destination = new Group(destination_id, null, null);
				for (Group g : me.membership) {
					if(g.equals(destination)){
						System.out.print("Message? ");
						String msg = scanner.nextLine();
						msg = DistributedSecuredChat.encrypt(
								msg, g.public_key);
						Message message = new Message(MessageType.Data,msg);
						rmi_obj.group_flood(g, me, me, message);
					}
					break;
				}
			}
			case 2: {
				System.out.print("Group Name? ");
				String group_id = scanner.nextLine();
				// Generate a key-pair
			    kpg = KeyPairGenerator.getInstance("RSA");
			    kpg.initialize(512); // 512 is the keysize.
			    kp = kpg.generateKeyPair();
			    pubk = kp.getPublic();
			    prvk = kp.getPrivate();
				Group new_group = new Group(group_id, pubk, prvk);
				me.membership.add(new_group);
			}
			case 3:{
				System.out.print("Name? ");
				String member_id = scanner.nextLine();
				User member = new User(member_id, "", null);
				Message message = new Message(MessageType.PublicKeyRequest,"Add group");
				rmi_obj.flood(member, me, me, message);
			}
			default:{
				System.out.println("Wrong!");
				break;
			}				
			}
			
			
			
			
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
	
	public static String encrypt(String inp, PublicKey key) throws Exception {
	    byte[] inpBytes = inp.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] cipherData = cipher.doFinal(inpBytes);
	    return new String(cipherData,"UTF8");
	}
	
	public static String decrypt(String inp, PrivateKey key) throws Exception{
		byte[] inpBytes = inp.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("RSA");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] cipherData = cipher.doFinal(inpBytes);
	    return new String(cipherData,"UTF8");
	}
	
	 /** Read the object from Base64 string. 
	 * @throws Base64DecodingException */
	   public static Object fromString( String s ) throws IOException ,
	                                                       ClassNotFoundException, Base64DecodingException {
	        byte [] data = Base64.decode( s );
	        ObjectInputStream ois = new ObjectInputStream( 
	                                        new ByteArrayInputStream(  data ) );
	        Object o  = ois.readObject();
	        ois.close();
	        return o;
	   }

	    /** Write the object to a Base64 string. */
	    public static String toString( Serializable o ) throws IOException {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( o );
	        oos.close();
	        return new String( Base64.encode( baos.toByteArray() ) );
	    }
}

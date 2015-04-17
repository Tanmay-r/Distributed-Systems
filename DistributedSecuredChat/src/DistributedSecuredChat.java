import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DistributedSecuredChat {
	static User me;
	static RmiServer rmi_obj = null;
	public static boolean is_closed = false;
	public static final int AES_Key_Size = 256;
	static byte[] aesKey;
	static Cipher pkCipher, aesCipher;
	static Scanner scanner;
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.print("Incorrect Use : Give client unique id");
			return;
		}
		
		
		
	    // Generate a key-pair
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	    kpg.initialize(1024,random); 
	    KeyPair kp = kpg.generateKeyPair();
	    PublicKey pubk = kp.getPublic();
	    PrivateKey prvk = kp.getPrivate();
	    System.out.println(kp.getPublic().getEncoded().length);
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
			System.out.print("Destination? ");
			String destination_id = scanner.nextLine();
			User destination = new User(destination_id, "", null);
			Message message = new Message(MessageType.PublicKeyRequest,null, "");
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
	
	public static String encrypt(SecretKeySpec inp, PublicKey key) throws Exception {
	    byte[] inpBytes = inp.getEncoded();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] cipherData = cipher.doFinal(inpBytes);
	    return new String(cipherData, "UTF8");
	}
	
	public static byte[] decrypt(byte[] inpBytes, PrivateKey key) throws Exception{
		
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    System.out.println(key.getEncoded().length);
	    System.out.println(inpBytes.length);
	    return cipher.doFinal(inpBytes);
	    
	}
	
	public static SecretKeySpec makeKey() throws NoSuchAlgorithmException{
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_Key_Size);
	    SecretKey key = kgen.generateKey();
	    aesKey = key.getEncoded();
	    SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
	    return aesKeySpec;
	}

	public static String encryptMessage(String inp, SecretKeySpec spec) throws Exception {
		byte[] inpBytes = inp.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		byte[] cipherData = cipher.doFinal(inpBytes);
		return new String(cipherData,"UTF8");
	}
	
	public static String decryptMessage(String inp, SecretKeySpec spec) throws Exception {
		byte[] inpBytes = inp.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, spec);
		byte[] cipherData = cipher.doFinal(inpBytes);
		return new String(cipherData,"UTF8");
	}
}

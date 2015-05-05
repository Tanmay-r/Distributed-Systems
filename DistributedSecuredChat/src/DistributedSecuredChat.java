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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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
		kpg.initialize(1024, random);
		KeyPair kp = kpg.generateKeyPair();
		PublicKey pubk = kp.getPublic();
		PrivateKey prvk = kp.getPrivate();
		System.out.println(kp.getPublic().getEncoded().length);
		me = new User();
		scanner = new Scanner(System.in);
		me.id = args[0];
		me.public_key = pubk;
		me.private_key = prvk;

		// SecretKeySpec spec = makeKey();
		// byte[] encrypted_data = encryptMessage("hellofjdfjd", spec);
		// byte[] aesKeyBytes = decrypt(encrypt(spec, pubk),prvk);
		// spec = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
		// String data = decryptMessage(encrypted_data, spec);
		// System.out.println(data);

		try {
			me.ip = chooseIP();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		startRmiServer();
		System.out.println("Hello!!" + me.id + " " + me.ip);
		System.out.println("To join chat");
		Boolean in_network = true;
		while (in_network) {
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
			scanner.nextLine();
			switch (action) {
			case 0: {
				System.out.print("Destination? ");
				String destination_id = scanner.nextLine();
				User destination = new User(destination_id, "", null);
				Message message = new Message(MessageType.PublicKeyRequest, null, "Simple message");
				rmi_obj.flood(destination, me, me, message);
				break;
			}
			case 1: {
				System.out.print("Group? ");
				String destination_id = scanner.nextLine();
				Group destination = new Group(destination_id, null, null);
				for (Group g : me.membership) {
					System.out.println("Group id " + g.id + " " + g.disabled + " " + g.token);
					if (g.equals(destination)) {
						System.out.print("Message? ");
						String msg = scanner.nextLine();
						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(msg, spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, g.public_key);
						Message message = new Message(MessageType.Data, aesKey, encrypted_data);

						rmi_obj.group_flood(g, me, me, message);
						break;
					}
				}
				break;
			}
			case 2: {
				System.out.print("Group Name? ");
				String group_id = scanner.nextLine();
				// Generate a key-pair
				kpg = KeyPairGenerator.getInstance("RSA");
				kpg.initialize(1024); // 512 is the keysize.
				kp = kpg.generateKeyPair();
				pubk = kp.getPublic();
				prvk = kp.getPrivate();
				Group new_group = new Group(group_id, pubk, prvk);
				new_group.token = true;
				me.membership.add(new_group);
				break;
			}
			case 3: {
				System.out.print("Name? ");
				String member_id = scanner.nextLine();
				User member = new User(member_id, "", null);
				Message message = new Message(MessageType.PublicKeyRequest, null, "Add group");
				rmi_obj.flood(member, me, me, message);
				break;
			}
			case 4: {
				System.out.print("Group Name? ");
				String destination_id = scanner.nextLine();
				Group destination = new Group(destination_id, null, null);
				for (Group g : me.membership) {
					if (g.equals(destination)) {
						if (!g.token) {
							SecretKeySpec spec = DistributedSecuredChat.makeKey();

							byte[] encrypted_data = DistributedSecuredChat.encryptMessage(me.id,
									spec);
							byte[] aesKey = DistributedSecuredChat.encrypt(spec, g.public_key);
							Message message = new Message(MessageType.LeaveGroup, aesKey,
									encrypted_data);
							me.membership.remove(g);
							rmi_obj.group_flood(g, me, me, message);
						} else {
							SecretKeySpec spec = DistributedSecuredChat.makeKey();

							byte[] encrypted_data = DistributedSecuredChat.encryptMessage(me.id,
									spec);
							byte[] aesKey = DistributedSecuredChat.encrypt(spec, g.public_key);
							Message message = new Message(MessageType.TokenPoll, aesKey,
									encrypted_data);
							rmi_obj.group_flood(g, me, me, message);
						}

						break;
					}
				}
				break;
			}
			case 5: {
				for (Group g : me.membership) {
					if (!g.token) {
						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(me.id,
								spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, g.public_key);
						Message message = new Message(MessageType.LeaveGroup, aesKey,
								encrypted_data);
						me.membership.remove(g);
						rmi_obj.group_flood(g, me, me, message);
					} else {
						SecretKeySpec spec = DistributedSecuredChat.makeKey();

						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(me.id,
								spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, g.public_key);
						Message message = new Message(MessageType.TokenPoll, aesKey,
								encrypted_data);
						rmi_obj.group_flood(g, me, me, message);
					}
				}
				if (me.parent == null) {
					if(me.children.size() > 0){
						User new_parent = me.children.get(0);
						me.children.remove(0);
						String msg = toString(me.children);
						SecretKeySpec spec = DistributedSecuredChat.makeKey();
	
						byte[] encrypted_data = DistributedSecuredChat.encryptMessage(msg, spec);
						byte[] aesKey = DistributedSecuredChat.encrypt(spec, new_parent.public_key);
						Message message = new Message(MessageType.ChildLeavingNetwork, aesKey,
								encrypted_data);
	
						rmi_obj.flood(new_parent, me, me, message);
						for (User child : me.children) {
							msg = toString(new_parent);
							spec = DistributedSecuredChat.makeKey();
	
							encrypted_data = DistributedSecuredChat.encryptMessage(msg, spec);
							aesKey = DistributedSecuredChat.encrypt(spec, child.public_key);
							message = new Message(MessageType.ParentLeavingNetwork, aesKey,
									encrypted_data);
							rmi_obj.flood(child, me, me, message);
						}
					}
				} else {
					String msg = toString(me.children);
					SecretKeySpec spec = DistributedSecuredChat.makeKey();

					byte[] encrypted_data = DistributedSecuredChat.encryptMessage(msg, spec);
					byte[] aesKey = DistributedSecuredChat.encrypt(spec, me.parent.public_key);
					Message message = new Message(MessageType.ChildLeavingNetwork, aesKey,
							encrypted_data);

					rmi_obj.flood(me.parent, me, me, message);
					for (User child : me.children) {
						msg = toString(me.parent);
						spec = DistributedSecuredChat.makeKey();

						encrypted_data = DistributedSecuredChat.encryptMessage(msg, spec);
						aesKey = DistributedSecuredChat.encrypt(spec, child.public_key);
						message = new Message(MessageType.ParentLeavingNetwork, aesKey,
								encrypted_data);
						rmi_obj.flood(child, me, me, message);
					}
				}
				in_network = false;
				break;
			}
			default: {
				System.out.println("Wrong!");
				break;
			}
			}

		}
		// scanner.close();

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
		RmiServerIntf rmi_obj = (RmiServerIntf) Naming.lookup("//" + user.ip + "/" + user.id);
		return rmi_obj;

	}

	public static String chooseIP() throws Exception {
		int index = 0;
		ArrayList<String> ips = new ArrayList<String>();
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
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

	public static byte[] encrypt(SecretKeySpec inp, PublicKey key) throws Exception {
		byte[] inpBytes = inp.getEncoded();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherData = cipher.doFinal(inpBytes);
		return cipherData;
	}

	public static byte[] decrypt(byte[] inpBytes, PrivateKey key) throws Exception {

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		System.out.println(key.getEncoded().length);
		System.out.println(inpBytes.length);
		return cipher.doFinal(inpBytes);

	}

	public static SecretKeySpec makeKey() throws NoSuchAlgorithmException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_Key_Size);
		SecretKey key = kgen.generateKey();
		aesKey = key.getEncoded();
		SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
		return aesKeySpec;
	}

	public static byte[] encryptMessage(String inp, SecretKeySpec spec) throws Exception {
		byte[] inpBytes = inp.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		byte[] iv = new byte[cipher.getBlockSize()];
		IvParameterSpec ivParams = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, spec, ivParams);
		byte[] cipherData = cipher.doFinal(inpBytes);
		return cipherData;
	}

	public static String decryptMessage(byte[] inpBytes, SecretKeySpec spec) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		byte[] ivByte = new byte[cipher.getBlockSize()];
		IvParameterSpec ivParamsSpec = new IvParameterSpec(ivByte);

		cipher.init(Cipher.DECRYPT_MODE, spec, ivParamsSpec);
		byte[] cipherData = cipher.doFinal(inpBytes);
		return new String(cipherData, "UTF8");
	}

	/** Read the object from Base64 string. */
	public static Object fromString(String s) throws IOException, ClassNotFoundException {

		byte[] data = DatatypeConverter.parseBase64Binary(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	public static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}
}

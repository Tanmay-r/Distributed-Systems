import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
	String id;
	String ip;
	String public_key;
	transient String private_key;
	transient User parent;
	transient ArrayList<User> children;

	public User(String id, String ip, String public_key, String private_key,
			User parent, ArrayList<User> children) {
		this.id = id;
		this.ip = ip;
		this.public_key = public_key;
		this.private_key = private_key;
		this.parent = parent;
		this.children = children;
	}

	public User(String id, String ip, String public_key) {
		this.id = id;
		this.ip = ip;
		this.public_key = public_key;
	}

	public User() {
		this.parent = null;
		this.children = new ArrayList<User>();
	}

}

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	String id;
	String ip;
	PublicKey public_key;
	transient PrivateKey private_key;
	transient User parent;
	transient ArrayList<User> children;
	transient ArrayList<Group> membership;


	public User(String id, String ip, PublicKey public_key,
			PrivateKey private_key, User parent, ArrayList<User> children,
			ArrayList<Group> membership) {
		super();
		this.id = id;
		this.ip = ip;
		this.public_key = public_key;
		this.private_key = private_key;
		this.parent = parent;
		this.children = children;
		this.membership = membership;
	}

	public User(String id, String ip, PublicKey public_key) {
		this.id = id;
		this.ip = ip;
		this.public_key = public_key;
	}

	public User() {
		this.parent = null;
		this.children = new ArrayList<User>();
		this.membership = new ArrayList<Group>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

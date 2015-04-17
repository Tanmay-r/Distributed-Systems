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

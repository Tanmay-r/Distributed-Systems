import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Group implements Serializable {
	private static final long serialVersionUID = 1L;
	String id;
	transient PublicKey public_key;
	transient PrivateKey private_key;

	public Group(String id, PublicKey public_key, PrivateKey private_key) {
		super();
		this.id = id;
		this.public_key = public_key;
		this.private_key = private_key;
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
		Group other = (Group) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MyKeyPair implements Serializable{
	PublicKey public_key;
	PrivateKey private_key;
	String group_id;
	
	public MyKeyPair(PublicKey public_key, PrivateKey private_key,
			String group_id) {
		super();
		this.public_key = public_key;
		this.private_key = private_key;
		this.group_id = group_id;
	}

}

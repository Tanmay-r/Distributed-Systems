/*
 * Automatically generated by jrpcgen 1.0.7 on 17/1/15 1:57 PM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
import org.acplt.oncrpc.*;
import java.io.IOException;

public class acc_id_num implements XdrAble {

    public String value;

    public acc_id_num() {
    }

    public acc_id_num(String value) {
        this.value = value;
    }

    public acc_id_num(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeString(value);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeString();
    }

}
// End of acc_id_num.java

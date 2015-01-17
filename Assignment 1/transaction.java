/*
 * Automatically generated by jrpcgen 1.0.7 on 17/1/15 1:57 PM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
import org.acplt.oncrpc.*;
import java.io.IOException;

public class transaction implements XdrAble {
    public acc_id_num dst_account;
    public int amount;

    public transaction() {
    }

    public transaction(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        dst_account.xdrEncode(xdr);
        xdr.xdrEncodeInt(amount);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        dst_account = new acc_id_num(xdr);
        amount = xdr.xdrDecodeInt();
    }

}
// End of transaction.java

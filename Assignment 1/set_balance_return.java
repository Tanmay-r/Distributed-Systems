/*
 * Automatically generated by jrpcgen 1.0.7 on 17/1/15 1:57 PM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
import org.acplt.oncrpc.*;
import java.io.IOException;

public class set_balance_return implements XdrAble {
    public acc_id_num account_number;
    public int old_balance;
    public int new_balance;

    public set_balance_return() {
    }

    public set_balance_return(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        account_number.xdrEncode(xdr);
        xdr.xdrEncodeInt(old_balance);
        xdr.xdrEncodeInt(new_balance);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        account_number = new acc_id_num(xdr);
        old_balance = xdr.xdrDecodeInt();
        new_balance = xdr.xdrDecodeInt();
    }

}
// End of set_balance_return.java

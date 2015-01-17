
import org.acplt.oncrpc.*;
import java.net.InetAddress;
import java.io.IOException;

public class Bank {

    static int opt;
    public static void main(String [] args) {

        // check the arguments
        if (args.length !=2) {
            System.out.println("usage: infoline host_name stu num ");
            System.exit(1);
        }



        //create an rpc client
        getAccTypeClient client = null;
        try {
            client = new getAccTypeClient(InetAddress.getByName(args[0]),
                                    OncRpcProtocols.ONCRPC_TCP);
        } catch ( Exception e ) {
            System.out.println("infoline: error when creating RPC client:");
            e.printStackTrace(System.out);
        }
        client.getClient().setTimeout(300*1000);

        System.out.print("Making request to server");

        // make request object
        acc_id_num arg1 =new acc_id_num ();
        arg1.value=args[1];
        try {
            byte res =client.GET_ACC_TYPE_1(arg1);
            System.out.println("Result is:"+ (char)res);
        } catch ( Exception e ) {
            System.out.println("Error contacting server");
            e.printStackTrace(System.out);
            return;
        }

        try {
            client.close();
        } catch ( Exception e ) {
            System.out.println("infoline: error when closing client:");
            e.printStackTrace(System.out);
        }
        client = null;
    }
}

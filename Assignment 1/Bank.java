
import org.acplt.oncrpc.*;
import java.net.InetAddress;
import java.io.IOException;

public class Bank {

    static int opt;
    public static void main(String [] args) {

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

        if(args.length == 2){
            acc_id_num arg1 = new acc_id_num ();
            arg1.value=args[1];
            try {
                byte res =client.GET_ACC_TYPE_1(arg1);
                System.out.println("Result is:"+ (char)res);
            } catch ( Exception e ) {
                System.out.println("Error contacting server");
                e.printStackTrace(System.out);
                return;
            }
        }
        else if(args.length >=2){
            String func_to_call = args[1];
            if(func_to_call.equals("GET_BALANCE")){
                if(args.length != 3){ 
                    System.out.println("infoline: error in number of arguments"); 
                    return;
                }
                acc_id_num arg1 = new acc_id_num ();
                arg1.value = args[2];
                try {
                    int res = client.GET_BALANCE_1(arg1);
                    System.out.println(arg1.value + " " + res);
                } catch ( Exception e ) {
                    System.out.println("Error contacting server");
                    e.printStackTrace(System.out);
                    return;
                }
            }
            else if(func_to_call.equals("SET_BALANCE")){
                if(args.length != 4){ 
                    System.out.println("infoline: error in number of arguments"); 
                    return;
                }
                acc_id_num arg1 = new acc_id_num ();
                arg1.value = args[2];
                int x = Integer.parseInt(args[3]);
                try {
                    set_balance_return res = client.SET_BALANCE_1(arg1, x);
                    System.out.println(res.account_number.value + " " + res.old_balance + " " + res.new_balance);
                } catch ( Exception e ) {
                    System.out.println("Error contacting server");
                    e.printStackTrace(System.out);
                    return;
                }
            }
            else if(func_to_call.equals("TRANSACTION")){
                if(args.length != 5){ 
                    System.out.println("infoline: error in number of arguments"); 
                    return;
                }
                acc_id_num arg1 = new acc_id_num ();
                acc_id_num arg2 = new acc_id_num ();
                arg1.value = args[2];
                arg2.value = args[3];
                int x = Integer.parseInt(args[4]);

                try {
                    transaction_return res = client.TRANSACTION_1(arg1, arg2, x);
                    System.out.println(res.src_account.account_number.value + " " + res.src_account.old_balance + " " + res.dst_account.account_number.value + " " + res.dst_account.old_balance);
                    System.out.println(res.src_account.account_number.value + " " + res.src_account.new_balance + " " + res.dst_account.account_number.value + " " + res.dst_account.new_balance);
                } catch ( Exception e ) {
                    System.out.println("Error contacting server");
                    e.printStackTrace(System.out);
                    return;
                }

            }
            else if(func_to_call.equals("GET_TRANSACTION_HISTORY")){
                if(args.length != 3){ 
                    System.out.println("infoline: error in number of arguments"); 
                    return;
                }
                acc_id_num arg1 = new acc_id_num ();
                arg1.value = args[2];
                try {
                    LINKEDLIST res = client.GET_TRANSACTIOn_HISTORY_1(arg1);
                    while(res != null){
                        System.out.println(res.txn.dst_account.value + " " + res.txn.amount);
                        res = res.next;
                    }
                } catch ( Exception e ) {
                    System.out.println("Error contacting server");
                    e.printStackTrace(System.out);
                    return;
                }
            }
            
        }
        else{
            System.out.println("infoline: error in number of arguments");
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

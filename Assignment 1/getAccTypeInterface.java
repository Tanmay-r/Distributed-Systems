import org.acplt.oncrpc.*;
import org.acplt.oncrpc.server.*;
import java.io.*;
import java.util.*;

public class getAccTypeInterface extends getAccTypeServerStub {

    private static String datafilename;
    private static String historyfilename;

    public getAccTypeInterface() 
        throws OncRpcException, IOException {
        super();
    }

    public void run() 
        throws OncRpcException, IOException {
        System.out.println("server unregister tports");
        unregister(transports);
        System.out.println("server register new tports");
        register(transports);
        System.out.println("run server tports");
        run(transports);
        System.out.println("server end run");
        unregister(transports);
        close(transports);
    }

    public byte GET_ACC_TYPE_1(acc_id_num arg1){
        System.out.println("Processing request for "+ arg1.value);
        BufferedReader in=null;
        try{
            in = new BufferedReader(new FileReader(datafilename));
            //read file
            String line =null;
            while((line=in.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line);
                // data file must have all 3 data fields
                if (st.countTokens()==3){
                    // check the id to see if equal
                    if(st.nextToken().equals(arg1.value)){
                        return  (st.nextToken().getBytes())[0];
                    }
                }
            }
            // if fall through then return error
            return 0;
        }
        catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
        }
        return 0;
    } 

    public int GET_BALANCE_1(acc_id_num arg1){
        System.out.println("Processing request for "+ arg1.value);
        BufferedReader in=null;
        try{
            in = new BufferedReader(new FileReader(datafilename));
            //read file
            String line =null;
            while((line=in.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line);
                // data file must have all 3 data fields
                if (st.countTokens()==3){
                    // check the id to see if equal
                    if(st.nextToken().equals(arg1.value)){
                        st.nextToken();
                        return  Integer.parseInt(st.nextToken());
                    }
                }
            }
            // if fall through then return error
            return 0;
        }
        catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
        }
        return 0;
    }

    public set_balance_return SET_BALANCE_1(acc_id_num arg1, int arg2){
        System.out.println("Processing request for "+ arg1.value);
        set_balance_return ret = new set_balance_return();
        ret.account_number = arg1;
        try{
            FileReader fr = new FileReader(datafilename);
            BufferedReader br = new BufferedReader(fr);

            //read file
            ArrayList<String> data = new ArrayList<String>();
            String line =null;
            while((line=br.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line);
                // data file must have all 3 data fields
                if (st.countTokens()==3){
                    // check the id to see if equal
                    if(st.nextToken().equals(arg1.value)){
                        String newLine = arg1.value + " " + st.nextToken();
                        ret.old_balance = Integer.parseInt(st.nextToken());
                        ret.new_balance = ret.old_balance + arg2;
                        newLine += " " + ret.new_balance;
                        data.add(newLine);                        
                    }
                    else{
                        data.add(line);
                    }
                }
            }
            br.close();

            FileWriter fw = new FileWriter(datafilename);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String l : data) {
                bw.write(l);
                bw.newLine();
            } 
            bw.close();

            // if fall through then return error
            return ret;
        }
        catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
        }
        return ret;
    }

    public transaction_return TRANSACTION_1(acc_id_num arg1, acc_id_num arg2, int arg3){
        System.out.println("Processing request for "+ arg1.value);
        transaction_return ret = new transaction_return();
        ret.src_account = new set_balance_return();
        ret.dst_account = new set_balance_return();
        ret.src_account.account_number = arg1;
        ret.dst_account.account_number = arg2;
        // ret.account_number = arg1;
        // ret.new_balance = arg2;
        try{
            FileReader fr = new FileReader(datafilename);
            BufferedReader br = new BufferedReader(fr);

            //read file
            ArrayList<String> data = new ArrayList<String>();
            String line =null;
            while((line=br.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line);
                // data file must have all 3 data fields
                if (st.countTokens()==3){
                    // check the id to see if equal
                    String acct_num = st.nextToken();
                    if(acct_num.equals(arg1.value)){
                        String newLine = arg1.value + " " + st.nextToken();
                        ret.src_account.old_balance = Integer.parseInt(st.nextToken());
                        ret.src_account.new_balance = ret.src_account.old_balance - arg3;
                        newLine += " " + ret.src_account.new_balance;
                        data.add(newLine);                        
                    }
                    else if(acct_num.equals(arg2.value)){
                        String newLine = arg2.value + " " + st.nextToken();
                        ret.dst_account.old_balance = Integer.parseInt(st.nextToken());
                        ret.dst_account.new_balance = ret.dst_account.old_balance + arg3;
                        newLine += " " + ret.dst_account.new_balance;
                        data.add(newLine);                                               
                    }
                    else{
                        data.add(line);
                    }
                }
            }
            br.close();

            FileWriter fw = new FileWriter(datafilename);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String l : data) {
                bw.write(l);
                bw.newLine();
            } 
            bw.close();
            
            //write history
            fw = new FileWriter(historyfilename, true);
            bw = new BufferedWriter(fw);
            bw.write(arg1.value + " " + arg2.value + " " + arg3);
            bw.newLine();
            bw.close();

            // if fall through then return error
            return ret;
        }
        catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
        }
        return ret;
    }

    public LINKEDLIST GET_TRANSACTIOn_HISTORY_1(acc_id_num arg1){
        System.out.println("Processing request for "+ arg1.value);
        LINKEDLIST ret = null;
        LINKEDLIST curr = null;
        try{
            FileReader fr = new FileReader(historyfilename);
            BufferedReader br = new BufferedReader(fr);

            //read file
            String line =null;
            while((line=br.readLine())!=null){
                StringTokenizer st = new StringTokenizer(line);
                // data file must have all 3 data fields
                if (st.countTokens()==3){
                    // check the id to see if equal
                    if(st.nextToken().equals(arg1.value)){
                        if(curr == null){                            
                            ret = new LINKEDLIST();
                            ret.txn = new transaction();
                            ret.txn.dst_account = new acc_id_num();
                            ret.txn.dst_account.value = st.nextToken();
                            ret.txn.amount = Integer.parseInt(st.nextToken());
                            curr = ret; 
                        }
                        else{
                            curr.next = new LINKEDLIST();
                            curr.next.txn = new transaction();
                            curr.next.txn.dst_account = new acc_id_num();
                            curr.next.txn.dst_account.value = st.nextToken();
                            curr.next.txn.amount = Integer.parseInt(st.nextToken());
                            curr = curr.next;    
                        }            
                    }
                }
            }
            br.close();
            if(ret == null){
                ret = new LINKEDLIST();
                ret.txn = new transaction();
                ret.txn.dst_account = new acc_id_num("0");
                ret.txn.amount = -1;
            }
            return ret;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("error Processing request for "+ arg1.value );
        }
        return ret;
    }

    public static void main(String [] args) {
        //check for file argument
        if (args.length >1) {
            System.out.println("usage: getAccTypeInterface [datafile]");
            System.exit(1);
        }
        if (args.length ==1) {
            datafilename=args[0];
        }
        else {
            datafilename = "acc-new.txt";
        }

        historyfilename = "transaction-history.txt";

        //test existance of datafile
        File f = new File(datafilename);
        if (!f.isFile()){
            // datafile is missing
            System.out.println(datafilename + " is not a valid file name \n Server aborting");
            System.exit(1);
        }

        try {
            System.out.println("Starting getAccTypeInterface...");
            getAccTypeInterface server = new getAccTypeInterface();
            server.run();
        } catch ( Exception e ) {
            System.out.println("Server error:");
            e.printStackTrace(System.out);
        }
        System.out.println("Server stopped.");
    }
}

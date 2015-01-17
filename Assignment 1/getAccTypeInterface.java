import org.acplt.oncrpc.*;
import org.acplt.oncrpc.server.*;
import java.io.*;
import java.util.*;

public class getAccTypeInterface extends getAccTypeServerStub {

    private static String datafilename;

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



   public  byte GET_ACC_TYPE_1(acc_id_num arg1){
        System.out.println("Processing request for "+ arg1.value );
	BufferedReader in=null;
	try{
		in = new BufferedReader(new FileReader(datafilename));
		//read file
		String line =null;
		while((line=in.readLine())!=null){
		     	StringTokenizer st = new StringTokenizer(line);
			// data file must have all 5 data fields
			if (st.countTokens()==2){
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
		datafilename="acc.txt";
	}

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

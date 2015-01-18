Assignment 1
Task 1

Tanmay Randhavane
110050010
Mayank Meghwanshi
110050012

Commands to run:
1) Generate the stubs and data structure classes
java -jar jrpcgen.jar getAccType.x

2) Compile the files
javac -classpath .:remotetea/classes/oncrpc.jar *.java

3) Run the server
java -classpath .:remotetea/classes/oncrpc.jar getAccTypeInterface

4) Run the client calls
	i) Get account type
	java -classpath .:remotetea/classes/oncrpc.jar Bank 127.0.0.1 acc_id

	ii) Get balance
	java -classpath .:remotetea/classes/oncrpc.jar Bank 127.0.0.1 GET_BALANCE acc_id

	iii) Set balance
	java -classpath .:remotetea/classes/oncrpc.jar Bank 127.0.0.1 SET_BALANCE acc_id x

	iv) Perform transaction
	java -classpath .:remotetea/classes/oncrpc.jar Bank 127.0.0.1 TRANSACTION src_acc_id dst_acc_id x

	v) Get transaction History
	java -classpath .:remotetea/classes/oncrpc.jar Bank 127.0.0.1 GET_TRANSACTION_HISTORY acc_id y

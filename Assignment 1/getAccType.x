const ID_NUM_SIZE = 5;
const NO_ACC = 0;

typedef string  acc_id_num<ID_NUM_SIZE>;

program BANK_ACCOUNT_PROG {
	version ACC_VERS_1 {
                char   GET_ACC_TYPE(acc_id_num) = 1; //1 is the number assigned to this function
        }=1; //1 is the number assigned to this version
}=9999; //9999 is the number assigned to this program

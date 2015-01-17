const ID_NUM_SIZE = 5;
const NO_ACC = 0;

typedef string  acc_id_num<ID_NUM_SIZE>;

struct set_balance_return {
	acc_id_num account_number;
	int old_balance;
	int new_balance;
};

struct transaction_return {
	set_balance_return src_account;
	set_balance_return dst_account;
};

struct transaction {
	acc_id_num dst_account;
	int amount;
};

struct LINKEDLIST {
    transaction txn;
    struct LINKEDLIST *next;
};

program BANK_ACCOUNT_PROG {
	version ACC_VERS_1 {
                char   GET_ACC_TYPE(acc_id_num) = 1; //1 is the number assigned to this function
                int    GET_BALANCE(acc_id_num) = 2;
                set_balance_return SET_BALANCE(acc_id_num, int) = 3;
                transaction_return    TRANSACTION(acc_id_num, acc_id_num, int) = 4;
                LINKEDLIST    GET_TRANSACTIOn_HISTORY(acc_id_num) = 5;
        }=1; //1 is the number assigned to this version
}=9999; //9999 is the number assigned to this program

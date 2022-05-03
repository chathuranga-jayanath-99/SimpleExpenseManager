package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    public static final String ACCOUNT_TABLE = "account_table";
    public static final String ACCOUNT_NO_COLUMN = "account_no";
    public static final String BANK_NAME_COLUMN = "bank_name";
    public static final String ACCOUNT_HOLDER_NAME_COLUMN = "account_holder_name";
    public static final String BALANCE_COLUMN = "balance";

    public static final String CREATE_TABLE_QUERY = "create table " + ACCOUNT_TABLE + " (" +
            ACCOUNT_NO_COLUMN + " text primary key not null, " +
            BANK_NAME_COLUMN + " text not null, " +
            ACCOUNT_HOLDER_NAME_COLUMN + " text not null, " +
            BALANCE_COLUMN + " real not null)";

    private DatabaseHelper databaseHelper;

    public PersistentAccountDAO(@Nullable Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    public List<String> getAccountNumbersList() {
        ArrayList<String> returnList = new ArrayList<>();

        String queryString = "select "+ACCOUNT_NO_COLUMN+" from "+ACCOUNT_TABLE;

        SQLiteDatabase readableDatabase = this.databaseHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                String accountNo = cursor.getString(cursor.getColumnIndex(ACCOUNT_NO_COLUMN));
                returnList.add(accountNo);
            } while (cursor.moveToNext());
        }


        // close both cursor and readableDatabase
        cursor.close();
        readableDatabase.close();
        return returnList;
    }

    @Override
    public List<Account> getAccountsList() {
        ArrayList<Account> returnList = new ArrayList<>();

        String queryString = "select * from "+ACCOUNT_TABLE;

        SQLiteDatabase readableDatabase = this.databaseHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            do {
                String accountNo = cursor.getString(cursor.getColumnIndex(ACCOUNT_NO_COLUMN));
                String bankName = cursor.getString(cursor.getColumnIndex(BANK_NAME_COLUMN));
                String accountHolderName = cursor.getString(cursor.getColumnIndex(ACCOUNT_HOLDER_NAME_COLUMN));
                double balance = cursor.getDouble(cursor.getColumnIndex(BALANCE_COLUMN));

                Account account = new Account(accountNo, bankName, accountHolderName, balance);
                returnList.add(account);
            } while (cursor.moveToNext());
        }

        // close cursor and readableDatabase
        cursor.close();
        readableDatabase.close();

        return returnList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Account account = null;

        String queryString = "select * from "+ACCOUNT_TABLE+
                " where "+ACCOUNT_NO_COLUMN+"=?";

        SQLiteDatabase readableDatabase = this.databaseHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(queryString, new String[]{accountNo});

        if (cursor.moveToFirst()) {
            String accountNoSel = cursor.getString(cursor.getColumnIndex(ACCOUNT_NO_COLUMN));
            String bankName = cursor.getString(cursor.getColumnIndex(BANK_NAME_COLUMN));
            String accountHolderName = cursor.getString(cursor.getColumnIndex(ACCOUNT_HOLDER_NAME_COLUMN));
            double balance = cursor.getDouble(cursor.getColumnIndex(BALANCE_COLUMN));

            account = new Account(accountNoSel, bankName, accountHolderName, balance);
        }

        // close cursor and readableDatabase
        cursor.close();
        readableDatabase.close();

        return account;
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase writableDatabase = this.databaseHelper.getWritableDatabase();

        String queryString = "insert into "+ACCOUNT_TABLE+" values (?,?,?,?)";

        writableDatabase.execSQL(queryString, new String[] {
                account.getAccountNo(), account.getBankName(), account.getAccountHolderName(), String.valueOf(account.getBalance())
        });

    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase writableDatabase = this.databaseHelper.getWritableDatabase();
        String queryString = "delete from "+ACCOUNT_TABLE+" where "+ACCOUNT_NO_COLUMN+"=?";

        Cursor cursor = writableDatabase.rawQuery(queryString, new String[]{accountNo});

        if (!cursor.moveToFirst()) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase writableDatabase = this.databaseHelper.getWritableDatabase();

        Cursor cursor = writableDatabase.rawQuery("select * from " + ACCOUNT_TABLE +
                " where " + ACCOUNT_NO_COLUMN + "= ?", new String[]{accountNo});

        if (cursor.moveToFirst()) {
            double balance = cursor.getDouble(cursor.getColumnIndex(BALANCE_COLUMN));

            switch (expenseType) {
                case EXPENSE:
                    balance = balance - amount;
                    break;
                case INCOME:
                    balance = balance + amount;
                    break;
            }

            String queryString = "update "+ACCOUNT_TABLE+" set "+BALANCE_COLUMN+"=? where "+ACCOUNT_NO_COLUMN+"=?";

            writableDatabase.execSQL(queryString, new String[]{String.valueOf(balance), accountNo});

        }
        else {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
    }
}

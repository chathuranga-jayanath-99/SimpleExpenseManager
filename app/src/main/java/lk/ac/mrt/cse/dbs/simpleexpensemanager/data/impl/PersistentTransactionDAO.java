package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {

    public static final String TRANSACTION_TABLE = "transaction_table";
    public static final String ID_COLUMN = "id";
    public static final String DATE_COLUMN = "date";
    public static final String ACCOUNT_NO_COLUMN = "account_no";
    public static final String EXPENSE_TYPE_COLUMN = "expense_type";
    public static final String AMOUNT_COLUMN = "amount";

    public static String CREATE_TABLE_QUERY = "create table " + TRANSACTION_TABLE + " (" +
            ID_COLUMN + " integer primary key autoincrement, " +
            DATE_COLUMN + " text, " +
            ACCOUNT_NO_COLUMN + " text, " +
            EXPENSE_TYPE_COLUMN + " text, " +
            AMOUNT_COLUMN + " real, " +
            "foreign key ("+ACCOUNT_NO_COLUMN+") references "+PersistentAccountDAO.ACCOUNT_TABLE+" ("+ACCOUNT_NO_COLUMN+"))";

    private DatabaseHelper databaseHelper;
    private SimpleDateFormat simpleDateFormat;

    public PersistentTransactionDAO(@Nullable Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase writableDatabase = this.databaseHelper.getWritableDatabase();

        String queryString = "insert into "+TRANSACTION_TABLE+" " +
                "("+DATE_COLUMN+", "+ACCOUNT_NO_COLUMN+", "+EXPENSE_TYPE_COLUMN+", "+AMOUNT_COLUMN+") values (?,?,?,?)";

        String expenseTypeStr = String.valueOf(expenseType);

        writableDatabase.execSQL(queryString, new String[] {
                simpleDateFormat.format(date), accountNo, expenseTypeStr, String.valueOf(amount)
        });
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        ArrayList<Transaction> returnList;

        String queryString = "select * from "+TRANSACTION_TABLE;

        SQLiteDatabase readableDatabase = this.databaseHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(queryString, null);

        returnList = getTransactionLogsInsideCursor(cursor);

        // close database
        readableDatabase.close();

        return returnList;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        ArrayList<Transaction> returnList;

        String queryString = "select * from "+TRANSACTION_TABLE+" limit ?";

        SQLiteDatabase readableDatabase = this.databaseHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(queryString, new String[]{String.valueOf(limit)});

        returnList = getTransactionLogsInsideCursor(cursor);

        // close database
        readableDatabase.close();

        return returnList;
    }

    private ArrayList<Transaction> getTransactionLogsInsideCursor(Cursor cursor) {
        ArrayList<Transaction> returnList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                try {
                    String dateStr = cursor.getString(cursor.getColumnIndex(DATE_COLUMN));
                    Date date = simpleDateFormat.parse(dateStr);

                    String accountNo = cursor.getString(cursor.getColumnIndex(ACCOUNT_NO_COLUMN));

                    String expenseTypeStr = cursor.getString(cursor.getColumnIndex(EXPENSE_TYPE_COLUMN));
                    ExpenseType expenseType = (expenseTypeStr.equals("EXPENSE")) ? ExpenseType.EXPENSE : ExpenseType.INCOME;

                    Double amount = cursor.getDouble(cursor.getColumnIndex(AMOUNT_COLUMN));

                    Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
                    returnList.add(transaction);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        // close cursor
        cursor.close();

        return returnList;
    }
}


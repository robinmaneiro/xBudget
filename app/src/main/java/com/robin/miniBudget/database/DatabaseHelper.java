package com.robin.miniBudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.robin.miniBudget.Category;
import com.robin.miniBudget.MainActivity;
import com.robin.miniBudget.R;
import com.robin.miniBudget.database.DatabaseSchema.TransactionTable;

import java.util.Currency;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "budgetbase.db";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertDemoGroups(db);
        insertDemoCats(db);
        insertConstantValues(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("create table " + TransactionTable.mTransactions +
                "(" +
                TransactionTable.TransactionColumns.ID + " PRIMARY KEY" + ", " +
                TransactionTable.TransactionColumns.CATEGORY_ID + ", " +
                TransactionTable.TransactionColumns.GROUP_ID + ", " +
                TransactionTable.TransactionColumns.NAME + ", " +
                TransactionTable.TransactionColumns.AMOUNT + ", " +
                TransactionTable.TransactionColumns.DESCRIPTION + ", " +
                TransactionTable.TransactionColumns.DATE +
                ")");

        db.execSQL("create table " + TransactionTable.mCategories +
                "(" +
                TransactionTable.CategoryColumns.ID + " PRIMARY KEY" + ", " +
                TransactionTable.CategoryColumns.GROUP_ID + ", " +
                TransactionTable.CategoryColumns.NAME + ", " +
                TransactionTable.CategoryColumns.AMOUNT + ", " +
                TransactionTable.CategoryColumns.DATE +
                ")");

        db.execSQL("create table " + TransactionTable.mGroups +
                "(" +
                TransactionTable.GroupColumns.ID + " PRIMARY KEY " + ", " +
                TransactionTable.GroupColumns.NAME +
                ")");

        db.execSQL("create table " + TransactionTable.mConstants +
                "(" +
                TransactionTable.ConstantColumns.NAME + " PRIMARY KEY " + ", " +
                TransactionTable.ConstantColumns.VALUE +
                ")");

    }

    //Insert Demo Groups
    private void insertDemoGroups(SQLiteDatabase db) {
        String[] demoGroups = {
                new String("INCOMES"), new String("EXPENSES"), new String("SAVINGS"),
        };

        for (int i = 0; i < demoGroups.length; i++) {
            db.execSQL("INSERT INTO " + TransactionTable.mGroups +
                    "(" +
                    TransactionTable.GroupColumns.ID + ", " +
                    TransactionTable.GroupColumns.NAME +
                    ") VALUES (" + i + 1 + ", '" + demoGroups[i] + "')");
        }
    }

    // Insert Categories demo for the first use of the application
    synchronized private void insertDemoCats(SQLiteDatabase db) {

        Category[] demoIncomeCategories = {
                new Category(context.getResources().getString(R.string.demo_income_cat_0), 1800.0, Category.Group.INCOMES),
                new Category(context.getResources().getString(R.string.demo_income_cat_1), 350.0, Category.Group.INCOMES),
                new Category(context.getResources().getString(R.string.demo_income_cat_2), 200.0, Category.Group.INCOMES)
        };

        Category[] demoExpenseCategories = {
                new Category(context.getResources().getString(R.string.demo_expense_cat_0), 750.0, Category.Group.EXPENSES),
                new Category(context.getResources().getString(R.string.demo_expense_cat_1), 150.0, Category.Group.EXPENSES),
                new Category(context.getResources().getString(R.string.demo_expense_cat_2), 50.0, Category.Group.EXPENSES),
                new Category(context.getResources().getString(R.string.demo_expense_cat_3), 250.0, Category.Group.EXPENSES),
                new Category(context.getResources().getString(R.string.demo_expense_cat_4), 150.0, Category.Group.EXPENSES)
        };

        for (Category c : demoIncomeCategories) { //Insert DEMO income categories in the database
            db.insert(TransactionTable.mCategories, null, getContentValuesCategory(c));
        }

        for (Category c : demoExpenseCategories) { //Insert DEMO expense categories in the database
            db.insert(TransactionTable.mCategories, null, getContentValuesCategory(c));
        }
    }

    // Insert Categories demo for the first use of the application
    public static synchronized  void insertConstantValues(SQLiteDatabase db) {
        String query = "INSERT INTO "+TransactionTable.mConstants+" ("+ TransactionTable.ConstantColumns.NAME+","+ TransactionTable.ConstantColumns.VALUE +") VALUES ('"+

        MainActivity.CURRENCY_KEY +"','"+ Currency.getInstance(Locale.getDefault()).getCurrencyCode()+"')";

        db.execSQL(query);
    }



        private ContentValues getContentValuesCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(TransactionTable.CategoryColumns.ID, category.getId().toString());
        values.put(TransactionTable.CategoryColumns.GROUP_ID, category.getGroupId());
        values.put(TransactionTable.CategoryColumns.NAME, category.getName());
        values.put(TransactionTable.CategoryColumns.AMOUNT, category.getAmount());
        values.put(TransactionTable.CategoryColumns.DATE, category.getDateAssigned());

        return values;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

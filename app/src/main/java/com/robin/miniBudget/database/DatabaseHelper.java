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

    private final String TAG = this.getClass().getSimpleName();
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
                TransactionTable.TransCols.ID + " PRIMARY KEY" + ", " +
                TransactionTable.TransCols.CATEGORY_ID + ", " +
                TransactionTable.TransCols.GROUP_ID + ", " +
                TransactionTable.TransCols.NAME + ", " +
                TransactionTable.TransCols.AMOUNT + ", " +
                TransactionTable.TransCols.DESCRIPTION + ", " +
                TransactionTable.TransCols.DATE +
                ")");

        db.execSQL("create table " + TransactionTable.mCategories +
                "(" +
                TransactionTable.CatCols.ID + " PRIMARY KEY" + ", " +
                TransactionTable.CatCols.GROUP_ID + ", " +
                TransactionTable.CatCols.NAME + ", " +
                TransactionTable.CatCols.AMOUNT + ", " +
                TransactionTable.CatCols.DATE +
                ")");

        db.execSQL("create table " + TransactionTable.mGroups +
                "(" +
                TransactionTable.GroupCols.ID + " PRIMARY KEY " + ", " +
                TransactionTable.GroupCols.NAME +
                ")");

        db.execSQL("create table " + TransactionTable.mConstants +
                "(" +
                TransactionTable.ConstantCols.NAME + " PRIMARY KEY " + ", " +
                TransactionTable.ConstantCols.VALUE +
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
                    TransactionTable.GroupCols.ID + ", " +
                    TransactionTable.GroupCols.NAME +
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
        String query = "INSERT INTO "+TransactionTable.mConstants+" ("+TransactionTable.ConstantCols.NAME+","+TransactionTable.ConstantCols.VALUE +") VALUES ('"+

        MainActivity.CURRENCY_KEY +"','"+ Currency.getInstance(Locale.getDefault()).getCurrencyCode()+"')";

        db.execSQL(query);
    }



        private ContentValues getContentValuesCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(TransactionTable.CatCols.ID, category.getId().toString());
        values.put(TransactionTable.CatCols.GROUP_ID, category.getGroupId());
        values.put(TransactionTable.CatCols.NAME, category.getName());
        values.put(TransactionTable.CatCols.AMOUNT, category.getAmount());
        values.put(TransactionTable.CatCols.DATE, category.getDateAssigned());

        return values;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

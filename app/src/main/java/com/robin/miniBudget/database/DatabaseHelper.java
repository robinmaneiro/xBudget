package com.robin.miniBudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.robin.miniBudget.Category;
import com.robin.miniBudget.MainActivity;
import com.robin.miniBudget.R;
import com.robin.miniBudget.SingleFragmentActivity;
import com.robin.miniBudget.TransFragment;
import com.robin.miniBudget.database.DatabaseSchema.TransactionTable;

import java.util.concurrent.TransferQueue;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = this.getClass().getSimpleName();
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "budgetbase.db";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        createTables(db);
        insertDemoGroups(db);
        insertDemoCats(db);
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

        for (Category c : demoIncomeCategories) {
            Log.d(TAG, "Inserted demo category: " + db.insert(TransactionTable.mCategories, null, getContentValuesCategory(c)));
        }

        for (Category c : demoExpenseCategories) {
            Log.d(TAG, "Inserted demo category: " + db.insert(TransactionTable.mCategories, null, getContentValuesCategory(c)));
        }
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

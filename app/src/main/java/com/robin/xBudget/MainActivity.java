package com.robin.xBudget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.mynameismidori.currencypicker.ExtendedCurrency;
import com.robin.xBudget.database.DatabaseHelper;
import com.robin.xBudget.database.DatabaseSchema;
import com.robin.xBudget.database.DatabaseSchema.TransactionTable;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MainActivity extends SingleFragmentActivity implements
        TransFragment.DemoObjectFragment.Listener
        , TransFragment.OuterListener
        , StatisticsFragment.OuterListener
        , StatisticsFragment.DemoObjectFragment.InnerListener
        , DataViewFragment.DemoObjectFragment.Listener
        , DataViewFragment.OuterListener
        , DialogTransaction.Listener
        , DialogCategory.Listener
            ,DialogSettings.Listener
{
    private final String TAG = this.getClass().getSimpleName();
    private SQLiteDatabase mDatabase;
    private Context mContext;

    public static String CURRENCY_KEY = "currency";
    public static String CURRENCY_SYMBOL = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    @Override
    protected Fragment createFragment() {
        getParsedMonthDates(TransactionTable.mCategories, null, null);

        CURRENCY_SYMBOL = ExtendedCurrency.getCurrencyByISO(getConstantValue(CURRENCY_KEY)).getSymbol(); //get currency from db and set symbol
        return TransFragment.newInstance();
    }


    public void init() {
        Log.d(TAG, "initObject() was called");
        //Starting the databases
        mContext = getApplicationContext();
        mDatabase = new DatabaseHelper(mContext).getWritableDatabase();
    }


    @Override
     public String getConstantValue (String key ){
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM constants"+" WHERE "+TransactionTable.ConstantCols.NAME+" = '"+key+"'",null);

        //Toast.makeText(getApplicationContext(),"value is"+cursor.getCount(),Toast.LENGTH_LONG).show();

        if(cursor.moveToFirst()) {
            do {
                return cursor.getString(cursor.getColumnIndex(TransactionTable.ConstantCols.VALUE));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return "";
    }

    @Override
    public void setConstantValue (String key,String value){
        String query = "UPDATE "+TransactionTable.mConstants
                + " SET "+TransactionTable.ConstantCols.VALUE+" = '"+value
                + "' WHERE "+TransactionTable.ConstantCols.NAME+" = '"+key+"'";

        mDatabase.execSQL(query);

    }

    private ContentValues getContentValuesTransaction(Transaction transaction) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");
        ContentValues values = new ContentValues();
        values.put(TransactionTable.TransCols.ID, transaction.getId().toString());
        values.put(TransactionTable.TransCols.CATEGORY_ID, transaction.getCategoryId().toString());
        values.put(TransactionTable.TransCols.GROUP_ID, transaction.getGroupId());
        values.put(TransactionTable.TransCols.NAME, transaction.getName());
        values.put(TransactionTable.TransCols.AMOUNT, transaction.getAmount());
        values.put(TransactionTable.TransCols.DESCRIPTION, transaction.getDescription());
        values.put(TransactionTable.TransCols.DATE, String.valueOf(fmt.print(transaction.getDateTime())));

        return values;
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


    /**
     * METHODS TO INSERT VALUES IN THE DATABASE
     */

    @Override
    public void deleteCategory(Category category) {
        ContentValues values = getContentValuesCategory(category);
        Log.d(TAG, "CATEGORY DELETED???" + mDatabase.delete(TransactionTable.mCategories, TransactionTable.CatCols.ID + " = ? ", new String[]{category.getId().toString()}));
        Log.d(TAG, "TRANSACTIONS DELETED???" + mDatabase.delete(TransactionTable.mTransactions, TransactionTable.TransCols.CATEGORY_ID + " = ?", new String[]{category.getId().toString()}));
    }

    @Override
    public void updateCategory(Category category) {
        ContentValues values = getContentValuesCategory(category);
        Log.d(TAG, "TRANSACTION UPDATED???" + mDatabase.update(TransactionTable.mCategories, values, TransactionTable.CatCols.ID + " = ? ", new String[]{category.getId().toString()}));
    }


    @Override
    public void insertTransaction(Transaction transaction) {
        ContentValues values = getContentValuesTransaction(transaction);
        Log.d(TAG, "TRANSACTION INSERTED???" + mDatabase.insert(TransactionTable.mTransactions, null, values));

        //Change month to show to the just added
        //calendarSpinner.setTime(transaction.getDate());
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        ContentValues values = getContentValuesTransaction(transaction);
        Log.d(TAG, "TRANSACTION INSERTED???" + mDatabase.update(TransactionTable.mTransactions, values, TransactionTable.TransCols.ID + " = ? ", new String[]{transaction.getId().toString()}));

        //Change month to show to the just added
        //calendarSpinner.setTime(transaction.getDate());
    }

    @Override
    public void deleteTransaction(Transaction transaction) {
        ContentValues values = getContentValuesTransaction(transaction);
        Log.d(TAG, "TRANSACTION DELETED???" + mDatabase.delete(TransactionTable.mTransactions, TransactionTable.TransCols.ID + " = ?", new String[]{transaction.getId().toString()}));

        //Change month to show to the just added
        //calendarSpinner.setTime(transaction.getDate());
    }

    @Override
    public void insertCategory(Category category) {
        ContentValues values = getContentValuesCategory(category);
        Log.d(TAG, "CATEGORY INSERTED???: " + mDatabase.insert(TransactionTable.mCategories, null, values));
    }


    /**
     * METHODS TO QUERY ENTRIES IN THE DATABASE
     */
    private GeneralCursorWrapper queryEntries(String table, String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                table
                ,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new GeneralCursorWrapper(cursor);
    }


    @Override
    public List<Category> getCategories(String table, String whereClause, String[] whereArgs) {

        List<Category> categories = new ArrayList<>();

        GeneralCursorWrapper cursor = queryEntries(table, whereClause, whereArgs);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                categories.add(cursor.getCategory());
                cursor.moveToNext();
            }
        }
        return categories;
    }

    @Override
    public List<Transaction> getTransactions(String table, String whereClause, String[] whereArgs) {

        List<Transaction> transactions = new ArrayList<>();

        GeneralCursorWrapper cursor = queryEntries(table, whereClause, whereArgs);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                transactions.add(cursor.getTransaction());
                cursor.moveToNext();
            }
        }
        return transactions;
    }

    @Override
    public Category getSingleCategory(String table, String whereClause, String[] whereArgs) {

        Category category = new Category();
        GeneralCursorWrapper cursor = queryEntries(table, whereClause, whereArgs);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                category = (cursor.getCategory());
                cursor.moveToNext();
            }
        }
        return category;
    }


    @Override
    public Transaction getSingleTransaction(String table, String whereClause, String[] whereArgs) {

        Transaction transaction = new Transaction();
        GeneralCursorWrapper cursor = queryEntries(table, whereClause, whereArgs);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                transaction = (cursor.getTransaction());
                cursor.moveToNext();
            }
        }
        return transaction;
    }

    @Override
    public List<String> getParsedMonthDates(String table, String whereClause, String[] whereArgs) {
        Set<DateTime> dateTimeSet = new TreeSet<>();
        List<String> dateTimeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM YYYY");

        Cursor cursor = mDatabase.rawQuery("SELECT DISTINCT " + TransactionTable.CatCols.DATE + " FROM " + table, null);
        GeneralCursorWrapper gcw = new GeneralCursorWrapper(cursor);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                dateTimeSet.add(gcw.getDistinctDate());
                cursor.moveToNext();
            }
        }
        dateTimeSet.add(DateTime.now());         //Add the current month to make sure that even though we delete all categories, the current month is going to be shown in the spinner.
        for (DateTime dt : dateTimeSet)
            if (!dateTimeList.contains(formatter.print(dt)))
                dateTimeList.add(formatter.print(dt)); //Insert String month only when it is not in the list to prevent duplicates

        return dateTimeList;
    }

    public Set<String> getParsedYearDates(String table, String whereClause, String[] whereArgs) {
        Set<String> dateTime = new TreeSet<>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");

        Cursor cursor = mDatabase.rawQuery("SELECT DISTINCT " + TransactionTable.CatCols.DATE + " FROM " + table, null);
        GeneralCursorWrapper gcw = new GeneralCursorWrapper(cursor);


        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                dateTime.add(formatter.print(gcw.getDistinctDate()));
                cursor.moveToNext();
            }
        }
        if (!dateTime.contains(formatter.print(DateTime.now())))
            dateTime.add(formatter.print(DateTime.now())); //If not in the Set, add the current month to make sure that even though we delete all categories, the current month is going to be shown in the spinner.

        return dateTime;
    }


    @Override
    public ArrayAdapter getMonthArrayAdapter() {
        List<String> dates = getParsedMonthDates(DatabaseSchema.TransactionTable.mCategories, null, null);

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, dates);

        return spinnerArrayAdapter;
    }

    @Override
    public ArrayAdapter getYearArrayAdapter() {
        Set<String> dates = getParsedYearDates(DatabaseSchema.TransactionTable.mCategories, null, null);

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, dates.toArray());

        return spinnerArrayAdapter;
    }


    /*
    @Override
    public Set<Category> getDistinctCategories(String table, String whereClause, String[] whereArgs){
        Set<Category> categories = new HashSet<>();
        //Cursor cursor = mDatabase.rawQuery("SELECT DISTINCT "+TransactionTable.CatCols.NAME+" FROM "+table+" WHERE "+whereClause, whereArgs);

        Cursor cursor = mDatabase.rawQuery("SELECT * FROM 'categories' WHERE group_id = 2",null);

        GeneralCursorWrapper gcw = new GeneralCursorWrapper(cursor);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                categories.add(gcw.getCategory());
                Log.d(TAG, "NAME IS:"+gcw.getCategory().getName());
                cursor.moveToNext();
            }
        }
        return categories;
    }

     */

    @Override
    public boolean checkIfCategoryExists(String TableName,
                                         String categoryName, int categoryDate) {
        String Query = "SELECT * FROM " + TableName + " WHERE " + TransactionTable.CatCols.NAME + " = '" + categoryName + "' AND " + TransactionTable.CatCols.DATE + " = " + categoryDate;
        Cursor cursor = mDatabase.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    @Override
    public String spinnerToJodaTime(String parsedDate) {
        DateTimeFormatter formatterFrom = DateTimeFormat.forPattern("MMM yyyy");
        DateTime dt = formatterFrom.parseDateTime(parsedDate);

        DateTimeFormatter formatterTo = DateTimeFormat.forPattern("yyyyM");
        String dateReady = formatterTo.print(dt);

        return dateReady;
    }

    @Override
    public String datePickerToJodaTime(String parsedDate) {
        DateTimeFormatter formatterFrom = DateTimeFormat.forPattern("yyyy MM");
        DateTime dt = formatterFrom.parseDateTime(parsedDate);

        DateTimeFormatter formatterTo = DateTimeFormat.forPattern("yyyyM");
        String dateReady = formatterTo.print(dt);

        return dateReady;
    }

    @Override
    public String getCurrentDateParsed() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM yyyy");
        DateTime dt = new DateTime();
        return formatter.print(dt);
    }


    @Override
    public double getTransAmount(String tableName, int group_id, int period) {
        List<Transaction> list = getTransactions(tableName, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(group_id)});

        double amount = 0;

        switch (period) {

            case 1:
                for (Transaction t : list) {
                    if (LocalDate.now().compareTo(new LocalDate(t.getDateTime())) == 0)
                        amount += t.getAmount();
                }
                break;

            case 2:
                for (Transaction t : list) {
                    if (LocalDate.now().getWeekOfWeekyear() == t.getDateTime().getWeekOfWeekyear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;

            case 3:
                for (Transaction t : list) {
                    if (LocalDate.now().getMonthOfYear() == t.getDateTime().getMonthOfYear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;


            case 4:
                for (Transaction t : list) {
                    if (LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }
        return amount;
    }

    @Override
    public synchronized String getTransDiff(String tableName, int group_id, int period) {
        List<Transaction> list = getTransactions(tableName, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(group_id)});

        double currentPeriodAmount = getTransAmount(tableName, group_id, period);
        double previousPeriodAmount = 0;


        switch (period) {

            case 1:
                for (Transaction t : list) {
                    if (LocalDate.now().minusDays(1).getDayOfYear() == t.getDateTime().getDayOfYear())
                        previousPeriodAmount += t.getAmount();
                }
                break;

            case 2:
                for (Transaction t : list) {
                    if (LocalDate.now().minusWeeks(1).getWeekOfWeekyear() == t.getDateTime().getWeekOfWeekyear()) {
                        if (LocalDate.now().getWeekOfWeekyear() == 1 && LocalDate.now().getWeekOfWeekyear() - 1 == t.getDateTime().getYear()
                                || LocalDate.now().getWeekOfWeekyear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                        ) {
                            previousPeriodAmount += t.getAmount();
                        }
                    }
                }
                break;

            case 3:
                for (Transaction t : list) {
                    if (LocalDate.now().minusMonths(1).getMonthOfYear() == t.getDateTime().getMonthOfYear()) {
                        if (LocalDate.now().getMonthOfYear() == 1 && LocalDate.now().getYear() - 1 == t.getDateTime().getYear()
                                || LocalDate.now().getMonthOfYear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                        ) {
                            previousPeriodAmount += t.getAmount();
                        }
                    }
                }
                break;


            case 4:
                for (Transaction t : list) {
                    if (LocalDate.now().getYear() - 1 == t.getDateTime().getYear()) {
                        previousPeriodAmount += t.getAmount();
                    }
                }
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }
        double difference = currentPeriodAmount - previousPeriodAmount;
        double percentage = (difference / currentPeriodAmount) * 100;
        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No data >";
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public synchronized String getTransDiffAvg(String tableName, int group_id, int period) {
        List<Transaction> list = getTransactions(tableName, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(group_id)}); //Get All transactions

        double currentPeriodAmount = getTransAmount(tableName, group_id, period);
        Map<String, Double> groupByDateTreeMap = new TreeMap<>();
        String firstDate;
        Integer timePassed;


        switch (period) {

            case 1:
                DateTimeFormatter dailyFormatter = DateTimeFormat.forPattern("YYYY-MM-dd");

                Map<String, Double> unorderedDailyMap =
                        list.stream().collect(Collectors.groupingBy(transaction -> dailyFormatter.print(transaction.getDateTime()), (Collectors.summingDouble(Transaction::getAmount))));

                groupByDateTreeMap.putAll(unorderedDailyMap); // Convert the HashMap into a TreeMap to order the element by key

                if(!groupByDateTreeMap.isEmpty()) {
                    firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                    timePassed = Days.daysBetween(dailyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getDays();
                }else{
                    timePassed = 0;
                }
                break;

            case 2:
                DateTimeFormatter weeklyFormatter = DateTimeFormat.forPattern("YYYY-ww");
                Map<String, Double> unorderedWeeklyMap =
                        list.stream().collect(Collectors.groupingBy(transaction -> weeklyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedWeeklyMap); // Convert the HashMap into a TreeMap to order the element by key

                for (Map.Entry<String, Double> entryset : groupByDateTreeMap.entrySet()) {
                    Log.d(TAG, "key: " + entryset.getKey() + " value: " + entryset.getValue());
                }
                if(!groupByDateTreeMap.isEmpty()) {

                firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                timePassed = Weeks.weeksBetween(weeklyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getWeeks();
            }else{
                    timePassed = 0;
                }

                break;

            case 3:
                DateTimeFormatter monthlyFormatter = DateTimeFormat.forPattern("YYYY-MM");
                Map<String, Double> unorderedMontlyMap =
                        list.stream().collect(Collectors.groupingBy(transaction -> monthlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedMontlyMap); // Convert the HashMap into a TreeMap to order the element by key
                if(!groupByDateTreeMap.isEmpty()) {
                firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                timePassed = Months.monthsBetween(monthlyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getMonths();
            }else{
                    timePassed = 0;
                }
                break;


            case 4:
                DateTimeFormatter yearlyFormatter = DateTimeFormat.forPattern("YYYY");
                Map<String, Double> unorderedYearlyMap =
                        list.stream().collect(Collectors.groupingBy(transaction -> yearlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedYearlyMap); // Convert the HashMap into a TreeMap to order the element by key
                if(!groupByDateTreeMap.isEmpty()) {
                firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                timePassed = Years.yearsBetween(yearlyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getYears();
            }else{
                    timePassed = 0;
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }


        double averagePeriodAmount = groupByDateTreeMap.values().stream().mapToDouble(Double::doubleValue).sum() / timePassed;


        double difference = currentPeriodAmount - averagePeriodAmount;
        double percentage = (difference / currentPeriodAmount) * 100;
        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No data >";
    }

    @Override
    public synchronized double getCatsAmount(String tableName, String name, int group_id, int period) {
        List<Category> categoryList = getCategories(tableName, "name = ? AND CAST(group_id as TEXT) = ?", new String[]{name, String.valueOf(group_id)});
        //Log.d("GETCATSACCOUNTS", "List of categories is: "+categoryList);

        List<Transaction> listTransactionFilter = new ArrayList<>();

        for (Category c : categoryList) {
            //Log.d("GETCATSACCOUNTS", "Category in the list is: "+c.getDateAssigned());

            for (Transaction t : getTransactions(TransactionTable.mTransactions, "category_id = ?", new String[]{c.getId().toString()})) {
                //Log.d("GETCATSACCOUNTS", "Transactino in the list is: "+c.getName()+" with date "+t.getDateTime()+" and amount "+t.getAmount());

                listTransactionFilter.add(t);
            }
        }

        double amount = 0;

        switch (period) {

            case 5:
                for (Transaction t : listTransactionFilter) {
                    if (LocalDate.now().compareTo(new LocalDate(t.getDateTime())) == 0)
                        amount += t.getAmount();
                }
                break;

            case 6:
                for (Transaction t : listTransactionFilter) {
                    if (LocalDate.now().getWeekOfWeekyear() == t.getDateTime().getWeekOfWeekyear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;
            case 7:
                for (Transaction t : listTransactionFilter) {
                    if (LocalDate.now().getMonthOfYear() == t.getDateTime().getMonthOfYear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;


            case 8:
                for (Transaction t : listTransactionFilter) {
                    if (LocalDate.now().getYear() == t.getDateTime().getYear()) {
                        amount += t.getAmount();
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }
        return amount;
    }

    public synchronized String getCatsDiff(String tableName, String name, int group_id, int period) {
        List<Category> categoryList = getCategories(tableName, "name = ? AND CAST(group_id as TEXT) = ?", new String[]{name, String.valueOf(group_id)});
        List<Transaction> transactionList = new ArrayList<>();

        for (Category c : categoryList) {
            for (Transaction t : getTransactions(TransactionTable.mTransactions, "category_id = ?", new String[]{c.getId().toString()})) {
                transactionList.add(t);
            }
        }

        double currentPeriodAmount = getCatsAmount(tableName, name, group_id, period);
        double previousPeriodAmount = 0;

        switch (period) {

            case 5:
                for (Transaction t : transactionList) {
                    if (LocalDate.now().minusDays(1).getDayOfYear() == t.getDateTime().getDayOfYear() && LocalDate.now().getYear() == t.getDateTime().getYear())
                        previousPeriodAmount += t.getAmount();
                }
                break;

            case 6:
                for (Transaction t : transactionList) {
                    if (LocalDate.now().minusWeeks(1).getWeekOfWeekyear() == t.getDateTime().getWeekOfWeekyear()) {
                        if (LocalDate.now().getWeekOfWeekyear() == 1 && LocalDate.now().getWeekOfWeekyear() - 1 == t.getDateTime().getYear()
                                || LocalDate.now().getWeekOfWeekyear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                        ) {
                            previousPeriodAmount += t.getAmount();
                        }
                    }
                }
                break;

            case 7:
                for (Transaction t : transactionList) {
                    if (LocalDate.now().minusMonths(1).getMonthOfYear() == t.getDateTime().getMonthOfYear()) {
                        if (LocalDate.now().getMonthOfYear() == 1 && LocalDate.now().getYear() - 1 == t.getDateTime().getYear()
                                || LocalDate.now().getMonthOfYear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                        ) {
                            previousPeriodAmount += t.getAmount();
                        }
                    }
                }
                break;

            case 8:
                for (Transaction t : transactionList) {
                    if (LocalDate.now().getYear() - 1 == t.getDateTime().getYear()) {
                        previousPeriodAmount += t.getAmount();
                    }
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }

        Log.d("position", "Period " + period + " currentPeriodAmount is: " + currentPeriodAmount);
        Log.d("position", "Period " + period + " previousPeriodAmount is: " + previousPeriodAmount);

        //Log.d("position","Period " +period+ " currentPeriodAmount is: "+currentPeriodAmount);

        double difference = currentPeriodAmount - previousPeriodAmount;
        double percentage = (difference / currentPeriodAmount) * 100;
        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No diff. >";
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized String getCatsDiffAvg(String tableName, String name, int group_id, int period) {
        List<Category> categoryList = getCategories(tableName, "name = ? AND CAST(group_id as TEXT) = ?", new String[]{name, String.valueOf(group_id)});
        List<Transaction> transactionList = new ArrayList<>();

        for (Category c : categoryList) {
            for (Transaction t : getTransactions(TransactionTable.mTransactions, "category_id = ?", new String[]{c.getId().toString()})) {
                transactionList.add(t);
            }
        }
        double currentPeriodAmount = getCatsAmount(tableName, name, group_id, period);

        Map<String, Double> groupByDateTreeMap = new TreeMap<>();
        String firstDate;
        Integer timePassed = 0;


        switch (period) {

            case 5:
                DateTimeFormatter dailyFormatter = DateTimeFormat.forPattern("YYYY-MM-dd");

                Map<String, Double> unorderedDailyMap =
                        transactionList.stream().collect(Collectors.groupingBy(transaction -> dailyFormatter.print(transaction.getDateTime()), (Collectors.summingDouble(Transaction::getAmount))));

                groupByDateTreeMap.putAll(unorderedDailyMap); // Convert the HashMap into a TreeMap to order the element by key
                if (!groupByDateTreeMap.isEmpty()) {
                    firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                    timePassed = Days.daysBetween(dailyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getDays();
                }

                for (Map.Entry<String, Double> entryset : groupByDateTreeMap.entrySet()) {
                    Log.d(TAG, "key: " + entryset.getKey() + " value: " + entryset.getValue());
                }

                break;

            case 6:
                DateTimeFormatter weeklyFormatter = DateTimeFormat.forPattern("YYYY-ww");
                Map<String, Double> unorderedWeeklyMap =
                        transactionList.stream().collect(Collectors.groupingBy(transaction -> weeklyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedWeeklyMap); // Convert the HashMap into a TreeMap to order the element by key


                if (!groupByDateTreeMap.isEmpty()) {
                    firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                    timePassed = Weeks.weeksBetween(weeklyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getWeeks();
                }

                break;

            case 7:
                DateTimeFormatter monthlyFormatter = DateTimeFormat.forPattern("YYYY-MM");
                Map<String, Double> unorderedMontlyMap =
                        transactionList.stream().collect(Collectors.groupingBy(transaction -> monthlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedMontlyMap); // Convert the HashMap into a TreeMap to order the element by key

                if (!groupByDateTreeMap.isEmpty()) {
                    firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                    timePassed = Months.monthsBetween(monthlyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getMonths();
                }
                break;


            case 8:
                DateTimeFormatter yearlyFormatter = DateTimeFormat.forPattern("YYYY");
                Map<String, Double> unorderedYearlyMap =
                        transactionList.stream().collect(Collectors.groupingBy(transaction -> yearlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
                groupByDateTreeMap.putAll(unorderedYearlyMap); // Convert the HashMap into a TreeMap to order the element by key

                if (!groupByDateTreeMap.isEmpty()) {
                    firstDate = (String) groupByDateTreeMap.keySet().toArray()[0];
                    timePassed = Years.yearsBetween(yearlyFormatter.parseDateTime(firstDate).toLocalDate(), DateTime.now().toLocalDate()).getYears();
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + period);
        }

        double averagePeriodAmount = groupByDateTreeMap.values().stream().mapToDouble(Double::doubleValue).sum() / timePassed;

        Log.d(TAG, "AVERAGEPERIODCATEGORY IS: " + averagePeriodAmount);
        /*
        for (Map.Entry entry : groupByDateTreeSet.entrySet()) {
            Log.d("KEYVALUES", "AVE " + entry.getKey() + " VALUE " + entry.getValue());
        }


        Log.d("AVERAGE", "AVERAGE IS:" + averagePeriodAmount + " WITH GROUP" + group_id + " and period " + period);

         */
        double difference = currentPeriodAmount - averagePeriodAmount;
        double percentage = (difference / currentPeriodAmount) * 100;
        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No diff. >";
    }

    @Override
    public synchronized double getMonthlySavingsAmount() {

        List<Transaction> listIncomes = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(1)});
        List<Transaction> listExpenses = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(2)});

        double amountIncomes = 0;
        double amountExpenses = 0;


        for (Transaction t : listIncomes) {
            if (LocalDate.now().getMonthOfYear() == t.getDateTime().getMonthOfYear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                amountIncomes += t.getAmount();
            }
        }


        for (Transaction t : listExpenses) {
            if (LocalDate.now().getMonthOfYear() == t.getDateTime().getMonthOfYear() && LocalDate.now().getYear() == t.getDateTime().getYear()) {
                amountExpenses += t.getAmount();
            }
        }

        return amountIncomes - amountExpenses;
    }

    @Override
    public synchronized String getSavingsDiff() {
        double currentPeriodSavings = getMonthlySavingsAmount();
        double previousMonthIncomes = 0;
        double previousMonthExpenses = 0;
        List<Transaction> listIncomes = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(Category.Group.INCOMES)});
        List<Transaction> listExpenses = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(Category.Group.EXPENSES)});

        for (Transaction t : listIncomes) {
            if (LocalDate.now().minusMonths(1).getMonthOfYear() == t.getDateTime().getMonthOfYear()) {
                if (LocalDate.now().getMonthOfYear() == 1 && LocalDate.now().getYear() - 1 == t.getDateTime().getYear()
                        || LocalDate.now().getMonthOfYear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                ) {
                    previousMonthIncomes += t.getAmount();
                }
            }
        }

        for (Transaction t : listExpenses) {
            if (LocalDate.now().minusMonths(1).getMonthOfYear() == t.getDateTime().getMonthOfYear()) {
                if (LocalDate.now().getMonthOfYear() == 1 && LocalDate.now().getYear() - 1 == t.getDateTime().getYear()
                        || LocalDate.now().getMonthOfYear() != 1 && LocalDate.now().getYear() == t.getDateTime().getYear()
                ) {
                    previousMonthExpenses += t.getAmount();
                }
            }
        }

        double difference = currentPeriodSavings - (previousMonthIncomes - previousMonthExpenses);
        double percentage = (difference / currentPeriodSavings) * 100;
        Log.d(TAG, "AMOUNT currentperiod " + currentPeriodSavings);
        Log.d(TAG, "AMOUNT previousperiod " + (previousMonthIncomes - previousMonthExpenses));

        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No diff. >";

    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized String getSavingsDiffAvg() {
        List<Transaction> listIncomes = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(Category.Group.INCOMES)});
        List<Transaction> listExpenses = getTransactions(TransactionTable.mTransactions, "CAST(group_id as TEXT) = ?", new String[]{String.valueOf(Category.Group.EXPENSES)});

        double currentPeriodSavings = getMonthlySavingsAmount();

        Map<String, Double> groupByMonthlyDateInc = new TreeMap<>();
        Map<String, Double> groupByMonthlyDateExp = new TreeMap<>();

        DateTimeFormatter monthlyFormatter = DateTimeFormat.forPattern("YYYY-MM");
        Map<String, Double> groupByMonthlyDateIncUnsorted = listIncomes.stream().collect(Collectors.groupingBy(transaction -> monthlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));
        Map<String, Double> groupByMonthlyDateExpUnsorted = listExpenses.stream().collect(Collectors.groupingBy(transaction -> monthlyFormatter.print(transaction.getDateTime()), Collectors.summingDouble(Transaction::getAmount)));

        groupByMonthlyDateInc.putAll(groupByMonthlyDateIncUnsorted);
        groupByMonthlyDateExp.putAll(groupByMonthlyDateExpUnsorted);


        String firstDateInc=null;
        String firstDateExp=null;
        DateTime firstDateTimeInc;
        DateTime firstDateTimeExp;
        int timePassed=0;

                if(!groupByMonthlyDateInc.isEmpty()) {
                    firstDateInc = (String) groupByMonthlyDateInc.keySet().toArray()[0];

                }else if(!groupByMonthlyDateExp.isEmpty()) {
                    firstDateExp = (String) groupByMonthlyDateExp.keySet().toArray()[0];

                }else if(!(groupByMonthlyDateExp.isEmpty()&&groupByMonthlyDateExp.isEmpty())) {
            DateTime firstDateTimeGeneralTrans;
                    firstDateTimeInc = monthlyFormatter.parseDateTime(firstDateInc);

                    firstDateTimeExp = monthlyFormatter.parseDateTime(firstDateExp);

                    if (firstDateTimeInc.isBefore(firstDateTimeExp)) { //Check if which transaction is previous and set the DateTime object 'firstDaTimeGeneralTrans'
                firstDateTimeGeneralTrans = firstDateTimeInc;
            } else {
                firstDateTimeGeneralTrans = firstDateTimeExp;
            }

            timePassed = Months.monthsBetween(firstDateTimeGeneralTrans.toLocalDate(), DateTime.now().toLocalDate()).getMonths() + 1;
        }else{
            timePassed=0;
        }

        double averagePeriodAmountInc = groupByMonthlyDateInc.values().stream().mapToDouble(Double::doubleValue).sum() / timePassed;
        double averagePeriodAmountExp = groupByMonthlyDateExp.values().stream().mapToDouble(Double::doubleValue).sum() / timePassed;


        double difference = currentPeriodSavings - (averagePeriodAmountInc - averagePeriodAmountExp);
        double percentage = (difference / currentPeriodSavings) * 100;
        return difference > 0.0 ? String.format("+%s%.2f    +%.0f%%", MainActivity.CURRENCY_SYMBOL, difference, percentage) : difference < 0.0 ? String.format("-%s%.2f    %.0f%%", MainActivity.CURRENCY_SYMBOL, Math.abs(difference), percentage) : "< No diff. >";
    }
}
package com.robin.xBudget;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.robin.xBudget.database.DatabaseSchema.TransactionTable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.UUID;

public class GeneralCursorWrapper extends CursorWrapper {
    public GeneralCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    protected Transaction getTransaction() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");

        String id = getString(getColumnIndex(TransactionTable.TransCols.ID));
        Integer groupId = getInt(getColumnIndex(TransactionTable.TransCols.GROUP_ID));
        String categoryId = getString(getColumnIndex(TransactionTable.TransCols.CATEGORY_ID));
        String name = getString(getColumnIndex(TransactionTable.TransCols.NAME));
        Double amount = getDouble(getColumnIndex(TransactionTable.TransCols.AMOUNT));
        String description = getString(getColumnIndex(TransactionTable.TransCols.DESCRIPTION));
        String date = getString(getColumnIndex(TransactionTable.TransCols.DATE));
        //Log.d("TEST", " DATE IS: "+date);

        Transaction transaction = new Transaction(UUID.fromString(id));
        transaction.setGroupId(groupId);
        transaction.setCategoryId(UUID.fromString(categoryId));
        transaction.setName(name);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(formatter.parseDateTime(date));
        return transaction;
    }
    protected Category getCategory() {

        String id = getString(getColumnIndex(TransactionTable.CatCols.ID));
        Integer groupId = getInt(getColumnIndex(TransactionTable.CatCols.GROUP_ID));
        String name = getString(getColumnIndex(TransactionTable.CatCols.NAME));
        Double amount = getDouble(getColumnIndex(TransactionTable.CatCols.AMOUNT));
        Integer date = getInt(getColumnIndex(TransactionTable.CatCols.DATE));

        Category category = new Category(UUID.fromString(id));
        category.setGroupId(groupId);
        category.setName(name);
        category.setAmount(amount);
        category.setDateAssigned(date);

        return category;
    }

    protected DateTime getDistinctDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYM");
        String date = getString(getColumnIndex(TransactionTable.CatCols.DATE));
        DateTime dateTime = formatter.parseDateTime(date);

        return dateTime;
    }
}

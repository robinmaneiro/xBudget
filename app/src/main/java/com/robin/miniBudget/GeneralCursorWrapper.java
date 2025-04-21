package com.robin.miniBudget;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.robin.miniBudget.database.DatabaseSchema.TransactionTable;

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

        String id = getString(getColumnIndex(TransactionTable.TransactionColumns.ID));
        Integer groupId = getInt(getColumnIndex(TransactionTable.TransactionColumns.GROUP_ID));
        String categoryId = getString(getColumnIndex(TransactionTable.TransactionColumns.CATEGORY_ID));
        String name = getString(getColumnIndex(TransactionTable.TransactionColumns.NAME));
        Double amount = getDouble(getColumnIndex(TransactionTable.TransactionColumns.AMOUNT));
        String description = getString(getColumnIndex(TransactionTable.TransactionColumns.DESCRIPTION));
        String date = getString(getColumnIndex(TransactionTable.TransactionColumns.DATE));

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

        String id = getString(getColumnIndex(TransactionTable.CategoryColumns.ID));
        Integer groupId = getInt(getColumnIndex(TransactionTable.CategoryColumns.GROUP_ID));
        String name = getString(getColumnIndex(TransactionTable.CategoryColumns.NAME));
        Double amount = getDouble(getColumnIndex(TransactionTable.CategoryColumns.AMOUNT));
        Integer date = getInt(getColumnIndex(TransactionTable.CategoryColumns.DATE));

        Category category = new Category(UUID.fromString(id));
        category.setGroupId(groupId);
        category.setName(name);
        category.setAmount(amount);
        category.setDateAssigned(date);

        return category;
    }

    protected DateTime getDistinctDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYM");
        String date = getString(getColumnIndex(TransactionTable.CategoryColumns.DATE));
        DateTime dateTime = formatter.parseDateTime(date);

        return dateTime;
    }
}

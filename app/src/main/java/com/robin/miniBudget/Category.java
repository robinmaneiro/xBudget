package com.robin.miniBudget;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Objects;
import java.util.UUID;

public class Category {

    private UUID mId;
    private Integer mGroupId;

    public void setDateAssigned(Integer dateAssigned) {
        mDateAssigned = dateAssigned;
    }

    private Integer mDateAssigned;
    private String mName;
    private Double mAmount;

    public Category() {
        this(UUID.randomUUID());
    }

    public Category(UUID id) {
        mId = id;
    }

    public Category(String name, Double amount, Integer groupId) {
        this();
        mName = name;
        mAmount = amount;
        mGroupId = groupId;
        DateTime mDateCreated = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYM");
        mDateAssigned = Integer.valueOf(fmt.print(mDateCreated));
    }

    public Integer getDateAssigned() {
        return mDateAssigned;
    }

    public Integer getGroupId() {
        return mGroupId;
    }

    public void setGroupId(Integer groupId) {
        this.mGroupId = groupId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Double getAmount() {
        return mAmount;
    }

    public void setAmount(Double amount) {
        mAmount = amount;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;

        return mGroupId.equals(category.mGroupId) &&
                mDateAssigned.equals(category.mDateAssigned) &&
                mName.equals(category.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mGroupId, mDateAssigned, mName);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getName();
    }

    public static class Group {
        public static final int INCOMES = 1;
        public static final int EXPENSES = 2;
        public static final int SAVINGS = 3;
    }
}

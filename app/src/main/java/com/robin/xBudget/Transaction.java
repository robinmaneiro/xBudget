package com.robin.xBudget;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.UUID;

public class Transaction {

    private UUID mId;
    private Integer groupId;
    private UUID mCategoryId;
    private String mName, mDescription;
    private Double mAmount;
    private DateTime mDateTime;

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public void setDate(DateTime dateTime) {
        mDateTime = dateTime;
    }

    public Transaction() {
        this(UUID.randomUUID());
    }

    public Transaction(UUID id){
        mId = id;
    }
    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public UUID getCategoryId() {
        return mCategoryId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setCategoryId(UUID categoryId) {
        mCategoryId = categoryId;
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


    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yy HH:mm");
        return

               fmt.print(getDateTime()) + "   " + getName() + "   " + getAmount();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Transaction)return this.getId().equals(((Transaction) o).getId());
        return false;
    }

    @Override
    public int hashCode(){
        return 1;
    }
}

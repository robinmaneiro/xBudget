package com.robin.miniBudget.database;

public class DatabaseSchema {

    /**
     * Class for defining the columns of the database
     */

    public static final class TransactionTable {

        private final String TAG = this.getClass().getSimpleName();

        public static final String mTransactions = "transactions";
        public static final String mCategories = "categories";
        public static final String mGroups = "groups";

        public static final class TransCols {
            public static final String ID = "id";
            public static final String GROUP_ID = "group_id";
            public static final String CATEGORY_ID = "category_id";
            public static final String NAME = "name";
            public static final String AMOUNT = "amount";
            public static final String DATE = "date";
            public static final String DESCRIPTION = "description";
        }

        public static final class CatCols{
            public static final String ID = "id";
            public static final String GROUP_ID = "group_id";
            public static final String NAME = "name";
            public static final String AMOUNT = "amount";
            public static final String DATE = "date";
        }

        public static final class GroupCols {
            public static final String ID = "id";
            public static final String NAME = "name";
        }
    }
}
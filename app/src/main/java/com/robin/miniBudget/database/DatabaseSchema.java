package com.robin.miniBudget.database;

public class DatabaseSchema {

    /**
     * Class for defining the columns of the database
     */
    public static final class TransactionTable {

        public static final String mTransactions = "transactions";
        public static final String mCategories = "categories";
        public static final String mGroups = "groups";
        public static final String mConstants = "constants";
        public static final class TransactionColumns {
            public static final String ID = "id";
            public static final String GROUP_ID = "group_id";
            public static final String CATEGORY_ID = "category_id";
            public static final String NAME = "name";
            public static final String AMOUNT = "amount";
            public static final String DATE = "date";
            public static final String DESCRIPTION = "description";
        }

        public static final class CategoryColumns {
            public static final String ID = "id";
            public static final String GROUP_ID = "group_id";
            public static final String NAME = "name";
            public static final String AMOUNT = "amount";
            public static final String DATE = "date";
        }

        public static final class GroupColumns {
            public static final String ID = "id";
            public static final String NAME = "name";
        }

        public static final class ConstantColumns {
            public static final String NAME = "name";
            public static final String VALUE = "value";
        }
    }
}
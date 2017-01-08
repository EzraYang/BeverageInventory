package com.example.android.beverageinventory.data;

import android.provider.BaseColumns;

/**
 * Created by EzraYang on 1/8/17.
 */


public class ProductContract {

    private ProductContract(){
    }

    public static class ProductEntry implements BaseColumns{

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String  COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_PICURI = "picture";
    }


}

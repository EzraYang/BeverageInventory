package com.example.android.beverageinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by EzraYang on 1/8/17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION =1;
    public static final String DATABASE_NAME = "inventory.db";

    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES =  "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME
                + " (" + ProductContract.ProductEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRICE + " INTEGER, "
                + ProductContract.ProductEntry.COLUMN_QUANTITY + " INTEGER, "
                + ProductContract.ProductEntry.COLUMN_SUPPLIER + " TEXT, "
                + ProductContract.ProductEntry.COLUMN_PICURI + " TEXT)";
        Log.i(LOG_TAG, "SQL_CREATE_ENTRIES String is " + SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}

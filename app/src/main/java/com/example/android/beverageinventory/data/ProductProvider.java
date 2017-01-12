package com.example.android.beverageinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.id;

/**
 * Created by EzraYang on 1/9/17.
 */

public class ProductProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private ProductDbHelper mDbHelper;

    /** URI matcher code for the content URI for the pets table */
    private static final int PROD_WHOLE = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PROD_SINGLE = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT, PROD_WHOLE);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT + "/#", PROD_SINGLE);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD_WHOLE:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PROD_SINGLE:
                selection = ProductContract.ProductEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknow uri" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }
    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PROD_WHOLE:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){
        String nameString = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
        Integer priceInt = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
        Integer quantityInt = values.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY);

        if (nameString == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        if (priceInt != null && priceInt < 0) {
            throw new IllegalArgumentException("Negative price");
        }
        if (quantityInt != null && quantityInt < 0){
            throw new IllegalArgumentException("Negative quantity");
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        Log.v(LOG_TAG, "New row id is " + newRowId);
        if ( id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // only offer single-updation
            case PROD_SINGLE:
                selection = ProductContract.ProductEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId((uri)))};
                return updateProd(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProd (Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // sanity check
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_NAME)){
            String nameString = values.getAsString(ProductContract.ProductEntry.COLUMN_NAME);
            if (nameString == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRICE)) {
            Integer priceInt = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
            if (priceInt != null && priceInt < 0) {
                throw new IllegalArgumentException("Negative price");
            }
        }

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_QUANTITY)) {
            Integer quantityInt = values.getAsInteger(ProductContract.ProductEntry.COLUMN_QUANTITY);
            if (quantityInt != null && quantityInt < 0) {
                throw new IllegalArgumentException("Negative price");
            }
        }

        // there's no constrain on supplier and uri info

        // if values dont have any row, no need to bother mDbHelper to inflate a database
        if (values.size() == 0) {
            return 0;
        }

        // Update the selected pets in the pets database table with the given ContentValues
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Return the number of rows that were affected

        int count = db.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            // only offer single-deletion
            case PROD_SINGLE:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // this will always be 1
                int numOfDelRow = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);

                getContext().getContentResolver().notifyChange(uri, null);
                return numOfDelRow;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD_WHOLE:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PROD_SINGLE:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri + "with unknow match: " + match);
        }
    }


}

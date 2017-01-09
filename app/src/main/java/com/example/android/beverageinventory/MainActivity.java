package com.example.android.beverageinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.beverageinventory.data.ProductContract;
import com.example.android.beverageinventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = MainActivity.class.getSimpleName();

    private ProductDbHelper mDbHelper;

    private ProductCursorAdapter mAdapter;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView productListView = (ListView) findViewById(R.id.list);
        mAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

////                Uri uriOfClickedProduct = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
//                Intent openEditorAct = new Intent(MainActivity.this, InfoActivity.class);
////                openEditorAct.setData(uriOfClickedPet);
//                startActivity(openEditorAct);
            }
        });

//        测试database是否创建正确
        mDbHelper = new ProductDbHelper(this);

        insertDummyData();
        displayDatabaseInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void insertDummyData(){
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, "Coffee");
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, "30");
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, "100");
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, "David");
        // an empty pic uri for now
        values.put(ProductContract.ProductEntry.COLUMN_PICURI, "");

        db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        Log.i("CatalogActivity", "New row id is " + newRowId);

    }

    private void displayDatabaseInfo(){

        db = mDbHelper.getReadableDatabase();

        // the simplest manual query
        Cursor cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,
                null, null, null, null, null, null);
        mAdapter.swapCursor(cursor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_add:
                // jump to @InfoActivity
                Intent addProduct = new Intent(this, InfoActivity.class);
                startActivity(addProduct);
                return true;
            default:
                Log.e(LOG_TAG, "Problem jumping to InfoActivity");
                return false;
        }
    }

}

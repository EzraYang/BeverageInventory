package com.example.android.beverageinventory;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.beverageinventory.data.ProductContract;

public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = MainActivity.class.getSimpleName();

    private ProductCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView productListView = (ListView) findViewById(R.id.list);
        mAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mAdapter);

        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uriOfClickedProd = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                // open @InfoActivity in edit mode with item uri
                Intent openInfoAct = new Intent(MainActivity.this, InfoActivity.class);
                openInfoAct.setData(uriOfClickedProd);
                startActivity(openInfoAct);
            }
        });

        displayDatabaseInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo(){

        String [] projection = {
            ProductContract.ProductEntry.COLUMN_ID,
            ProductContract.ProductEntry.COLUMN_NAME,
            ProductContract.ProductEntry.COLUMN_PRICE,
            ProductContract.ProductEntry.COLUMN_QUANTITY,
            ProductContract.ProductEntry.COLUMN_SUPPLIER,
            ProductContract.ProductEntry.COLUMN_PICURI
        };

        Cursor cursor = getContentResolver().query(ProductContract.ProductEntry.CONTENT_URI , projection, null, null, null, null);

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

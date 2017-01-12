package com.example.android.beverageinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beverageinventory.data.ProductContract;

/**
 * Created by EzraYang on 1/9/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // find fields in view inflated from list_item
        TextView nameField = (TextView) view.findViewById(R.id.lstItm_nameField);
        TextView priceField = (TextView) view.findViewById(R.id.lstItm_priceField);
        final TextView quantityField = (TextView) view.findViewById(R.id.lstItm_quantityField);
        final Button sellBtn = (Button) view.findViewById(R.id.lstItm_sellBtn);

        // get column index for all columns by column name
        int _idColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);

        // get name, price, quantity value to display on screen
        String nameString = cursor.getString(nameColumnIndex);
        String priceString = cursor.getString(priceColumnIndex);
        int quantityInt = cursor.getInt(quantityColumnIndex);
        Log.i("CursorAdapter", "mQuantity is " + quantityInt);

        // get supplier, pictureUri to prepare construction new content value
        final int idInt = cursor.getInt(_idColumnIndex);

        // set string value of name price and quantity to responding places in template
        nameField.setText(nameString);
        priceField.setText(priceString);
        quantityField.setText(String.valueOf(quantityInt));

        sellBtn.setFocusable(false);

        // wrap quantityInt in a final array,
        // so it can be both reached and modified from within onClick inner class
        final int[] quantity = {new Integer(quantityInt)};

        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity[0] > 1){
                    quantity[0] -= 1;
                    quantityField.setText(String.valueOf(quantity[0]));

                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantity[0]);

                    Uri uri = Uri.parse(ProductContract.ProductEntry.CONTENT_URI + "/" + idInt);
                    // update居然可以只更改发生改变的column!!
                    context.getContentResolver().update(uri, values,null, null);
                } else {
                    Toast.makeText(context, "Click on the product then delete it", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

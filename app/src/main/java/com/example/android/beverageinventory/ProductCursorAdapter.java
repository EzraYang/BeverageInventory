package com.example.android.beverageinventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
    public void bindView(View view, Context context, Cursor cursor) {
        // find fields in view inflated from list_item
        TextView nameField = (TextView) view.findViewById(R.id.bev_nameField);
        TextView priceField = (TextView) view.findViewById(R.id.bev_priceField);
        TextView quantityField = (TextView) view.findViewById(R.id.bev_quantityField);


        // get column index for both name and breed by column name
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);

        // get the String value of name and breed out from cursor
        String nameString = cursor.getString(nameColumnIndex);
        String priceString = cursor.getString(priceColumnIndex);
        String quantityString = cursor.getString(quantityColumnIndex);

        // set string value of name and breed to responding places in template
        nameField.setText(nameString);
        priceField.setText(priceString);
        quantityField.setText(quantityString);
    }
}

package com.example.android.beverageinventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.beverageinventory.data.ProductContract;

/**
 * Created by EzraYang on 1/9/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private int mQuantityInt;

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
        TextView nameField = (TextView) view.findViewById(R.id.lstItm_nameField);
        TextView priceField = (TextView) view.findViewById(R.id.lstItm_priceField);
        final TextView quantityField = (TextView) view.findViewById(R.id.lstItm_quantityField);
        Button sellBtn = (Button) view.findViewById(R.id.lstItm_sellBtn);

        // get column index for name, price and quantity by column name
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);

        // get the String value of name and breed out from cursor
        String nameString = cursor.getString(nameColumnIndex);
        String priceString = cursor.getString(priceColumnIndex);
//        final String quantityString = cursor.getString(quantityColumnIndex);
        mQuantityInt = cursor.getInt(quantityColumnIndex);

        // set string value of name and breed to responding places in template
        nameField.setText(nameString);
        priceField.setText(priceString);
        quantityField.setText(String.valueOf(mQuantityInt));

        sellBtn.setFocusable(false);

        // TODO: to make the sell btn actually modify database using update
        // now it's only modify temporary storage
        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mQuantityInt > 1) {
                    mQuantityInt -= 1;
                    quantityField.setText(String.valueOf(mQuantityInt));
                }
            }
        });
    }
}

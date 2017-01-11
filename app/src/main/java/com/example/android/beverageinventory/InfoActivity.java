package com.example.android.beverageinventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beverageinventory.data.ProductContract;
import com.example.android.beverageinventory.data.ProductDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.view.View.GONE;
import static com.example.android.beverageinventory.data.ProductDbHelper.LOG_TAG;

public class InfoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    private Uri mUriOfUploadedPic;

//    5 textviews responding to database value
    private TextView mNameField;
    private TextView mPriceField;
    private TextView mQuantityField;
    private TextView mSupplierField;

//    2 textviews related to change quantity
    private TextView mIncreseField;
    private TextView mDecreaseField;

//    2 views related to image
    private ImageView mImageField;
    private TextView mImageHint;

    private Uri mUriOfClickedProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Find all relevant views that we will need to read user input from
        mNameField = (TextView) findViewById(R.id.info_nameField);
        mPriceField = (TextView) findViewById(R.id.info_priceField);
        mQuantityField = (TextView) findViewById(R.id.info_quantityField);
        mSupplierField = (TextView) findViewById(R.id.info_supplierField);
        mIncreseField = (TextView) findViewById(R.id.info_increseField);
        mDecreaseField = (TextView) findViewById(R.id.info_decreseField);
        mImageField = (ImageView) findViewById(R.id.info_imageField);
        mImageHint = (TextView) findViewById(R.id.info_imageHint);

        mUriOfClickedProd = getIntent().getData();
        // if CatalogActivity didnt send any uri
        // then start InfoActivity in add mode
        if (mUriOfClickedProd == null){
            Log.i(LOG_TAG, "uri of the clicked prod is null" );
            setTitle(R.string.title_add_prod);

            // hide the update quantity field
            LinearLayout updateField = (LinearLayout) findViewById(R.id.info_updateField);
            updateField.setVisibility(GONE);

            // hide the ORDER MORE button
            Button orderMoreBtn = (Button) findViewById(R.id.info_orderMoreBtn);
            orderMoreBtn.setVisibility(GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        // else MainActivity did send an uri
        // start InfoActivity in edit mode
        // and get details of clicked product
        else {
            setTitle(R.string.title_edit_prod);
            Log.i(LOG_TAG, "uri of the clicked product is: " + mUriOfClickedProd);

            // fetch info about this prod and display it
            fetchProdInfo();
        }

        // to select a image
        RelativeLayout imageField = (RelativeLayout) findViewById(R.id.image_field);
        imageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

//  methods below are called only in ADD mode
    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mUriOfClickedProd == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


// methods below are called only in EDIT mode
    private void fetchProdInfo(){
        // select all column
        String [] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_PICURI
        };

        Cursor cursorOfClkProd = getContentResolver().query(mUriOfClickedProd, projection,
                null, null, null);

        // Bail out early if the cursor is null or there is less than 1 row in the cursor
        if (cursorOfClkProd == null || cursorOfClkProd.getCount() < 1){
            return;
        }

        // by default, the return cursor is at -1 row (the header row),
        // we have to move it to the 1st row
        // cursor.moveToFirst() return a boolean indicating whether the move is success or not
        if (cursorOfClkProd.moveToFirst()){
            // get column id for 4 columns needed
            int nameColumnIndex = cursorOfClkProd.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
            int priceColumnIndex = cursorOfClkProd.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursorOfClkProd.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursorOfClkProd.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER);
            int picuriColumnIndex = cursorOfClkProd.getColumnIndex(ProductContract.ProductEntry.COLUMN_PICURI);

            // retrieve responding data according to column ids
            String nameString = cursorOfClkProd.getString(nameColumnIndex);
            Integer priceInteger = cursorOfClkProd.getInt(priceColumnIndex);
            Integer quantityInteger = cursorOfClkProd.getInt(quantityColumnIndex);
            String supplierString = cursorOfClkProd.getString(supplierColumnIndex);
            String picUriString = cursorOfClkProd.getString(picuriColumnIndex);

            Uri picUri = Uri.parse(picUriString);

            // update UI
            mNameField.setText(nameString);
            mPriceField.setText(String.valueOf(priceInteger));
            mQuantityField.setText(String.valueOf(quantityInteger));
            mSupplierField.setText(supplierString);
            mImageField.setImageBitmap(getBitmapFromUri(picUri));

            mImageHint.setVisibility(View.GONE);
        }
    }


// methods below are called in BOTH ADD and EDIT mode
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            // can only be called from edit mode
            case R.id.action_delete:
                deleteProduct();
                finish();
                return true;
//            // Respond to a click on the "Up" arrow button in the app bar
//            case android.R.id.home:
//                // If the pet hasn't changed, continue with navigating up to parent activity
//                // which is the {@link CatalogActivity}.
//                if (!mPetHasChanged) {
//                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
//                    return true;
//                }

//                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
//                // Create a click listener to handle the user confirming that
//                // changes should be discarded.
//                DialogInterface.OnClickListener discardButtonClickListener =
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                // User clicked "Discard" button, navigate to parent activity.
//                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
//                            }
//                        };
//
//                // Show a dialog that notifies the user they have unsaved changes
//                showUnsavedChangesDialog(discardButtonClickListener);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct(){
        String nameString = mNameField.getText().toString().trim();
        String priceString = mPriceField.getText().toString().trim();
        String quantityString = mQuantityField.getText().toString().trim();
        String supplierString = mSupplierField.getText().toString().trim();
        String uriString = mUriOfUploadedPic.toString();

        // TODO: sanity check

        // Create a new map of values, where column names are the keys
        ContentValues value = new ContentValues();
        value.put(ProductContract.ProductEntry.COLUMN_NAME, nameString);
        value.put(ProductContract.ProductEntry.COLUMN_PRICE, priceString);
        value.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityString);
        value.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, supplierString);
        value.put(ProductContract.ProductEntry.COLUMN_PICURI, uriString);


        ProductDbHelper mDbHelper = new ProductDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        if (mUriOfClickedProd == null){
            Uri newRowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, value);
            Log.i("EditorActivity", "New row uri is" + newRowUri);

            // show a toast message depending on whether or not the insertion is successful
            if (newRowUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_prod_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_prod_successful),
                        Toast.LENGTH_SHORT).show();
            }


        } else {
            // update the prod
        }

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, value);
    }

    private void deleteProduct(){
        // confirming delePet called from editor mode
        if (mUriOfClickedProd != null) {
            int numOfRowDel = getContentResolver().delete(mUriOfClickedProd, null, null);
            Log.i(LOG_TAG, numOfRowDel + "row(s) deleted");

            if (numOfRowDel == 1) {
                Toast.makeText(this, getString(R.string.editor_delete_prod_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_prod_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // not in edit mode
        else {
            Log.i(LOG_TAG, "Sth wrong with callling deletePet");
        }

    }


//  methods below are ones to manipulate pic uploading and get pic uri
    // method to open image selector
    private void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // method to retrieve uri of the selected image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUriOfUploadedPic = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUriOfUploadedPic.toString());

                mImageField.setImageBitmap(getBitmapFromUri(mUriOfUploadedPic));
                mImageHint.setVisibility(View.GONE);
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }

    // method to set imageField to the selected pic using its uri
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // origin method
//         Get the dimensions of the View
//        int targetW = mImageField.getWidth();
//        int targetH = mImageField.getHeight();

        // method 1 , force mesure
//        mImageField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        int targetW = mImageField.getMeasuredWidth();
//        int targetH = mImageField.getMeasuredHeight();

        // method 2, manually set width and height
        // ok this works, though not best solution
        int targetW = 120;
        int targetH = 120;
        Log.i(LOG_TAG, "targetW is " + targetW + "; targetH is " + targetH);

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


}

package com.example.android.beverageinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beverageinventory.data.ProductContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.view.View.GONE;
import static com.example.android.beverageinventory.data.ProductDbHelper.LOG_TAG;

public class InfoActivity extends AppCompatActivity {

    private Uri mUriOfClickedProd;

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    private Uri mUriOfUploadedPic = null;
    private Uri mUriOfOldPic = null;

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

    private int mQuantityInt;
    private String mNameString;
    private String mSupplierString;

    private boolean mProdHasChanged = false;
    private boolean mProdSaved = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProdHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Find all relevant views that we will need to read user input from
        mNameField = (TextView) findViewById(R.id.info_nameField);
        mPriceField = (TextView) findViewById(R.id.info_priceField);
        mQuantityField = (TextView) findViewById(R.id.info_quantityField);
        mSupplierField = (TextView) findViewById(R.id.info_supplierField);
        mImageField = (ImageView) findViewById(R.id.info_imageField);
        mImageHint = (TextView) findViewById(R.id.info_imageHint);

        mUriOfClickedProd = getIntent().getData();
        // if CatalogActivity didnt send any uri then start InfoActivity in add mode
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
            invalidateOptionsMenu();
        }
        // else MainActivity did send an uri
        // start InfoActivity in edit mode, and get details of clicked product
        else {
            setTitle(R.string.title_edit_prod);
            Log.i(LOG_TAG, "uri of the clicked product is: " + mUriOfClickedProd);

            // fetch info about this prod and display it
            fetchProdInfo();

            // initialize 2 incre/decrease textview that only appears in edit mode
            mIncreseField = (EditText) findViewById(R.id.info_increseField);
            mDecreaseField = (EditText) findViewById(R.id.info_decreseField);

            // initialize 3 buttons that only appears in Edit mode
            Button mIncreaseBtn = (Button) findViewById(R.id.info_increaseBtn);
            Button mDecreaseBtn = (Button) findViewById(R.id.info_decreaseBtn);
            Button mOrderMoreBtn = (Button) findViewById(R.id.info_orderMoreBtn);

            // attach onTouchListener to check if prod is updated
            mNameField.setOnTouchListener(mTouchListener);
            mPriceField.setOnTouchListener(mTouchListener);
            mQuantityField.setOnTouchListener(mTouchListener);
            mSupplierField.setOnTouchListener(mTouchListener);
            mIncreaseBtn.setOnTouchListener(mTouchListener);
            mDecreaseBtn.setOnTouchListener(mTouchListener);

            mIncreaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    increaseQuantity();
                }
            });

            mDecreaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    decreaseQuantity();
                }
            });

            mOrderMoreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderMore();
                }
            });

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
                ProductContract.ProductEntry.COLUMN_ID,
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
            mNameString = cursorOfClkProd.getString(nameColumnIndex);
            Integer priceInteger = cursorOfClkProd.getInt(priceColumnIndex);
            mQuantityInt = cursorOfClkProd.getInt(quantityColumnIndex);
            mSupplierString = cursorOfClkProd.getString(supplierColumnIndex);
            String picUriString = cursorOfClkProd.getString(picuriColumnIndex);

            // update UI
            mNameField.setText(mNameString);
            mPriceField.setText(String.valueOf(priceInteger));
            mQuantityField.setText(String.valueOf(mQuantityInt));
            mSupplierField.setText(mSupplierString);

            if(picUriString.isEmpty() || picUriString.length() == 0 || picUriString.equals("") || picUriString == null){
                // do nothing
            } else {
                mUriOfOldPic = Uri.parse(picUriString);
                mImageField.setImageBitmap(getBitmapFromUri(mUriOfOldPic));
                mImageHint.setVisibility(View.GONE);
            }
        }
    }

    private void increaseQuantity(){
        String incrementString = mIncreseField.getText().toString().trim();

        if(incrementString.isEmpty() || incrementString.length() == 0 || incrementString.equals("") || incrementString == null)
        {
            //EditText is empty, make a toast
            Toast.makeText(this, getString(R.string.editor_enter_num),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            //EditText is not empty
            int incrementInt = Integer.parseInt(incrementString);
            mQuantityInt += incrementInt;
            mQuantityField.setText(String.valueOf(mQuantityInt));
            mIncreseField.setText("");
        }
    }

    private void decreaseQuantity(){
        String decrementString = mDecreaseField.getText().toString().trim();

        if(decrementString.isEmpty() || decrementString.length() == 0 || decrementString.equals("") || decrementString == null)
        {
            //EditText is empty, make a toast
            Toast.makeText(this, getString(R.string.editor_enter_num),
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            //EditText is not empty
            int decrementInt = Integer.parseInt(decrementString);
            int newQuantityInt = mQuantityInt - decrementInt;
            if (newQuantityInt < 0){
                Toast.makeText(this, getString(R.string.editor_decrement_failed),
                        Toast.LENGTH_SHORT).show();
                mDecreaseField.setText("");
            }
            else {
                mQuantityInt = newQuantityInt;
                mQuantityField.setText(String.valueOf(mQuantityInt));
                mDecreaseField.setText("");
            }
        }
    }

    private void orderMore(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("message/rfc822");

        String mailBody = "Hi " + mSupplierString + "! \n" +
                            "\nI'd like to order more of " + mNameString + "!\n" +
                            "\nThank you!";
        emailIntent.putExtra(Intent.EXTRA_TEXT, mailBody);

        startActivity(Intent.createChooser(emailIntent, "Send e-mail via"));
    }

    private void deleteProduct(){
        // confirming delePet called from editor mode
        if (mUriOfClickedProd != null) {
            int numOfRowDel = getContentResolver().delete(mUriOfClickedProd, null, null);
            Log.i(LOG_TAG, numOfRowDel + "row(s) deleted");

            if (numOfRowDel == 1) {
                Toast.makeText(this, getString(R.string.editor_delete_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // not in edit mode
        else {
            Log.i(LOG_TAG, "Sth wrong with callling deletePet");
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                if (mProdSaved){
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            // can only be called from edit mode
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct(){
        String nameString = mNameField.getText().toString().trim();
        String priceString = mPriceField.getText().toString().trim();
        String quantityString = mQuantityField.getText().toString().trim();
        String supplierString = mSupplierField.getText().toString().trim();

        // user may accidentally hit save button with nothing input, don't save it!
        if ( mUriOfClickedProd == null
                && (TextUtils.isEmpty(nameString)
                || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(supplierString)
                || mUriOfUploadedPic == null) ) {
            Toast.makeText(this, getString(R.string.editor_lack_info),
                    Toast.LENGTH_SHORT).show();
        } else{
            String uriString;
            if (mUriOfUploadedPic != null) {
                // new contentvalue should use uri of newly uploaded pic
                // no matter in Add mode or in Edit mode
                uriString = mUriOfUploadedPic.toString();
            } else {
                // Add mode, no pic uploaded
                if (mUriOfClickedProd == null){
                    uriString = "";
                }
                // Edit mode, no pic uploaded, and there is an old pic
                else if (mUriOfOldPic != null && mUriOfClickedProd != null){
                    uriString = mUriOfOldPic.toString();
                }
                // Edit mode, no pic uploaded, and no old pic as well
                else {
                    uriString = "";
                }
            }

            // Create a new map of values, where column names are the keys
            ContentValues value = new ContentValues();
            value.put(ProductContract.ProductEntry.COLUMN_NAME, nameString);
            value.put(ProductContract.ProductEntry.COLUMN_PRICE, priceString);
            value.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityString);
            value.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, supplierString);
            value.put(ProductContract.ProductEntry.COLUMN_PICURI, uriString);

            if (mUriOfClickedProd == null){
                Uri newRowUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, value);
                Log.i("EditorActivity", "New row uri is" + newRowUri);

                // show a toast message depending on whether or not the insertion is successful
                if (newRowUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_successful),
                            Toast.LENGTH_SHORT).show();
                    mProdSaved = true;
                }
            } else {
                if (mProdHasChanged){
                    // update the prod
                    int numOfRowUpdated = getContentResolver().update(mUriOfClickedProd, value, null, null);
                    Log.i(LOG_TAG, numOfRowUpdated + "rows updated");

                    if (numOfRowUpdated == 1) {
                        // update succeed
                        Toast.makeText(this, getString(R.string.editor_update_successful),
                                Toast.LENGTH_SHORT).show();
                        mProdSaved = true;
                    } else {
                        // update failed
                        Toast.makeText(this, getString(R.string.editor_update_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // user didn't touch any changable field
                    Toast.makeText(this, getString(R.string.editor_update_nothing),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProdHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

        // dimensions were fixed
        int targetW = 120;
        int targetH = 120;

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

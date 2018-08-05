package com.example.android.bookstoreapp;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.Contract.BookEntry;

import java.util.Currency;
import java.util.Locale;

import static android.telephony.PhoneNumberUtils.normalizeNumber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    private TextView nameEditText;
    private TextView priceEditText;
    private TextView quantityEditText;
    private TextView supplierNameEditText;
    private TextView supplierPhoneEditText;
    private Uri currentBookUri;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        currentBookUri = intent.getData();

        setTitle(getString(R.string.details_activity_label));
        getLoaderManager().initLoader(BOOK_LOADER, null, this);


        // Find all relevant views needed to read user input
        nameEditText = (TextView) findViewById(R.id.edit_book_name);
        priceEditText = (TextView) findViewById(R.id.edit_book_price);
        quantityEditText = (TextView) findViewById(R.id.edit_book_quantity);
        supplierNameEditText = (TextView) findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = (TextView) findViewById(R.id.edit_supplier_phone);
        TextView currencyView = (TextView) findViewById(R.id.currency);
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        currencyView.setText(symbol);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menu option clicked on
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.back_to_book_list:
                Intent intent = new Intent(DetailActivity.this, CatalogActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.go_to_editor:

                Intent i = new Intent(DetailActivity.this, EditorActivity.class);
                i.setData(currentBookUri);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE
        };
        return new CursorLoader(this, currentBookUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(String.valueOf(BookEntry.COLUMN_BOOK_QUANTITY));
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            int currentID = cursor.getInt(idColumnIndex);
            String currentName = cursor.getString(nameColumnIndex);
            String currentPrice = cursor.getString(priceColumnIndex);
            int currentQuantity = cursor.getInt(quantityColumnIndex);
            String currentSupplierName = cursor.getString(supplierNameColumnIndex);
            String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
            normalizeNumber(currentSupplierPhone);
            currentSupplierPhone = PhoneNumberUtils.formatNumber(currentSupplierPhone,
                    Locale.getDefault().getCountry());

            nameEditText.setText(currentName);
            priceEditText.setText(currentPrice);
            quantityEditText.setText(Integer.toString(currentQuantity));
            supplierNameEditText.setText(currentSupplierName);
            supplierPhoneEditText.setText(currentSupplierPhone);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            final String finalCurrentSupplierPhone = currentSupplierPhone;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(finalCurrentSupplierPhone!= null){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts
                            ("tel", finalCurrentSupplierPhone, null));
                    startActivity(intent);
                    }else{
                       return;
                    }
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");

    }

}



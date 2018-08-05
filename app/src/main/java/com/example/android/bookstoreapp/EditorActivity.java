package com.example.android.bookstoreapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.Contract;
import com.example.android.bookstoreapp.data.Contract.BookEntry;

import java.util.Currency;
import java.util.Locale;

import static android.telephony.PhoneNumberUtils.normalizeNumber;
import static com.example.android.bookstoreapp.data.Contract.BookEntry.COLUMN_BOOK_PRICE;
import static com.example.android.bookstoreapp.data.Contract.BookEntry.COLUMN_BOOK_QUANTITY;
import static com.example.android.bookstoreapp.data.Contract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME;
import static com.example.android.bookstoreapp.data.Contract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE;
import static com.example.android.bookstoreapp.data.Contract.BookEntry.COLUMN_BOOK_TITLE;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private boolean bookHasChanged = false;
    private Uri currentBookUri;
    int quantityAddABook;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentBookUri = intent.getData();

        if (currentBookUri == null) {

            setTitle(getString(R.string.add_a_book_editor_activity_label));
            invalidateOptionsMenu();

        } else {

            setTitle(getString(R.string.edit_book_editor_activity_label));
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        nameEditText = (EditText) findViewById(R.id.edit_book_name);
        priceEditText = (EditText) findViewById(R.id.edit_book_price);
        quantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        supplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        TextView currencyView = (TextView) findViewById(R.id.currency);
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        currencyView.setText(symbol);
        quantityEditText.setText("0");

        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

        ImageButton minusButton = (ImageButton) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                quantityAddABook--;
                if (quantityAddABook < 0) {
                    Toast.makeText(EditorActivity.this, R.string.lower_than_0_warning,
                            Toast.LENGTH_SHORT).show();
                    quantityAddABook = 0;
                }

                displayQuantity(quantityAddABook);

            }
        });
        ImageButton plusButton = (ImageButton) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                quantityAddABook++;
                displayQuantity(quantityAddABook);

            }
        });
    }

    private void displayQuantity(int quantityAddABook) {

        TextView quantity = (TextView) findViewById(R.id.edit_book_quantity);
        quantity.setText(String.valueOf(quantityAddABook));
    }

    private void saveBook() {

        String title = nameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        final String quantity = quantityEditText.getText().toString().trim();
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString();

        if (currentBookUri == null &&
                TextUtils.isEmpty(title) &&
                TextUtils.isEmpty(price) &&
                Integer.parseInt(quantity) == 0 &&
                TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone)) {
            return;
        }

        final ContentValues values = new ContentValues();

        if (currentBookUri != null &&
                TextUtils.isEmpty(title) ||
                TextUtils.isEmpty(price) ||
                TextUtils.isEmpty(supplierName) ||
                TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, getString(R.string.Empty_field),
                    Toast.LENGTH_SHORT).show();
        } else {

            values.put(COLUMN_BOOK_TITLE, title);
            values.put(COLUMN_BOOK_PRICE, price);
            values.put(COLUMN_BOOK_SUPPLIER_NAME, supplierName);
            values.put(COLUMN_BOOK_SUPPLIER_PHONE, supplierPhone);

            int quantityInt = 0;
            if (!TextUtils.isEmpty(quantity)) {
                quantityInt = Integer.parseInt(quantity);
            }

            values.put(COLUMN_BOOK_QUANTITY, quantityInt);


            if (currentBookUri == null) {

                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_book_error),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_book_saved),
                            Toast.LENGTH_SHORT).show();
                }
            } else {

                int rowsAffected = getContentResolver().update(currentBookUri, values,
                        null, null);
                if (rowsAffected == 0) {

                    Toast.makeText(this, getString(R.string.editor_edit_book_error),
                            Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, getString(R.string.editor_book_edited),
                            Toast.LENGTH_SHORT).show();
                }
            }
            Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_book:
                saveBook();
                return true;

            case android.R.id.home:
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    Intent i = new Intent(EditorActivity.this, CatalogActivity.class);
                    startActivity(i);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.delete_a_book:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
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

            ImageButton minusButton = (ImageButton) findViewById(R.id.minusButton);
            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String quantityColumn = cursor.getString(cursor.getColumnIndexOrThrow
                            (BookEntry.COLUMN_BOOK_QUANTITY));
                    String columnIndex = cursor.getString(cursor.getColumnIndexOrThrow
                            (Contract.BookEntry._ID));
                    decreaseNumberOfBooks(Integer.valueOf
                            (columnIndex), Integer.valueOf(quantityColumn));
                }
            });
            ImageButton plusButton = (ImageButton) findViewById(R.id.plusButton);
            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String quantityColumn = cursor.getString(cursor.getColumnIndexOrThrow
                            (BookEntry.COLUMN_BOOK_QUANTITY));
                    String columnIndex = cursor.getString(cursor.getColumnIndexOrThrow
                            (Contract.BookEntry._ID));
                    increaseNumberOfBooks(Integer.valueOf
                            (columnIndex), Integer.valueOf(quantityColumn));
                }
            });

            nameEditText.setText(currentName);
            priceEditText.setText(currentPrice);
            quantityEditText.setText(Integer.toString(currentQuantity));
            supplierNameEditText.setText(currentSupplierName);
            supplierPhoneEditText.setText(currentSupplierPhone);
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

    private void deleteBook() {
        int rowsDeleted = 0;

        if (currentBookUri != null) {

            rowsDeleted = getContentResolver().delete(
                    currentBookUri, null,
                    null
            );
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_book_deleted),
                        Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_a_book);
            menuItem.setVisible(false);
        }
        return true;
    }

    public void decreaseNumberOfBooks(Integer id, Integer quantity) {

        quantity--;

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);

        Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

        if (quantity < 0) {
            Toast.makeText(EditorActivity.this, R.string.lower_than_0_warning,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsAffected = getContentResolver().update(updateUri, values, null, null);
        if (rowsAffected == 0) {

            Toast.makeText(this, getString(R.string.editor_edit_book_error),
                    Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, getString(R.string.editor_book_edited),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseNumberOfBooks(Integer id, Integer quantity) {

        quantity++;

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);

        Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

        int rowsAffected = getContentResolver().update(updateUri, values, null, null);

        if (rowsAffected == 0) {

            Toast.makeText(this, getString(R.string.editor_edit_book_error),
                    Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, getString(R.string.editor_book_edited),
                    Toast.LENGTH_SHORT).show();
        }
    }
}


package com.example.android.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bookstoreapp.R;
import com.example.android.bookstoreapp.data.Contract.BookEntry;


public class BooksProvider extends ContentProvider {

    private DataBaseHelper dataBaseHelper;

    public static final String LOG_TAG = BooksProvider.class.getSimpleName();

    private static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_BOOKS + "/#", BOOK_ID);
    }


    @Override
    public boolean onCreate() {

        dataBaseHelper = new DataBaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = dataBaseHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {

            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) throws IllegalArgumentException {

        String name = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
        String price = values.getAsString(BookEntry.COLUMN_BOOK_PRICE);
        Integer quantity = values.getAsInteger(String.valueOf(BookEntry.COLUMN_BOOK_QUANTITY));
        String supplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
        String supplierPhoneNumber = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);


        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.No_title));
        }
        if (price == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.No_price));
        }
        if (supplierName == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.No_supplier));
        }
        if (supplierPhoneNumber == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.No_phone));
        }

        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();

        long id = database.insert(BookEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:

                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:

                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(BookEntry.COLUMN_BOOK_TITLE)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
            if (name == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.No_title));
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            String price = values.getAsString(BookEntry.COLUMN_BOOK_PRICE);
            if (price == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.No_price));
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot by lower than 0");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.No_supplier));
            }
        }
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE)) {
            String supplierPhoneNumber = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);
            if (supplierPhoneNumber == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.No_phone));
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();

        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}

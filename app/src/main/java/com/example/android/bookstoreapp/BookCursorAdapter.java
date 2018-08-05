package com.example.android.bookstoreapp;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BooksProvider;
import com.example.android.bookstoreapp.data.Contract;

import java.util.Currency;
import java.util.Locale;

public class BookCursorAdapter extends CursorAdapter {

    private String quantityColumn;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.table_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView titleTextView = (TextView) view.findViewById(R.id.book_title);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        ImageButton quantityButton = (ImageButton) view.findViewById(R.id.quantityButton);
        quantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                quantityColumn = cursor.getString(cursor.getColumnIndexOrThrow
                        (Contract.BookEntry.COLUMN_BOOK_QUANTITY));
                String columnIndex = cursor.getString(cursor.getColumnIndexOrThrow
                        (Contract.BookEntry._ID));
                CatalogActivity catalogActivity = (CatalogActivity) context;
                catalogActivity.decreaseNumberOfBooks(Integer.valueOf
                        (columnIndex), Integer.valueOf(quantityColumn));
            }
        });

        String titleColumn = cursor.getString(cursor.getColumnIndexOrThrow(Contract.BookEntry.COLUMN_BOOK_TITLE));
        String priceColumn = cursor.getString(cursor.getColumnIndexOrThrow
                (Contract.BookEntry.COLUMN_BOOK_PRICE));
        quantityColumn = cursor.getString(cursor.getColumnIndexOrThrow(Contract.BookEntry.COLUMN_BOOK_QUANTITY));

        TextView currencyView = (TextView) view.findViewById(R.id.currency);
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();

        titleTextView.setText(titleColumn);
        priceTextView.setText(priceColumn);
        currencyView.setText(symbol);
        quantityTextView.setText(quantityColumn);

    }
}


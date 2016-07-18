package com.udacityprojects.geonarz.stockhawk.Widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacityprojects.geonarz.stockhawk.R;
import com.udacityprojects.geonarz.stockhawk.data.QuoteColumns;
import com.udacityprojects.geonarz.stockhawk.data.QuoteProvider;
import com.udacityprojects.geonarz.stockhawk.rest.Utils;

public class QuoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor cursor;
    private Context context;

    public QuoteViewsFactory(Context context)
    {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (cursor != null) {
            cursor.close();
        }

        cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (cursor == null) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int i) {

        if (!cursor.moveToPosition(i)) {
            return null;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
        views.setTextViewText(R.id.stock_symbol, cursor.getString(cursor.getColumnIndex("symbol")));

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        if (Utils.showPercent) {
            views.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            views.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex("change")));
        }

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra("symbol", cursor.getString(cursor.getColumnIndex("symbol")));
        views.setOnClickFillInIntent(R.id.list_item, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        // Get the row ID for the view at the specified position
        if (cursor != null && cursor.moveToPosition(i)) {
            final int QUOTES_ID_COL = 0;
            return cursor.getLong(QUOTES_ID_COL);
        }
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
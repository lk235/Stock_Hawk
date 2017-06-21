package com.udacity.stockhawk.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

import java.util.ArrayList;

/**
 * Created by lk235 on 2017/6/15.
 */

public class ListViewProvider implements RemoteViewsService.RemoteViewsFactory{


        private ArrayList<ListViewItem> itemList = new ArrayList<>();
        private Context context = null;
        private int appWidgetId;

        public ListViewProvider(Context context, Intent intent) {
            this.context = context;
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            populateListItem();

        }

        private void populateListItem() {
            Log.i("WIDGET", "" + (StockWidgetIntentService.itemArrayList != null));
            if(StockWidgetIntentService.itemArrayList != null) {

                itemList = (ArrayList<ListViewItem>) StockWidgetIntentService.itemArrayList
                        .clone();

            }else {
                itemList = new ArrayList<>();
            }

//            for (int i = 0; i < 10; i++) {
//                ListViewItem listViewItem = new ListViewItem();
//                listViewItem.symbol = "symbol" + i;
//                listViewItem.price = i
//                        + "100";
//                listViewItem.priceChange = "100%";
//                itemList.add(listViewItem);


        }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
       Log.i("WIDGET", "CHANGED");
        populateListItem();
    }

    @Override
    public void onDestroy() {

    }

    @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
        @Override
        public RemoteViews getViewAt(int position) {
            final RemoteViews remoteView = new RemoteViews(
                    context.getPackageName(), R.layout.stock_widget_item);
            ListViewItem listViewItem = (ListViewItem)itemList.get(position);
            remoteView.setTextViewText(R.id.widget_symol, listViewItem.symbol);
            remoteView.setTextViewText(R.id.widget_price, listViewItem.price);
            remoteView.setTextViewText(R.id.widget_price_change, listViewItem.priceChange);

            Bundle extras = new Bundle();
            extras.putInt(StockAppWidgetProvider.EXTRA_ITEM, position);
            final Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);

            remoteView.setOnClickFillInIntent(R.id.widget_item, fillInIntent);


            return remoteView;
        }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

}


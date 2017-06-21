package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.util.ArrayList;

/**
 * Created by lk235 on 2017/6/15.
 */

public class StockWidgetIntentService extends IntentService {


    public static ArrayList<ListViewItem> itemArrayList;
    public StockWidgetIntentService(){
        super("StockWidgetIntentService");
    }





    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                StockAppWidgetProvider.class));


        Cursor data = getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
        Log.i("WIDGET", "IntentService" + data.getCount());
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        itemArrayList = new ArrayList<>();

        for (int i = 0; i < data.getCount(); i++,data.moveToNext()){
            ListViewItem listViewItem = new ListViewItem();
            listViewItem.symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            listViewItem.price = data.getString(Contract.Quote.POSITION_PRICE);
            listViewItem.priceChange = data.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            itemArrayList.add(listViewItem);
            Log.i("WIDGET",data.getString(Contract.Quote.POSITION_SYMBOL));
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);


//        adapter.setCursor(data);
//
//
//        data.close();
//
//        // Perform this loop procedure for each Today widget
//        for (int appWidgetId : appWidgetIds) {
//            int layoutId = R.layout.widget_today_small;
//            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
//
//            // Add the data to the RemoteViews
//            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
//            // Content Descriptions for RemoteViews were only added in ICS MR1
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                setRemoteContentDescription(views, description);
//            }
//            views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
//
//            // Create an Intent to launch MainActivity
//            Intent launchIntent = new Intent(this, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
//            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
//
//            // Tell the AppWidgetManager to perform an update on the current app widget
//            appWidgetManager.updateAppWidget(appWidgetId, views);

        }


}

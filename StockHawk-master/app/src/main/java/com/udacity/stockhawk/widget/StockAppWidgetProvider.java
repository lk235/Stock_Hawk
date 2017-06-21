package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Administrator on 2017/6/13.
 */

public class StockAppWidgetProvider extends AppWidgetProvider {
    public static final String EXTRA_ITEM = "extra_item";
    public static final String TOAST_ACTION = "toast_action";



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity

            Intent serviceIntent = new Intent(context, StockWidgetIntentService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);


//            context.startService(serviceIntent); Intent intent = new Intent(context, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_appwidget);
            RemoteViews views = updateWidgetListView(context, appWidgetIds[i]);

            Intent startActivityIntent = new Intent(context, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.widget_item, pendingIntent);

            Intent toastIntent = new Intent(context, StockAppWidgetProvider.class);
            toastIntent.setAction(StockAppWidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_item, toastPendingIntent);


           ;

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.i("WIDGET", "onUpdate");
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
//
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("WIDGET" ,"" + intent.getAction());
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            context.startService(new Intent(context, StockWidgetIntentService.class));
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);
            RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);

            if (intent.getAction().equals(TOAST_ACTION)) {

                int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
                Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);

            Log.i("WIDGET", "START SERVICE");
        }
        Log.i("WIDGET", "onRecieve");
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.stock_appwidget);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, StockRemoteViewService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.widget_list_view,
                svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.widget_list_view, R.id.empty_text_view);

        return remoteViews;
    }
}

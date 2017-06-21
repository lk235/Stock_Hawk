package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;

    public static final String DATA_UPDATE_STATUS = "data_update_status";
    public static final String DATA_UPDATE_SUCESSFUL = "data_update_sucessful";
    public static final String DATA_UPDATE_FAILED = "data_update_failed";
    //private static final int PERIOD = 300000;
    private static final int PERIOD = 30000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;
    private static final String LOG_TAG = QuoteSyncJob.class.getSimpleName();


    @Retention(RetentionPolicy.SOURCE)

    @IntDef({STOCK_STATUS_OK, STOCK_STATUS_UNKNOWN, STOCK_STATUS_INVALID})

    public @interface StockStatus {}

    public static final int STOCK_STATUS_OK = 0;
    public static final int STOCK_STATUS_UNKNOWN = 1;
    public static final int STOCK_STATUS_INVALID = 2;
    public static final String ACTION_DATA_UPDATED =
            "com.udacity.stockhawk.app.ACTION_DATA_UPDATED";

    private QuoteSyncJob() {
    }

    static void getQuotes(final Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);


        try {


            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }
            Log.i("QUOTES","QUOTES");

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Log.i("QUOTES","" + quotes.isEmpty() + quotes.size() + quotes.values());


            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Log.i(LOG_TAG, symbol);


                Stock stock = quotes.get(symbol);





                if(!stock.isValid()){
                   setStockStatus(context, STOCK_STATUS_INVALID);
                    PrefUtils.removeStock(context, symbol);
                    return;
                }
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.DAILY);


//                List<Entry> entries = new ArrayList<Entry>();
//                for(HistoricalQuote historicalData : history){
//                   entries.add(new Entry(historicalData.getDate().getTimeInMillis(),historicalData.getClose().floatValue()));
//
//                }



                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    //Log.i("H1","" + historyBuilder.append(it.getDate().getTimeInMillis()));
                    historyBuilder.append(", ");
                    //Log.i("H2" ,"" + historyBuilder.append(", "));
                    historyBuilder.append(it.getClose());
                   // Log.i("H3" , "" + historyBuilder.append(it.getClose()));
                    historyBuilder.append("\n");
                   // Log.i("H4" , "" + historyBuilder.append("\n"));

                    Log.i("CHART", "" + it.getSymbol());

                                        Log.i("CHART", "" + it.toString());
                    Log.i("CHART", "" + it.getAdjClose());
                    Log.i("CHART", "" + it.getDate().getTime());
                    Log.i("CHART", "" + it.getHigh());
                    Log.i("CHART", "" + it.getLow());
                    Log.i("CHART", "" + it.getOpen());
                    Log.i("CHART", "" + it.getVolume());
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);

            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
            return;



        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {
        Log.i("SYNC1","SYNC1");
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
            Log.i("SYNC2","SYNC2");
        } else {
            Log.i("SYNC3","SYNC3");
            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }

    static private void setStockStatus(Context c, @StockStatus int stockStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_stock_status_key), stockStatus);
        spe.commit();
    }


}

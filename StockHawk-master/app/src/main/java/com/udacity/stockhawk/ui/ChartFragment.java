package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.listener.OnDrawListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import yahoofinance.YahooFinance;


/**
 * Created by lk235 on 2017/6/6.
 */

public class ChartFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private final int CHART_LOADER = 0;

    public static final String[] HISTORY_COLUMNS = {

            Contract.Quote._ID,
            Contract.Quote.COLUMN_HISTORY


    };

    public static final int COL_SYMBOL_ID = 0;
    public static final int COL_HISTORY = 1;
    private LineChart mChart;
    private String mSymbol;
    private Highlight mHighlight;


    public ChartFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);

         mChart= (LineChart) rootView.findViewById(R.id.chart);
         mChart.setTouchEnabled(true);
         mChart.setOnChartGestureListener(new OnChartGestureListener() {
             @Override
             public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

             }

             @Override
             public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

             }

             @Override
             public void onChartLongPressed(MotionEvent me) {

             }

             @Override
             public void onChartDoubleTapped(MotionEvent me) {

             }

             @Override
             public void onChartSingleTapped(MotionEvent me) {
                 float tappedX = me.getX();
                 float tappedY = me.getY();
                 MPPointD point = mChart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(tappedX, tappedY);
                 double xValue = point.x;
                 double yValue = point.y;
                 mHighlight = mChart.getHighlightByTouchPoint((float)xValue, (float)yValue);
                 mHighlight.setDraw((float)xValue, (float)yValue);


             }

             @Override
             public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

             }

             @Override
             public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

             }

             @Override
             public void onChartTranslate(MotionEvent me, float dX, float dY) {

             }
         });










//        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...); // styling, ...
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CHART_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSymbol = getActivity().getIntent().getStringExtra(MainActivity.EXTRA_SYMBOL);
        Uri uri = Contract.Quote.makeUriForStock(mSymbol);
        Log.i("LOADER", "CREATED");
        return new CursorLoader(
                getActivity(),
                uri,
                HISTORY_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("LOADER", "FINISHED");
        data.moveToFirst();
        String history = data.getString(COL_HISTORY);
        Log.i("LOADER", history);

        String[] str1 = history.split("\n");
        Log.i("LOADER__", str1[0] + "  " + str1[1]);
        ArrayList<String[]> str = new ArrayList<>();
        int j = 0;
        while (j < str1.length){
            str.add(str1[j].split(","));
            j++;
        }


        List<Entry> entries = new ArrayList<Entry>();



        for (String[] str2 : str) {


            entries.add(new Entry(Float.parseFloat(str2[0]), Float.parseFloat(str2[1])));

            Log.i("LOADER_" , str2[0] + " " + str2[1]);

        }
        //Log.i("LOADER", "" + entries.size());

        LineDataSet dataSet = new LineDataSet(entries, mSymbol); // add entries to dataset
        dataSet.setColor(R.color.material_blue_500);
        dataSet.setValueTextColor(R.color.material_green_700); // styling, ...

        LineData lineData = new LineData(dataSet);

        mChart.setData(lineData);

        mChart.invalidate(); // refresh

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("yy-MM-dd ");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.MILLISECONDS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        YAxis rightAxis = mChart.getAxisRight();
        leftAxis.setTextColor(Color.WHITE);
        rightAxis.setTextColor(Color.WHITE);





    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    public class StockMarkerView extends MarkerView {
//
//        private TextView chartValue;
//        public StockMarkerView (Context context, int layoutResource) {
//            super(context, layoutResource);
//            // this markerview only displays a textview
//            chartValue = (TextView) findViewById(R.id.value_chart_text_view);
//        }
//
//        // callbacks everytime the MarkerView is redrawn, can be used to update the
//        // content (user-interface)
//        @Override
//        public void refreshContent(Entry e, Highlight highlight) {
//            chartValue.setText(e.getX() + "\n" + e.getY()); // set the entry-value as the display text
//        }



}

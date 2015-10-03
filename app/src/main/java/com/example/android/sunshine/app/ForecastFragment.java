package com.example.android.sunshine.app;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import static com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;



/**
 * Created by Mohamed Yasser on 8/10/2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int FORECAST_LOADER = 0;
    public final static String LOCATION_QUERY = "newlocation";
    private final String LOG_TAG = "TAG";
    private ForecastAdapter mForecastAdapter;
    private ListView mListView;
    private boolean mUseTodayLayout;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;



    public ForecastFragment() {
        Log.v(LOG_TAG,"inside ForecastFragment constructor");
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.v(LOG_TAG,"inside onCreate method ForecastFragment");

        setHasOptionsMenu(true);
//        postalCode = "94043";
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.view_location){
            getLocation();
        }
//        if(id==R.id.action_refresh){
//            updateWeather();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    private void getLocation() {
//        SharedPreferences prefs = PreferenceManager.
//                getDefaultSharedPreferences(getApplicationContext());
//        String location = prefs.getString(getString(R.string.pref_location_key)
//                , getString(R.string.pref_location_default));
        String location = Utility.getPreferredLocation(getActivity());
        double lat = 0.0;
        double longitude = 0.0;

        if(mForecastAdapter!=null){

            Cursor cursor = mForecastAdapter.getCursor();
            if(cursor!=null){
                cursor.moveToPosition(0);
                lat = cursor.getDouble(ForecastFragment.COL_COORD_LAT);
                longitude = cursor.getDouble(ForecastFragment.COL_COORD_LONG);
            }
        }

        Uri geoLocation = Uri.parse("geo:" + lat + "," + longitude);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW).setData(geoLocation);
        startActivity(intent);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call" + location + ": no activity to handle the intent ");
        }
    }

    private void updateWeather(){
        Log.v(LOG_TAG, "inside updateWeather ForecastFragment");
//        AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(getActivity(),SunshineService.AlarmReceiver.class);
//        intent.putExtra(ForecastFragment.LOCATION_QUERY, Utility.getPreferredLocation(getActivity()));
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, pi);
        SunshineSyncAdapter.syncImmediately(getActivity());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG,"inside onCreateView ForecastFragment");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(
                            WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                mPosition = i;

            }
        });
        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }else if(mPosition==ListView.INVALID_POSITION){
            mListView.setItemChecked(mListView.getFirstVisiblePosition(),true);
        }
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(LOG_TAG,"inside onActivityCreated ForecastFragment");

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
            * so for convenience we're breaking it out into its own method now.
            */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }


    void onTempUnitChanged(){
        getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
    }



     void onLocationChanged(){
         Log.v(LOG_TAG,"inside onLocationChanged ForecastFragment");
        updateWeather();
         getLoaderManager().restartLoader(FORECAST_LOADER,null,this);

     }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG,"inside onCreateLoader ForecastFragment");

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                                weatherForLocationUri,
                                FORECAST_COLUMNS,
                                null,
                                null,
                                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG,"inside onLoadFinished ForecastFragment");

        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG,"inside onLoaderReset ForecastFragment");

        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG,"inside onSaveInstanceState ForecastFragment");

        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        Log.v(LOG_TAG,"inside setUseTodayLayout ForecastFragment");

        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }


}
package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.example.android.sunshine.app.data.WeatherContract.*;

/**
 * Created by Mohamed Yasser on 8/10/2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private final static int Loader_ID = 0;
    public ForecastAdapter mForecastAdapter;
    FetchWeatherTask newTask;
    LoaderManager loaderManager;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        if(id==R.id.action_refresh){
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void updateWeather(){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String postalCode = prefs.getString(getString(R.string.pref_location_key),
//                getString(R.string.pref_location_key));
//        String tempType = prefs.getString(getString(R.string.temp_pref_key)
//                , getString(R.string.temp_pref_default_value));
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
//        newTask = new FetchWeatherTask(getActivity(),mForecastAdapter);
        newTask.execute(location);


    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            ArrayList<String> list = new ArrayList<String>();
//            list.add(0,"Today-Sunny-88/63");
//            list.add(1,"Tomorrow-foggy-70/46");
//            list.add(2,"Weds-Cloudy-72/63");
//            list.add(3,"Thurs-Rainy-64/51");
//            list.add(4,"Fri-/foggy-70/46");
//            list.add(5,"Sat--Sunny-76/68");
//        String[] data = {
//                "Mon 6/23?- Sunny - 31/17",
//                "Tue 6/24 - Foggy - 21/8",
//                "Wed 6/25 - Cloudy - 22/17",
//                "Thurs 6/26 - Rainy - 18/11",
//                "Fri 6/27 - Foggy - 21/10",
//                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
//                "Sun 6/29 - Sunny - 20/7"
//        };
//
//        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
//        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,
//                R.id.list_item_forecast_textview,new ArrayList<String>());
        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(mForecastAdapter);

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String text = mForecastAdapter.getItem(i);
//                Intent intent = new
//                        Intent();
//                intent.setClass(getActivity(),DetailActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT,text);
//                startActivity(intent);
//
//            }
//        });


        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loaderManager = getLoaderManager();
        loaderManager.initLoader(Loader_ID,null,this);

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

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

//        for (String s : resultStrs) {
//            Log.v(LOG_TAG, "Forecast entry: " + s);
//        }
        return resultStrs;

    }

    @Override
    public Loader<Cursor> CursorLoaderonCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), BASE_CONTENT_URI,null,null,
                WeatherEntry.COLUMN_DATE + "ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }



    @Override
    public void onLoaderReset(Loader loader) {
        mForecastAdapter.swapCursor(null);
    }
//    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//        String[] weatherData;
//        Double maxTemp;
//
//        @Override
//        protected void onPostExecute(String[] strings) {
//            super.onPostExecute(strings);
//            weatherData = strings;
//            mForecastAdapter.clear();
//            for(String forecastDay:weatherData){
//                mForecastAdapter.add(forecastDay);
//            }
////            mForecastAdapter.addAll(weatherData);
//
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//
//            if(params.length==0){
//                return null;
//            }
//
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            String postalCode=params[0];
//            String tempType = params[1];
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
////                // http://openweathermap.org/API#forecast
////                Uri.Builder uriBuilder = new Uri.Builder();
////                uriBuilder.scheme("http")
////                        .authority("api.openweathermap.org/data/2.5/forecast/daily?")
////                        .appendQueryParameter("q",postalCode)
////                        .appendQueryParameter("mode","json")
////                        .appendQueryParameter("units","metric")
////                        .appendQueryParameter("cnt","7");
////                String myURL = uriBuilder.build().toString();
////                URL url = new URL(myURL);
//
//                Uri builtUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?")
//                        .buildUpon()
//                        .appendQueryParameter("q", postalCode)
//                        .appendQueryParameter("mode", "json")
//                        .appendQueryParameter("units",tempType)
//                        .appendQueryParameter("cnt","7")
//                        .build();
//
////                Log.v(LOG_TAG,"builtUri: "+ builtUri.toString());
//
//                URL url = new URL(builtUri.toString());
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    forecastJsonStr = null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    forecastJsonStr = null;
//                }
//                forecastJsonStr = buffer.toString();
////                Log.v(LOG_TAG, "ForecastJSON string: " + forecastJsonStr);
//                try {
//                       weatherData = getWeatherDataFromJson(forecastJsonStr,7);
//
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG,e.getMessage(),e);
//                }
//
////                if(weatherData!=null){
////                    for(String dayWeather:weatherData) {
////                        Log.v(LOG_TAG, "Weather Forecast " +dayWeather);
////                    }
////                }
//            } catch (IOException e) {
//                Log.e("ForecastFragment", "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attempting
//                // to parse it.
//                forecastJsonStr = null;
//            } finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e("ForecastFragment", "Error closing stream", e);
//                    }
//                }
//            }
//
//
//            return weatherData;
//        }
//    }




}
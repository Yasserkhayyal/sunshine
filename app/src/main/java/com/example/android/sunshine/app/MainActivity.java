package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{
    private final String LOG_TAG = "TAG";
    private String mLocation;
    private final String FORECASTFRAGMENT_TAG = "Forecast_Fragment";
    private String mTempPref;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private static final String[] LOCATION_COORDINATES = new String[] {
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private static final int LAT_INDEX = 0;
    private static final int LONG_INDEX = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG,"inside onCreate method");
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(getApplicationContext());
        mTempPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getString(R.string.temp_pref_key)
                        ,getString(R.string.temp_pref_default_value));
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/fragment_detail-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container
                                , new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        String tempUnit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getString(R.string.temp_pref_key)
                        , getString(R.string.temp_pref_default_value));
        // update the location in our second pane using the fragment manager
        ForecastFragment ff = null;
        if(tempUnit!=null && !mTempPref.equals(tempUnit) ){
            ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

            if ( null != ff ) {
                ff.onTempUnitChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onTempUnitChanged();
            }
        }
        mTempPref = tempUnit;

        if (location != null && !location.equals(mLocation))
             ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

        if ( null != ff ) {
            ff.onLocationChanged();
        }
        DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if ( null != df ) {
            df.onLocationChanged(location);
        }
        mLocation = location;
    }






    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
    }


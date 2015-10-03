package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final String LOG_TAG = "TAG";

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;


    public void setUseTodayLayout(boolean useTodayLayout) {
        Log.v(LOG_TAG,"inside setUseTodayLayout ForecastAdapter");
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        Log.v(LOG_TAG,"inside getItemViewType ForecastAdapter");

        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext,high, isMetric)
                + "/" + Utility.formatTemperature(mContext,low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        double max_temp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        double min_temp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        long date  = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String short_desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        String highAndLow = formatHighLows(
                max_temp,
                min_temp);

        return Utility.formatDate(date)+
                " - " + short_desc +
                " - " + highAndLow;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG,"inside newView ForecastAdapter");

        // Choose the fragment_detail type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        // TODO: Determine layoutId from viewType
        switch(viewType){
            case VIEW_TYPE_TODAY:
               layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.v(LOG_TAG,"inside bindView ForecastAdapter");

        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();


        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        // Use placeholder image for now
//        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        int viewType = getItemViewType(cursor.getPosition());
        switch(viewType){
            case VIEW_TYPE_TODAY:
                viewHolder.iconView
                        .setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;

            case VIEW_TYPE_FUTURE_DAY:
                viewHolder.iconView
                        .setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
                break;
        }
        // TODO Read date from cursor

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
        // TODO Read weather forecast from cursor

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);
        viewHolder.iconView.setContentDescription(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        // Read high temperature from cursor
        viewHolder.highTempView.setText(Utility.formatTemperature(mContext,high, isMetric));

        // TODO Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(mContext,low, isMetric));
    }
}
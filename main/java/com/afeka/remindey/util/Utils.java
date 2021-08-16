package com.afeka.remindey.util;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.preference.PreferenceManager;

import com.afeka.remindey.R;
import com.afeka.remindey.logic.Priority;
import com.afeka.remindey.logic.Reminder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Utils used for different functions that are used frequently.
 */

public class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    public static String formatDate(Date date) {
        if (date == null)
            return null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        simpleDateFormat.applyPattern("EEE, MMM d");

        return simpleDateFormat.format(date);
    }

    public static String formatDateSlash(Date date) {
        if (date == null)
            return null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        simpleDateFormat.applyPattern("dd/MM/yyyy");

        return simpleDateFormat.format(date);
    }

    public static Date stringToDate(String strDate) {
        if (strDate == null || strDate == "")
            return null;
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d");
        try {
            Date date = format.parse(strDate);
            return date;
        } catch (ParseException e) {
            Log.d("Utils", "stringToDate " + e.toString());
        }
        return null;
    }

    public static Date stringToSlashDate(String strDate) {
        if (strDate == null || strDate == "")
            return null;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = format.parse(strDate);
            return date;
        } catch (ParseException e) {
            Log.d("Utils", "stringToDate " + e.toString());
        }
        return null;
    }

    public static String formatTime(Date date) {
        if (date == null)
            return null;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        simpleDateFormat.applyPattern("HH:mm");
        return simpleDateFormat.format(date);
    }

    public static String formatTimeInt(int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        return formatTime(c.getTime());
    }

    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int priorityColor(Reminder reminder) {
        int color;
        if (reminder.getPriority() == Priority.HIGH) {
            color = Color.argb(200, 201, 21, 23);
        } else if (reminder.getPriority() == Priority.MEDIUM) {
            color = Color.argb(200, 155, 179, 0);
        } else {
            color = Color.argb(200, 51, 181, 129);
        }

        if (reminder.isOverdue()) {
            color = Color.argb(200, 50, 50, 200);
        }
        return color;
    }


    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }


    static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return "Unknown location";
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        Log.d("LOCATION_UPDATES", "getLocationResultText " + sb.toString());
        return sb.toString();
    }

    public static void setLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    public static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    public static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = context.getResources().getQuantityString(1, locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }


}

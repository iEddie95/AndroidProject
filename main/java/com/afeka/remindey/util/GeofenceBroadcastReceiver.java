package com.afeka.remindey.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.afeka.remindey.logic.Reminder;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * GeofenceBroadcastReceiver used to manage events when geofence is triggered (enter/leave location).
 * Mostly for creating the pre-made reminders.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GEOFENCE_RECEIVER";
    private SharedPreferences sharedPref;

    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Geofence Triggered");
//        Toast.makeText(context, "Geofence Triggered", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("SHARED_PREF", "lock home in " + sharedPref.getBoolean("lock_home_in", false));
        Log.d("SHARED_PREF", "lock home out " + sharedPref.getBoolean("lock_home_out", false));

        Reminder reminder;
        BuiltInRemindersHelper remindersHelper = new BuiltInRemindersHelper(context);

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
//                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");

                if (sharedPref.getBoolean("lock_home_in", false)) {
                    remindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.LOCK_HOME_IN);
                }
                if (sharedPref.getBoolean("pet_feed", false)) {
                    remindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.FEED_PET_HOME);
                }
                if (sharedPref.getBoolean("laundry_check", false)) {
                    remindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.LAUNDRY_CHECK);
                }
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
//                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
                if (sharedPref.getBoolean("lock_home_out", false)) {
                    remindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.LOCK_HOME_OUT);
                }
                break;
        }
    }
}

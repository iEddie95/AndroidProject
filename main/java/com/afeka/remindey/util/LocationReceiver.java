package com.afeka.remindey.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {
    public static final String TAG = "LocationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
        if (location != null) {
            Log.d(TAG, Utils.getLocationText(location));
//            Toast.makeText(context, Utils.getLocationText(location),
//                    Toast.LENGTH_SHORT).show();
        }
    }
}
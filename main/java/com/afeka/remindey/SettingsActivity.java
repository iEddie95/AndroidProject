package com.afeka.remindey;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.afeka.remindey.util.BuiltInRemindersHelper;
import com.afeka.remindey.util.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SettingsActivity used to manage the pre-made reminders, location based and from templates.
 * IMPORTANT: Enable location service
 * In the settings menu (from the three dots menu), you can enable location based reminders.
 * Important to click on "Use current location as home" to set you home address, that the reminders could trigger.
 * For example: you can enable a reminder to check if you locked the door when you leave.
 * Also to set a reminder when you get home to make sure you locked the door, or other built in reminders.
 * Reminder templates: you can create two kinds of alarms from a template- Feed pet and pills reminders.
 * Feed pet you can just click and add the reminders (you can always edit the times and title from the main screen, just like editing any reminder).
 * Pills reminder- select how many time a day you take a pill, choose time and also choose if you want food reminder before after taking a pill.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements LocationListener {
        private static final String TAG = "SETTINGS_LOCATION";
        private static final String BUILT_IN_REMINDERES = "Built-in reminders";

        private Preference viewLocationPreference;
        private Preference setLocationPreference;
        private Preference feedPetPreference;
        private EditTextPreference feedPetEditPreference;
        private Preference pillsPreference;


        private FusedLocationProviderClient fusedLocationClient;
        private LocationRequest locationRequest;
        private ArrayList<String> permissions = new ArrayList<>();


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            viewLocationPreference = (Preference) getPreferenceManager().findPreference("my_location");
            setLocationPreference = (Preference) getPreferenceManager().findPreference("set_current_location");
            feedPetEditPreference = (EditTextPreference) getPreferenceManager().findPreference("FEED_PET_ROUTINE");
            pillsPreference = (Preference) getPreferenceManager().findPreference("set_pills");


            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            assert viewLocationPreference != null;
            assert feedPetEditPreference != null;

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            String address = sharedPref.getString("my_location", "Not Set");
            viewLocationPreference.setSummary(address);

            setLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getContext(), "YOU NEED TO ENABLE LOCATION PERMISSIONS", Toast.LENGTH_SHORT).show();

                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return false;
                    }


                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                    locationRequest = new LocationRequest();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(1000);
                    locationRequest.setFastestInterval(1000);

                    LocationServices.getFusedLocationProviderClient(getContext())
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    if (locationResult != null) {
                                        Location location = locationResult.getLastLocation();
                                        fusedLocationClient.removeLocationUpdates(this);
                                        Log.d("LOCATION", "onLocationResult my location: lat:" + location.getLatitude() + "alt: " + location.getAltitude()
                                                + " long: " + location.getLongitude());
                                        List<Address> addressList;
                                        Log.d("LOCATION_FRAGMENT", "onSuccess my location: lat:" + location.getLatitude() + "alt: " + location.getAltitude()
                                                + " long: " + location.getLongitude());
                                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                        try {
                                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//                                            SharedPreferences sharedPreferences = viewLocationPreference.getSharedPreferences();
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("my_location", addressList.get(0).getAddressLine(0));
                                            editor.putFloat("location_long", (float) location.getLongitude());
                                            editor.putFloat("location_lat", (float) location.getLatitude());
                                            editor.putBoolean("GEOFENCE_CHANGE", true);
                                            editor.apply();
//                                            viewLocationPreference.setSummary(address);
                                            viewLocationPreference.setSummary(addressList.get(0).getAddressLine(0));
                                            Log.d("LOCATION_FRAGMENT", "onSuccess my address: lat:" + addressList.get(0).getCountryName() + " " + addressList.get(0).getLocality() + " " + addressList.get(0).getAddressLine(0));

                                        } catch (IOException e) {
                                            Log.d("LOCATION_ERROR", e.toString());
                                        }

                                    }
                                }

                                @Override
                                public void onLocationAvailability(@NonNull @NotNull LocationAvailability locationAvailability) {
                                    super.onLocationAvailability(locationAvailability);
                                }
                            }, null);

                    return true;
                }
            });


            feedPetEditPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BuiltInRemindersHelper remindersHelper = new BuiltInRemindersHelper(getActivity());
                    remindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.FEED_PET_ROUTINE);
                    return true;
                }
            });

            pillsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getParentFragmentManager().beginTransaction().replace(R.id.settings, new RoutineRemindersFragment()).commit();
                    return true;
                }
            });

        }


        @Override
        public void onLocationChanged(@NonNull @NotNull Location location) {

        }

    }

    /**
     * Fragment for repeated and more complex reminder such as taking pills
     */
    public static class RoutineRemindersFragment extends PreferenceFragmentCompat implements OnTimeSetListener, OnDateSetListener {
        private final int START_HOUR = 8;
        private final int MAX_HOURS = 24;
        DropDownPreference timesDayPills;
        EditTextPreference pillName;
        PreferenceCategory setTime;
        Preference selectedPreference;
        Preference setEndDate;
        ListPreference eatBeforeAfter;
        DropDownPreference eatTime;
        SwitchPreference switchEatReminder;
        BuiltInRemindersHelper builtInRemindersHelper;
        boolean isRepeatEnd;
        int hour;
        int min;
        int eatTimeValue;
        String endDate;
        int eatBeforeAfterValue;
        int timesDay;

        @Override
        public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            builtInRemindersHelper = new BuiltInRemindersHelper(getContext());
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pills_reminders, rootKey);
        }

        @Override
        public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.settings_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
            if (item.getItemId() == R.id.done_settings) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int count = setTime.getPreferenceCount();
                editor.putInt("timeSetCount", count);
                if (isRepeatEnd) {
                    editor.putBoolean("isRepeatEndPills", true);
                    editor.putString("timeSetEndDate", endDate);
                }
                Log.d("Preference", "enable food reminders" + sharedPreferences.getBoolean("enable_food", false));
                if (sharedPreferences.getBoolean("enable_food", false)) {
                    editor.putInt("eatBeforeAfter", eatBeforeAfterValue);
                    editor.putInt("whenEatReminders", eatTimeValue);
                }
                editor.commit();
                builtInRemindersHelper.createNewReminder(BuiltInRemindersHelper.BuildInReminders.PILLS_REMINDER);
                Log.d("Preference", "onOptionsItemSelected " + count);
                getActivity().finish();
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            timesDayPills = (DropDownPreference) getPreferenceManager().findPreference("time_a_day_pills");
            pillName = (EditTextPreference) getPreferenceManager().findPreference("pill_name");
            timesDay = Integer.parseInt(timesDayPills.getEntry().toString());
            setTime = (PreferenceCategory) getPreferenceManager().findPreference("set_time");
            setEndDate = (Preference) getPreferenceManager().findPreference("end_date");
            switchEatReminder = (SwitchPreference) getPreferenceManager().findPreference("enable_food");
            eatBeforeAfter = (ListPreference) getPreferenceManager().findPreference("before_after_food");
            eatTime = (DropDownPreference) getPreferenceManager().findPreference("time_eat");
            isRepeatEnd = false;
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            eatBeforeAfterValue = Integer.parseInt(eatBeforeAfter.getValue());
            eatTimeValue = Integer.parseInt(eatTime.getValue());

            // Select the number of times taking pill in a day
            timesDayPills.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    DropDownPreference downPreference = (DropDownPreference) preference;
                    int index = downPreference.findIndexOfValue(newValue.toString());
                    String value = (String) downPreference.getEntries()[index];
                    Log.d("Preference", "onPreferenceChange " + index + " " + value);
                    setFields(Integer.parseInt(value));
                    return true;
                }
            });

            // Opens calendar date picker dialog
            setEndDate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogFragment newFragment = new DatePickerFragment();
                    ((DatePickerFragment) newFragment).registerListener((OnDateSetListener) RoutineRemindersFragment.this);
                    newFragment.show(getParentFragmentManager(), "datePicker");
                    return true;
                }
            });
            setFields(timesDay);

            // Select if eating before/after taking a pill
            eatBeforeAfter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    eatBeforeAfterValue = Integer.parseInt((String) newValue);
                    Log.d("Preference", "onPreferenceChange " + newValue + " " + eatBeforeAfterValue);
                    return true;
                }
            });

            eatTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    eatTimeValue = Integer.parseInt((String) newValue);
                    Log.d("Preference", "onPreferenceChange " + eatTimeValue);
                    return true;
                }
            });

            super.onViewCreated(view, savedInstanceState);
        }

        /**
         * Sets the number of fields in the UI, for selecting the alarm time of taking pill reminder
         */
        private void setFields(int value) {
            setTime.removeAll();
            for (int i = 0; i < value; i++) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                hour = START_HOUR + (((MAX_HOURS - START_HOUR) / value) * i); // calculate the interval between reminders for the alarms
                min = 0;
//                min = sharedPreferences.getInt("alarm_" + (i + 1) + "Min", 0);
                Preference pref = new Preference(getContext());
                pref.setTitle("Alarm " + (i + 1));
                pref.setKey("alarm_" + (i + 1));
                pref.setSummary(Utils.formatTimeInt(hour, min));
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        selectedPreference = preference;
                        DialogFragment newFragment = new TimePickerFragment();
                        ((TimePickerFragment) newFragment).registerListener((OnTimeSetListener) RoutineRemindersFragment.this);
                        newFragment.show(getParentFragmentManager(), "timePicker");
                        return true;
                    }
                });
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("alarm_" + (i + 1) + "Hour", hour);
                editor.putInt("alarm_" + (i + 1) + "Min", min);
                editor.commit();
                setTime.addPreference(pref);
            }
        }

        @Override
        public void onTimeSet(int hour, int min) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Log.d("Preference", "onTimeSet Fragment " + hour + ":" + min);
            if (selectedPreference != null) {
                selectedPreference.setSummary(Utils.formatTimeInt(hour, min));
                editor.putInt(selectedPreference.getKey() + "Hour", hour);
                editor.putInt(selectedPreference.getKey() + "Min", min);
                editor.commit();
                Log.d("Preference", "selectedPreference  " + selectedPreference.getKey());
            }

        }

        @Override
        public void onDateSet(Date date) {
            Log.d("onDateSet", "before parse + " + date.toString());
            endDate = Utils.formatDateSlash(date);
            Log.d("onDateSet", "after parse + " + endDate);

            isRepeatEnd = true;
            setEndDate.setSummary(endDate);
        }

        @Override
        public void onDateSet(int year, int month, int day) {

        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        OnTimeSetListener listener;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void registerListener(OnTimeSetListener listener) {
            Log.d("DatePicker", "registering...");
            this.listener = (OnTimeSetListener) listener;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            listener.onTimeSet(hourOfDay, minute);

        }

    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private OnDateSetListener listener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void registerListener(OnDateSetListener listener) {
            Log.d("DatePicker", "registering...");
            this.listener = listener;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            listener.onDateSet(c.getTime());
            listener.onDateSet(year, month, day);
        }
    }

}
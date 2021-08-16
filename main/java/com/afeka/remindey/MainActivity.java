package com.afeka.remindey;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afeka.remindey.adapter.RecyclerViewAdapter;
import com.afeka.remindey.databinding.ActivityMainBinding;
import com.afeka.remindey.logic.Category;
import com.afeka.remindey.logic.Priority;
import com.afeka.remindey.logic.Reminder;
import com.afeka.remindey.logic.ReminderComparator;
import com.afeka.remindey.logic.RepeatType;
import com.afeka.remindey.model.CategoryViewModel;
import com.afeka.remindey.model.ReminderViewModel;
import com.afeka.remindey.model.SharedViewModel;
import com.afeka.remindey.util.GeofenceHelper;
import com.afeka.remindey.util.LocationReceiver;
import com.afeka.remindey.util.LocationService;
import com.afeka.remindey.util.ReminderReceiver;
import com.afeka.remindey.util.RemindeyApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * MainActivity it's the main screen of the application.
 * IMPORTANT: Enable location service
 * Most of the app functionality manged in the Main Activity (location, notifications, geofence, adding reminders).
 */

public class MainActivity extends AppCompatActivity implements OnTodoClickListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String REMINDER_TABLE = "REMINDERS";
    public final static String CATEGORY_TABLE = "CATEGORIES";
    public static final long UPDATE_INTERVAL = 10000;
    public static final long FASTEST_INTERVAL = 5000;
    private static final int ALL_PERMISSION_RESULT = 1111;
    private static final int SNOOZE_HIGH_DELAY = 10; // high priority reminders second reminder if enabled in settings
    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL; // Every 5 minutes.
    public CategoryViewModel categoryViewModel;
    NavigationView navigationView;
    List<ActivityTransition> transitions = new ArrayList<>();
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private ReminderViewModel reminderViewModel;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private List<Reminder> reminderList;
    private List<Category> categoryList;
    private BottomSheetFragment bottomSheetFragment;
    private SharedViewModel sharedViewModel;
    private BroadcastReceiver br;
    private AddListBottomFragment addListBottomFragment;
    private DrawerLayout drawerLayout;
    private Group categoryGroup;
    private TextView userHeader;
    private TextView emailHeader;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(REMINDER_TABLE);
    private CollectionReference categoryCollectionReference = db.collection(MainActivity.CATEGORY_TABLE);
    private RemindeyApi remindeyApi = RemindeyApi.getInstance();
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsRejected = new ArrayList<>();
//    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private float GEOFENCE_RADIUS = 20;
    private ArrayList<Geofence> geofenceList;
    private PendingIntent geofencePendingIntent;
    private LocationService mService = null;
    private LocationReceiver locationReceiver;
    private boolean mBound = false;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    // Monitors the state of the connection to the service.
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("Service_Connection", "bound to service");

            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSION_RESULT);
            }
        }

        locationReceiver = new LocationReceiver();

        geofencingClient = LocationServices.getGeofencingClient(this);

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);


        reminderList = new ArrayList<>();
        categoryList = new ArrayList<>();

        bottomSheetFragment = new BottomSheetFragment();
        ConstraintLayout constraintLayout = findViewById(R.id.bottomSheet);
        BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetFragment.registerListener(this);

        addListBottomFragment = new AddListBottomFragment();
        ConstraintLayout addListConstraintLayout = (ConstraintLayout) findViewById(R.id.addListLayout);
        BottomSheetBehavior<ConstraintLayout> addListBottomSheetBehavior = BottomSheetBehavior.from(addListConstraintLayout);
        addListBottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_HIDDEN);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        //MENU
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        userHeader = headerView.findViewById(R.id.username_header);
        emailHeader = headerView.findViewById(R.id.email_header);

        if (RemindeyApi.getInstance() != null) {
            userHeader.setText(remindeyApi.getUsername());
            emailHeader.setText(remindeyApi.getUserEmail());
        }

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        categoryGroup = findViewById(R.id.category_group);

        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

        reminderViewModel.getAllReminders().observe(this, new Observer<List<Reminder>>() {
            @Override
            public void onChanged(List<Reminder> reminders) {
                reminders.sort(new ReminderComparator());
                recyclerViewAdapter = new RecyclerViewAdapter(reminders, MainActivity.this);
                recyclerView.setAdapter(recyclerViewAdapter);
            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        geofenceHelper = new GeofenceHelper(this);
        geofenceList = new ArrayList<>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


//        SharedPreferences sharedPref = getSharedPreferences("my_location", MODE_PRIVATE);
        double longitude = sharedPref.getFloat("location_long", 0);
        double latitude = sharedPref.getFloat("location_lat", 0);
        boolean isChanged = sharedPref.getBoolean("GEOFENCE_CHANGE", false);
        Log.d("GEOFENCE", "add " + latitude + " " + longitude);


        boolean isGeofence = sharedPref.getBoolean("geofence", false);

        if (isGeofence) {
            bindService(new Intent(this, LocationService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            if (mBound) {
                // Unbind from the service. This signals to the service that this activity is no longer
                // in the foreground, and the service can respond by promoting itself to a foreground
                // service.
                unbindService(mServiceConnection);
                mBound = false;
            }
        }

        if (isChanged && latitude != 0 && longitude != 0) {
            removeGeofence();
            addGeofence(latitude, longitude, GEOFENCE_RADIUS);
            sharedPref.edit().putBoolean("GEOFENCE_CHANGE", false).commit();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
//        if (client != null && !client.isConnected()) {
//            client.connect();
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
//        }
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LIFE_CYCLE", "onStop");
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // show add reminder bottom fragment
    private void showBottomSheetDialog() {
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    // show add list/category bottom fragment
    private void showAddListBottomSheetDialog() {
        addListBottomFragment.show(getSupportFragmentManager(), addListBottomFragment.getTag());
    }


    /**
     * You can filter the reminders shown on the screen, by selecting from the menu-
     * the reminders that are due today, tomorrow and all reminders or selecting the by category.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SubMenu subMenu = navigationView.getMenu().addSubMenu("Categories");
        categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                subMenu.clear();
                for (Category category :
                        categories) {
                    categoryList.add(category);
                    MenuItem item = subMenu.add(1, categoryList.indexOf(category), categoryList.size(), category.getName());
                    item.setActionView(new ImageButton(MainActivity.this));
                    item.getActionView().setBackground(getDrawable(R.drawable.ic_baseline_delete_forever_24));
                    item.getActionView().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            navigationView.getMenu().removeItem(getTaskId());
                            CategoryViewModel.delete(category);
                            return true;
                        }
                    });
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_signout:
                //sign user out
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                break;
        }

        Log.d("MENU", "onOptionsItemSelected " + item.getItemId());

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.nav_today) {
            toolbar.setTitle(getString(R.string.app_name) + "-" + getString(R.string.menu_today));
            reminderViewModel.setFilterToday(true);
            drawerLayout.closeDrawers();
            return true;
        }

        if (id == R.id.nav_tomorrow) {
            toolbar.setTitle(getString(R.string.app_name) + "-" + getString(R.string.menu_tomorrow));
            reminderViewModel.setFilterTomorrow(true);
            drawerLayout.closeDrawers();
            return true;
        }

        if (id == R.id.nav_all) {
            toolbar.setTitle(getString(R.string.app_name));
            reminderViewModel.setFilterCategory(null);
            drawerLayout.closeDrawers();
            return true;
        }

        if (id == R.id.nav_add_list) {
            showAddListBottomSheetDialog();
            Log.d("MENU", "onOptionsItemSelected nav_add_list");
            return true;
        }

        // Filter the view by the selected category/list
        // e.g view only "Shopping" reminders
        if (navigationView.getMenu().getItem(id) != null) {
            toolbar.setTitle(getString(R.string.app_name) + "-" + categoryList.get(id).getName());
            reminderViewModel.setFilterCategory(categoryList.get(id));
            recyclerViewAdapter.notifyDataSetChanged();
            drawerLayout.closeDrawers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* When the reminder to-do row is clicked- used for edit/delete the reminder */
    @Override
    public void onTodoClick(Reminder reminder) {
        Log.d("CLICK", "onTodoClick " + reminder.toString());
        sharedViewModel.selectItem(reminder);
        sharedViewModel.setIsEdit(true);
        showBottomSheetDialog();
    }

    /* reminder to do radio button clicked- the reminder has done- delete and check if repeated */
    @Override
    public void onTodoRadioButtonClick(Reminder reminder) {
        Log.d("CLICK", "onTodoRadioButtonClick " + reminder.getReminder());
        Log.d("CLICK", "onTodoRadioButtonClick isRepeat " + reminder.isRepeat());
        ReminderViewModel.delete(reminder);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isSnoozeHigh = sharedPref.getBoolean("snooze_high", false);
        cancelAlarm(reminder);
        recyclerViewAdapter.notifyDataSetChanged();
        if (reminder.isRepeat()) {
            Reminder newReminder = reminder;
            reminderRepeatHandle(newReminder);
        }
        if (isSnoozeHigh && reminder.getPriority() == Priority.HIGH) {
            scheduleDelayNotification(reminder);
        }
    }

    private void reminderRepeatHandle(Reminder reminder) {
        Log.d("REPEAT", "reminderRepeatHandle isRepeat" + reminder.isRepeat());
        if (reminder.isRepeat()) {
            Calendar c = Calendar.getInstance();
            c.setTime(reminder.dueDate);
            if (reminder.getRepeatType() == RepeatType.DAILY) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            } else if (reminder.getRepeatType() == RepeatType.WEEKLY) {
                c.add(Calendar.DAY_OF_YEAR, 7);
            } else if (reminder.getRepeatType() == RepeatType.MONTHLY) {
                c.add(Calendar.MONTH, 1);
            }
            reminder.setDueDate(c.getTime());

            // if next date is due date then stop repeat and delete
            if (reminder.getRepeatEnd() != null && reminder.getRepeatEnd().before(reminder.getDueDate())) {
                reminder.setRepeat(false);
                ReminderViewModel.delete(reminder);
                recyclerViewAdapter.notifyDataSetChanged();
            } else { //add new repeated reminder
                Log.d("REPEAT", "reminderRepeatHandle " + reminder.getReminder());
                Reminder newReminder = new Reminder(reminder.getReminder(), reminder.getPriority(), reminder.getDueDate(), reminder.getDateCreated(), reminder.getDueHour(), reminder.getDueMin(), false, reminder.isRepeat(), reminder.getRepeatType(), reminder.getRepeatEnd());
                newReminder.setUserId(remindeyApi.getUserId());
                newReminder.setNotificationId(new Random().nextInt());
                ReminderViewModel.insert(newReminder);
                onAddTodoClick(newReminder);
            }
        }

    }

    /* Called when added new reminder, and create new alarm/notification if alarm is set
     * in the the reminder  */
    @Override
    public void onAddTodoClick(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("REMINDER_TEXT", reminder.getReminder());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminder.getNotificationId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("NOTIFICATION_ID", "scheduleNotification " + reminder.getNotificationId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTime(reminder.getDueDate());
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getDueHour());
        calendar.set(Calendar.MINUTE, reminder.getDueMin());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("NOTIFICATION", "scheduleNotification " + calendar.getTime());
    }

    public void cancelAlarm(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(getApplicationContext(), ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), reminder.getNotificationId(), myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }


    private void scheduleDelayNotification(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("REMINDER_TEXT", reminder.getReminder());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminder.getNotificationId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("NOTIFICATION_ID", "scheduleNotification " + reminder.getNotificationId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, SNOOZE_HIGH_DELAY);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Log.d("NOTIFICATION", "scheduleNotification " + calendar.getTime());
    }


    @Override
    public void onLocationChanged(@NonNull @NotNull Location location) {
        if (location != null) {
            Log.d("LOCATION", "onLocationChanged my location: lat:" + location.getLatitude() + "alt: " + location.getAltitude()
                    + " long: " + location.getLongitude());
        }

    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String permission :
                wantedPermissions) {
            if (!hasPermission(permission)) {
                result.add(permission);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_PERMISSION_RESULT:
                for (String permission :
                        permissionsRejected) {
                    if (!hasPermission(permission)) {
                        permissionsRejected.add(permission);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            Toast.makeText(MainActivity.this, "You must have location permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
//                else {
//                    if (client != null) {
//                        client.connect();
//                    }
//                }
                break;
        }

    }


    private void addGeofence(double latitude, double longitude, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(remindeyApi.getUserId(), latitude, longitude, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ADD_GEOFENCE", "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d("ADD_GEOFENCE", "onFailure: " + errorMessage);
                    }
                });
    }

    public void removeGeofence() {
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("GEOFENCE", "geofence was removed");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("GEOFENCE", "geofence was not removed");

            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
    }

    private boolean checkPermissions() {
        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) && (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.d("TAG", "Displaying permission rationale to provide additional context.");
            Snackbar.make(MainActivity.this,
                    findViewById(R.id.mainActivity),
                    "Permissions Request",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.d("TAG", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


}
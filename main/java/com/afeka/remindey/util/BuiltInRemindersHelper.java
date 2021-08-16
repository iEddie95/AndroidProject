package com.afeka.remindey.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.afeka.remindey.MainActivity;
import com.afeka.remindey.R;
import com.afeka.remindey.logic.Priority;
import com.afeka.remindey.logic.Reminder;
import com.afeka.remindey.logic.RepeatType;
import com.afeka.remindey.model.ReminderViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * BuiltInRemindersHelper use to manage some pre-made reminders that the user can create easily from the setting.
 * The reminders created based on home location that the user can set in the settings, and also create from ready templates.
 * The reminders are saved in the Firestore DB for each user.
 */

public class BuiltInRemindersHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    private final String TABLE_NAME = "BUILT_IN_REMINDERS";
    private final int LOCK_HOME_IN_DELAY_ALERT = 5;
    private final int IN_DELAY_ALERT = 10;
    private final int LOCK_HOME_OUT_DELAY_ALERT = 0;
    Reminder reminder;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(TABLE_NAME);
    private List<Reminder> reminders;
    private int min;
    private int hour;


    public BuiltInRemindersHelper(Context base) {
        super(base);
        reminder = new Reminder();

    }

    public void createNewReminder(BuildInReminders type) {
        this.reminders = new ArrayList<>();
        this.reminder = new Reminder();
        initReminder(type);
    }

    private void initReminder(BuildInReminders type) {
        collectionReference
                .whereEqualTo("reminderName", type.toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Reminder> reminderList = new ArrayList<>();
                        for (QueryDocumentSnapshot ds :
                                queryDocumentSnapshots) {
                            Reminder reminder = ds.toObject(Reminder.class);
                            reminder.setUserId(RemindeyApi.getInstance().getUserId());
                            reminder.setDateCreated(Calendar.getInstance().getTime());
                            reminder.setNotificationId(new Random().nextInt());
                            Calendar c = Calendar.getInstance();
                            if (reminder.getDueHour() == 0) {
                                reminder.setDueHour(c.get(Calendar.HOUR_OF_DAY));
                                reminder.setDueMin(c.get(Calendar.MINUTE));
                            }
                            c.set(Calendar.HOUR_OF_DAY, reminder.getDueHour());
                            c.set(Calendar.MINUTE, reminder.getDueMin());
                            reminder.setDueDate(c.getTime());
                            reminderList.add(reminder);
                            Log.d("BuildInReminder", "onSuccess " + reminder.toString());
                        }
                        if (!reminderList.isEmpty()) {
                            reminders.addAll(reminderList);
                            handleReminders(type, reminderList);
                        }
                    }
                });
    }

    private void handleReminders(BuildInReminders type, List<Reminder> reminderList) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch (type) {
            case LOCK_HOME_IN:
                reminder = reminderList.get(0);
//                reminder = reminders.get(0);
                ReminderViewModel.insert(reminder);
                createNotification(reminder, LOCK_HOME_IN_DELAY_ALERT, true);
                break;
            case LOCK_HOME_OUT:
                reminder = reminderList.get(0);
//                reminder = reminders.get(0);
                ReminderViewModel.insert(reminder);
                sendHighPriorityNotification("Home leave reminder", reminder.getReminder(), MainActivity.class);
                break;
            case FEED_PET_ROUTINE:
                for (Reminder r :
                        reminders) {
                    r.setReminder(r.getReminder().replace("%s", preference.getString("FEED_PET_ROUTINE", "")));
                    Log.d("FEED_PET", preference.getString("FEED_PET_ROUTINE", ""));
                    ReminderViewModel.insert(r);
                    createNotification(r, LOCK_HOME_OUT_DELAY_ALERT, false);
                }
                break;
            case FEED_PET_HOME:
                reminder = reminderList.get(0);
//                reminder = reminders.get(0);
                reminder.setReminder(reminder.getReminder().replace("%s", preference.getString("FEED_PET_ROUTINE", "pet")));
                ReminderViewModel.insert(reminder);
                createNotification(reminder, IN_DELAY_ALERT, true);
                break;
            case LAUNDRY_CHECK:
                reminder = reminderList.get(0);
                ReminderViewModel.insert(reminder);
                createNotification(reminder, IN_DELAY_ALERT, true);
                break;
            case PILLS_REMINDER:
                reminder = reminders.get(0);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                int count = sharedPreferences.getInt("timeSetCount", 1);
                Log.d("PILLS_REMINDER", "timeSetCount " + count);
                boolean isFoodReminders = sharedPreferences.getBoolean("enable_food", false);
                Log.d("PILLS_REMINDER", "enable food reminders " + isFoodReminders);
                for (int i = 0; i < count; i++) {
                    Log.d("PILLS_REMINDER", "i=" + i);
                    Calendar calendar = Calendar.getInstance();
                    Log.d("PILLS_REMINDER", "alarm_" + (i + 1) + "Min");
                    min = sharedPreferences.getInt("alarm_" + (i + 1) + "Min", 0);
                    hour = sharedPreferences.getInt("alarm_" + (i + 1) + "Hour", 8);
                    calendar.set(Calendar.MINUTE, min);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.SECOND, 0);
                    reminder.setDueDate(calendar.getTime());
                    reminder.setDueMin(min);
                    reminder.setDueHour(hour);
                    if (sharedPreferences.getBoolean("isRepeatEndPills", false)) {
                        String strDate = sharedPreferences.getString("timeSetEndDate", "");
                        Date date = Utils.stringToSlashDate(strDate);
                        if (date != null) {
                            reminder.setRepeatEnd(date);
                        }
                    }
                    reminder.setReminder(reminder.getReminder().replace("%s", sharedPreferences.getString("pill_name", "pill")));
                    Log.d("PILLS_REMINDER", "insert " + reminder.toString());
                    //notification
                    createNotification(reminder, 0, false);
                    //insert
                    ReminderViewModel.insert(reminder);
                    if (isFoodReminders) {
                        createDependedReminder(BuildInReminders.FOOD_REMINDER, reminder);
                    }
                }
                break;
        }
    }

    private void createDependedReminder(BuildInReminders type, Reminder reminder) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Reminder newReminder = new Reminder();
        newReminder.setReminder("Don't forget to eat");
        newReminder.setRepeat(true);
        newReminder.setPriority(Priority.MEDIUM);
        newReminder.setDone(false);
        newReminder.setRepeatType(RepeatType.DAILY);
        newReminder.setUserId(RemindeyApi.getInstance().getUserId());
        newReminder.setNotificationId(new Random().nextInt());
        int eatBeforeAfter = preference.getInt("eatBeforeAfter", -1);
        int whenEatReminders = preference.getInt("whenEatReminders", 60);
        Calendar calendar = Calendar.getInstance();
        newReminder.setDateCreated(calendar.getTime());
        calendar.setTime(reminder.getDueDate());
        Log.d("FOOD_REMINDER", "when to eat " + eatBeforeAfter + " " + whenEatReminders);
        calendar.add(Calendar.MINUTE, (eatBeforeAfter * whenEatReminders));
        newReminder.setDueDate(calendar.getTime());
        newReminder.setDueMin(calendar.get(Calendar.MINUTE));
        newReminder.setDueHour(calendar.get(Calendar.HOUR_OF_DAY));
        if (preference.getBoolean("isRepeatEndPills", false)) {
            String strDate = preference.getString("timeSetEndDate", "");
            Date date = Utils.stringToSlashDate(strDate);
            if (date != null) {
                newReminder.setRepeatEnd(date);
            }
        }
        createNotification(newReminder, 0, false);
        ReminderViewModel.insert(newReminder);
        Log.d("FOOD_REMINDER", newReminder.toString());
    }

    private void createNotification(Reminder reminder, int delay, boolean isDelay) {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(getApplicationContext(), ReminderReceiver.class);
        notificationIntent.putExtra("REMINDER_TEXT", reminder.getReminder());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminder.getNotificationId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("NOTIFICATION_ID", "scheduleNotification " + reminder.getNotificationId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (isDelay) {
            calendar.add(Calendar.MINUTE, delay);
        } else {
            calendar.setTime(reminder.getDueDate());
            calendar.set(Calendar.HOUR_OF_DAY, reminder.getDueHour());
            calendar.set(Calendar.MINUTE, reminder.getDueMin());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("NOTIFICATION", "scheduleNotification " + reminder.getReminder() + " " + calendar.getTime());
    }

    public void sendHighPriorityNotification(String title, String body, Class activityName) {

        Intent intent = new Intent(this, activityName);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 267, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, channelID)
//                .setContentTitle(title)
//                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(body).setBigContentTitle(title).bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(this).notify(new Random().nextInt(), notification);
    }

    public enum BuildInReminders {
        LOCK_HOME_IN, LOCK_HOME_OUT, FEED_PET_ROUTINE, FEED_PET_HOME, LAUNDRY_CHECK, PILLS_REMINDER, FOOD_REMINDER;

        @NonNull
        @NotNull
        @Override
        public String toString() {
            switch (this) {
                case LOCK_HOME_IN:
                case LOCK_HOME_OUT:
                    return "LOCK_HOME";
                case FEED_PET_ROUTINE:
                    return "FEED_PET_ROUTINE";
                case FEED_PET_HOME:
                    return "FEED_PET_HOME";
                case LAUNDRY_CHECK:
                    return "LAUNDRY_CHECK";
                case PILLS_REMINDER:
                    return "PILLS_REMINDER";
                case FOOD_REMINDER:
                    return "FOOD_REMINDER";
                default:
                    return "";
            }
        }
    }
}

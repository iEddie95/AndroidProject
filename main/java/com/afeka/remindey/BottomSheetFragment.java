package com.afeka.remindey;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afeka.remindey.logic.Category;
import com.afeka.remindey.logic.Priority;
import com.afeka.remindey.logic.Reminder;
import com.afeka.remindey.logic.RepeatType;
import com.afeka.remindey.model.CategoryViewModel;
import com.afeka.remindey.model.ReminderViewModel;
import com.afeka.remindey.model.SharedViewModel;
import com.afeka.remindey.util.RemindeyApi;
import com.afeka.remindey.util.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * BottomSheetFragment opens a fragment from the bottom of the Main Activity.
 * To add a new reminder, please click on the plus "+" button, insert the reminder details and click on the up arrow.
 * Calendar button: Select from three choices- today, tomorrow and next week or select the date for the reminder from the calendar.
 * Not selecting a date will auto select "today".
 * For create an alarm click on "Set time" and pick the desirable time, not selecting will not trigger an alarm.
 * Priority: Select for choosing the priority, if choosing high and enabling the feature in the settings,
 * high priority reminders will trigger a reminder 10 minutes after done.
 * Repeat: select the repeat frequency of the reminders/alerts- daily, weekly, monthly or never. Also you can select the end date of the repeat.
 * Categories: You can add a list for organizing the reminders by categories.
 * The reminders are saved in the Firestore DB for each user.
 */

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener, OnTimeSetListener, OnDateSetListener {

    Calendar calendar = Calendar.getInstance();
    Calendar calendarTime;
    private EditText enterTodo;
    private ImageButton calendarButton;
    private ImageButton priorityButton;
    private ImageButton repeatButton;
    private ImageButton categoryButton;
    private RadioGroup priorityRadioGroup;
    private RadioButton selectedRadioButton;
    private int selectedButtonId;
    private ImageButton saveButton;
    private CalendarView calendarView;
    private Group calendarGroup;
    private Chip setTimeChip;
    private RadioGroup repeatRadioGroup;
    private RadioButton selectRepeatButton;
    private Button deleteButton;
    private ListView categoryListView;
    private Group categoryGroup;
    private RadioGroup repeatEndRadioGroup;
    private RadioButton selectRepeatEndButton;
    private CategoryViewModel categoryViewModel;
    private Date dueDate;
    private SharedViewModel sharedViewModel;
    private boolean isEdit;
    private boolean isRepeat = false;
    private boolean isAlarm = false;
    private boolean isDueDate = false;
    private boolean isPriority = false;
    private boolean isCategory = false;
    private Priority priority = Priority.LOW;
    private RepeatType repeatType = RepeatType.NEVER;
    private Category category;
    private Date repeatEndDate;
    private OnTodoClickListener onTodoClickListener;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection(MainActivity.REMINDER_TABLE);

    public BottomSheetFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        calendarGroup = view.findViewById(R.id.calendar_group);
        calendarView = view.findViewById(R.id.calendar_view);
        calendarButton = view.findViewById(R.id.today_calendar_button);
        enterTodo = view.findViewById(R.id.enter_todo_et);
        saveButton = view.findViewById(R.id.save_todo_button);
        priorityButton = view.findViewById(R.id.priority_todo_button);
        priorityRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_priority);
        repeatButton = view.findViewById(R.id.repeat_button);
        repeatRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_repeat);
        repeatEndRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_repeat_end);
        deleteButton = (Button) view.findViewById(R.id.button_delete);
        categoryButton = view.findViewById(R.id.category_button);
        categoryListView = (ListView) view.findViewById(R.id.category_list_view);
        categoryGroup = view.findViewById(R.id.category_list_group);

        Chip toadyChip = view.findViewById(R.id.today_chip);
        toadyChip.setOnClickListener(this);
        Chip tomorrowChip = view.findViewById(R.id.tomorrow_chip);
        tomorrowChip.setOnClickListener(this);
        Chip nextWeekChip = view.findViewById(R.id.next_week_chip);
        nextWeekChip.setOnClickListener(this);

        setTimeChip = view.findViewById(R.id.set_time_chip);

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

        dueDate = Calendar.getInstance().getTime();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the reminder from the shared view model in case of Edit Reminder event
        if (sharedViewModel.getSelectedItem().getValue() != null) {
            isEdit = sharedViewModel.getIsEdit();
            Reminder reminder = sharedViewModel.getSelectedItem().getValue();
            enterTodo.setText(reminder.getReminder());
            if (isEdit) {
                deleteButton.setVisibility(deleteButton.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
            Log.d("EDIT_REMINDER", "onViewCreated " + reminder.toString());
        }

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                Log.d("CATEGORY", "onViewCreated" + categories.toString());
                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(BottomSheetFragment.this.getContext(), android.R.layout.simple_list_item_1, categories);
                categoryListView.setAdapter(categoryAdapter);
            }
        });

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CALENDAR", "onClick " + calendarGroup.getVisibility());
                calendarGroup.setVisibility(calendarGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatRadioGroup.setVisibility(repeatRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                priorityRadioGroup.setVisibility(priorityRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                categoryGroup.setVisibility(categoryGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);

                Utils.hideSoftKeyboard(view);
            }
        });

        // Get and set the selected date
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                Log.d("CAL", "onViewCreted " + "year " + year + " month " + (month + 1) + " day of month " + dayOfMonth);
                calendar.clear();
                calendar.set(year, month, dayOfMonth);
                Log.d("CAL", "onSelectedDayChange " + calendar.getTime());
                dueDate = calendar.getTime();
                isDueDate = true;
            }
        });

        priorityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideSoftKeyboard(view);
                priorityRadioGroup.setVisibility(priorityRadioGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                calendarGroup.setVisibility(calendarGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatRadioGroup.setVisibility(repeatRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                categoryGroup.setVisibility(categoryGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);

                // Get and set the selected priority
                priorityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        isPriority = true;
                        if (priorityRadioGroup.getVisibility() == View.VISIBLE) {
                            selectedButtonId = i;
                            selectedRadioButton = (RadioButton) getView().findViewById(selectedButtonId);
                            if (selectedRadioButton.getId() == R.id.radioButton_high) {
                                priority = Priority.HIGH;
                            } else if (selectedRadioButton.getId() == R.id.radioButton_med) {
                                priority = Priority.MEDIUM;
                            } else {
                                priority = Priority.LOW;
                            }
                        } else { //In case nothing is selected default will always be LOW
                            priority = Priority.LOW;
                        }
                    }
                });
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideSoftKeyboard(view);
                repeatRadioGroup.setVisibility(repeatRadioGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatRadioGroup.clearCheck();
                repeatEndRadioGroup.clearCheck();
                calendarGroup.setVisibility(calendarGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                priorityRadioGroup.setVisibility(priorityRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                categoryGroup.setVisibility(categoryGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);

                // Get and set the selected repeat frequency
                repeatRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        if (repeatRadioGroup.getVisibility() == View.VISIBLE) {
                            selectedButtonId = i;
                            selectRepeatButton = (RadioButton) getView().findViewById(selectedButtonId);
                            if (selectRepeatButton != null) {
                                if (selectRepeatButton.getId() == R.id.radioButton_daily) {
                                    repeatType = RepeatType.DAILY;
                                } else if (selectRepeatButton.getId() == R.id.radioButton_weekly) {
                                    repeatType = RepeatType.WEEKLY;
                                } else if (selectRepeatButton.getId() == R.id.radioButton_monthly) {
                                    repeatType = RepeatType.MONTHLY;
                                } else {
                                    repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                                    repeatType = RepeatType.NEVER;
                                    isRepeat = false;
                                }
                            }
                            if (repeatType != RepeatType.NEVER) {
                                isRepeat = true;
                                repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.GONE ? View.VISIBLE : View.VISIBLE);
                            }
                            Log.d("REPEAT", "setOnCheckedChangeListener " + isRepeat);
                        }

                    }
                });
            }
        });

        setTimeChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(view);
            }
        });


        repeatEndRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (repeatEndRadioGroup.getVisibility() == View.VISIBLE) {
                    selectedButtonId = i;
                    selectRepeatEndButton = (RadioButton) view.findViewById(selectedButtonId);
                    if (selectRepeatEndButton != null) {
                        if (selectRepeatEndButton.getId() == R.id.radioButton_repeat_end_on) {
                            showDatePickerDialog(view);
                        }
                    }
                }
            }
        });

        // Get all data, create new reminder and insert to DB
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reminder = enterTodo.getText().toString().trim();
                if (!TextUtils.isEmpty(reminder) && dueDate != null) {
                    if (isEdit) { //update selected reminder
                        editReminder(reminder);
                    } else { // create new reminder
                        createReminder(reminder);
                    }
                } else {
                    Snackbar.make(saveButton, R.string.empty_field, Snackbar.LENGTH_LONG).show();
                }
                if (BottomSheetFragment.this.isVisible()) {
                    BottomSheetFragment.this.dismiss();
                }
                calendarTime = null;
                enterTodo.setText("");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedViewModel.getSelectedItem().getValue() != null) {
                    sharedViewModel.getSelectedItem().getValue().setRepeat(false);
                    sharedViewModel.getSelectedItem().getValue().setPriority(Priority.LOW);
                    onTodoClickListener.onTodoRadioButtonClick(sharedViewModel.getSelectedItem().getValue());
                    deleteButton.setVisibility(deleteButton.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                    if (BottomSheetFragment.this.isVisible()) {
                        BottomSheetFragment.this.dismiss();
                    }
                }
            }
        });

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideSoftKeyboard(view);
                categoryGroup.setVisibility(categoryGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                priorityRadioGroup.setVisibility(priorityRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                calendarGroup.setVisibility(calendarGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatEndRadioGroup.setVisibility(repeatEndRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
                repeatRadioGroup.setVisibility(repeatRadioGroup.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);

                categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        isCategory = true;
                        category = (Category) adapterView.getItemAtPosition(i);
                        adapterView.setSelection(i);
                        Log.d("CATEGORY", "onItemClick " + category.toString());
                        categoryGroup.setVisibility(View.GONE);
                        categoryListView.setVisibility(View.GONE);
                    }
                });

            }
        });

    }

    // Create new reminder and insert
    private void createReminder(String reminder) {
        if (isAlarm) {
            calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
        }
        dueDate = calendar.getTime();
        Reminder myReminder = new Reminder();
        myReminder.setReminder(reminder);
        myReminder.setPriority(priority);
        myReminder.setDueDate(dueDate);
        myReminder.setDone(false);
        myReminder.setRepeat(isRepeat);
        myReminder.setRepeatType(repeatType);
        myReminder.setRepeatEnd(repeatEndDate);
        myReminder.setDateCreated(Calendar.getInstance().getTime());
        myReminder.setUserId(RemindeyApi.getInstance().getUserId());
        if (isAlarm) { //
            myReminder.setDueHour(calendarTime.get(Calendar.HOUR_OF_DAY));
            myReminder.setDueMin(calendarTime.get(Calendar.MINUTE));
        } else { // flag no time selected
            myReminder.setDueHour(-1);
            myReminder.setDueMin(-1);
        }
        if (isCategory) { //set category is selected
            myReminder.setCategoryID(category.getCategoryID());
        }
        if (isAlarm) {
            Random rand = new Random();
            myReminder.setNotificationId(rand.nextInt());
            Log.d("NEW_REMINDER", "setNotificationId " + myReminder.getNotificationId());
            onTodoClickListener.onAddTodoClick(myReminder);
        }
        ReminderViewModel.insert(myReminder);
        Log.d("Insert_REMINDER", "saveButton.onClick " + myReminder.toString());
    }

    // Update selected reminder
    private void editReminder(String reminder) {
        Reminder updateReminder = sharedViewModel.getSelectedItem().getValue();
        Log.d("EDIT_REMINDER", "saveButton.onClick " + updateReminder.toString());
        updateReminder.setReminder(reminder);
        updateReminder.setDateCreated(Calendar.getInstance().getTime());
        if (isPriority) { // priority changed
            updateReminder.setPriority(priority);
        }
        if (isCategory) { // category changed
            Log.d("EDIT_REMINDER", "CATEGORY " + category.getCategoryID());
            updateReminder.setCategoryID(category.getCategoryID());
        }
        if (isDueDate || isAlarm) { // date changed or time changed
            if (isAlarm) { // time changed
                calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
                updateReminder.setDueHour(calendarTime.get(Calendar.HOUR_OF_DAY));
                updateReminder.setDueMin(calendarTime.get(Calendar.MINUTE));
            } else { // time not changed- save previous
                calendar.set(Calendar.HOUR_OF_DAY, updateReminder.getDueHour());
                calendar.set(Calendar.MINUTE, updateReminder.getDueMin());
            }
            dueDate = calendar.getTime();
            updateReminder.setDueDate(dueDate);
            onTodoClickListener.onAddTodoClick(updateReminder); //update notification
        }
        Log.d("UPDATE_REMINDER", "onUpdate " + updateReminder.toString());
        ReminderViewModel.update(updateReminder);
        sharedViewModel.setIsEdit(false);
        deleteButton.setVisibility(deleteButton.getVisibility() == View.VISIBLE ? View.GONE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (id == R.id.today_chip) {
            //set date for today
            calendar.add(Calendar.DAY_OF_YEAR, 0);
            dueDate = calendar.getTime();
            Log.d("TIME", "onClick " + dueDate.toString());

        } else if (id == R.id.tomorrow_chip) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            dueDate = calendar.getTime();
            Log.d("TIME", "onClick " + dueDate.toString());
        } else if (id == R.id.next_week_chip) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            dueDate = calendar.getTime();
            Log.d("TIME", "onClick " + dueDate.toString());
        }
        isDueDate = true;
    }

    @Override
    public void onTimeSet(int hour, int min) {
        Log.d("TIME", "onTimeSet " + hour + ":" + min);
        calendarTime = Calendar.getInstance();
        calendarTime.set(Calendar.HOUR_OF_DAY, hour);
        calendarTime.set(Calendar.MINUTE, min);
        isAlarm = true;
        Log.d("TIME_SET", "after onTimeSet " + calendarTime.get(Calendar.HOUR_OF_DAY) + ":" + calendarTime.get(Calendar.MINUTE));
    }

    public void registerListener(OnTodoClickListener onTodoClickListener) {
        this.onTodoClickListener = onTodoClickListener;
    }

    @Override
    public void onDateSet(Date date) {
        if (date != null) {
            repeatEndDate = date;
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {

    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        ((TimePickerFragment) newFragment).registerListener(this);
        newFragment.show(getParentFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).registerListener(this);
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        isEdit = false;
        isRepeat = false;
        sharedViewModel.setIsEdit(false);
        enterTodo.setText("");
        priority = Priority.LOW;
        repeatType = RepeatType.NEVER;
        isPriority = false;
        isAlarm = false;
        isDueDate = false;
        isCategory = false;
        dueDate = Calendar.getInstance().getTime();
        sharedViewModel.selectItem(null);
        calendar = Calendar.getInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        isEdit = false;
        isRepeat = false;
        enterTodo.setText("");
        priority = Priority.LOW;
        isPriority = false;
        isAlarm = false;
        isDueDate = false;
        isCategory = false;
        sharedViewModel.setIsEdit(false);
        repeatType = RepeatType.NEVER;
        dueDate = Calendar.getInstance().getTime();
        calendar = Calendar.getInstance();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isEdit = false;
        isRepeat = false;
        sharedViewModel.setIsEdit(false);
        enterTodo.setText("");
        priority = Priority.LOW;
        repeatType = RepeatType.NEVER;
        isAlarm = false;
        isDueDate = false;
        isCategory = false;
        isPriority = false;
        calendar = Calendar.getInstance();
        dueDate = Calendar.getInstance().getTime();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    /**
     * Time Picker for UI, user can choose the time for the reminder alert
     */
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private OnTimeSetListener listener;

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
            Log.d("TimePicker", "registering...");
            this.listener = listener;
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
        }
    }
}
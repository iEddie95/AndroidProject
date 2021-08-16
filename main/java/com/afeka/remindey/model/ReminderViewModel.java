package com.afeka.remindey.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.afeka.remindey.data.RemindeyRepository;
import com.afeka.remindey.logic.Category;
import com.afeka.remindey.logic.Reminder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ViewModel for showing and managing the reminders in the UI and DB
 */


public class ReminderViewModel extends AndroidViewModel {
    public static RemindeyRepository repository;

    public MutableLiveData<List<Reminder>> allReminders;
    public MutableLiveData<Category> filterCategory;
    public MutableLiveData<Reminder> reminderLiveData;


    public ReminderViewModel(@NonNull @NotNull Application application) {
        super(application);
        this.repository = new RemindeyRepository();
        this.allReminders = repository.getAllReminder();
        this.filterCategory = repository.getFilterCategory();
        this.reminderLiveData = repository.getReminderLiveData();
    }

    public static void update(Reminder reminder) {
        repository.update(reminder);
    }

    public static void insert(Reminder reminder) {
        repository.insert(reminder);
    }

    public static void delete(Reminder reminder) {
        repository.delete(reminder);
    }

    public MutableLiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public void setFilterCategory(Category category) {
        repository.setFilterCategory(category);
    }

    public void setFilterToday(boolean bool) {
        repository.setTodayFilter(bool);
    }

    public void setFilterTomorrow(boolean bool) {
        repository.setTomorrowFilter(bool);
    }

    public MutableLiveData<Reminder> getReminderLiveData() {
        return reminderLiveData;
    }

}

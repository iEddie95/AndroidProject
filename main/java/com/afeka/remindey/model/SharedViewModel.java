package com.afeka.remindey.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.afeka.remindey.logic.Reminder;

/**
 * ViewModel for the selected reminder from the UI
 */

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Reminder> selectedItem = new MutableLiveData<>();
    private boolean isEdit;

    public void selectItem(Reminder reminder) {
        selectedItem.setValue(reminder);
    }

    public LiveData<Reminder> getSelectedItem() {
        return selectedItem;
    }

    public boolean getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }
}

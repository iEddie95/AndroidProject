package com.afeka.remindey.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.afeka.remindey.data.RemindeyRepository;
import com.afeka.remindey.logic.Category;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ViewModel for showing and managing the categories in the UI and DB
 */

public class CategoryViewModel extends AndroidViewModel {

    public static RemindeyRepository repository;
    public final MutableLiveData<List<Category>> allCategories;

    public CategoryViewModel(@NonNull @NotNull Application application) {
        super(application);

        this.repository = ReminderViewModel.repository;
        this.allCategories = repository.getAllCategories();
    }

    public static void insert(Category category) {
        repository.insertCategory(category);
    }

    public static void delete(Category category) {
        repository.deleteCategory(category);
    }

    public MutableLiveData<List<Category>> getAllCategories() {
        return allCategories;
    }
}

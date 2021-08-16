package com.afeka.remindey.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.afeka.remindey.MainActivity;
import com.afeka.remindey.logic.Category;
import com.afeka.remindey.logic.Reminder;
import com.afeka.remindey.util.RemindeyApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/** RemindeyRepository handles all the read/write functions from and to Firestore DB*/

public class RemindeyRepository {

    private MutableLiveData<List<Reminder>> allReminders;
    private MutableLiveData<List<Category>> allCategories;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(MainActivity.REMINDER_TABLE);
    private CollectionReference categoryCollectionReference = db.collection(MainActivity.CATEGORY_TABLE);

    private RemindeyApi remindeyApi = RemindeyApi.getInstance();
    private MutableLiveData<Reminder> reminderLiveData;
    private MutableLiveData<Category> categoryLiveData;
    private MutableLiveData<Category> filterCategory;

    private boolean isTodayFilter = false;
    private boolean isTomorrowFilter = false;


    public RemindeyRepository() {
        this.allReminders = new MutableLiveData<>();
        this.allCategories = new MutableLiveData<>();
        this.reminderLiveData = new MutableLiveData<>();
        this.filterCategory = new MutableLiveData<>();
        this.allReminders = getFromDb();
        this.categoryLiveData = new MutableLiveData<>();
        this.allCategories = getCategoryFromDb();
    }

    public MutableLiveData<List<Reminder>> getFromDb() {
        if (isTodayFilter) {
            getFromDbToday();
        } else if (isTomorrowFilter) {
            getFromDbTomorrow();
        } else if (filterCategory.getValue() != null) {
            collectionReference.whereEqualTo("userId", remindeyApi.getUserId())
                    .whereEqualTo("categoryID", filterCategory.getValue().getCategoryID())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<Reminder> reminderList = new ArrayList<>();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    if (snapshot != null) {
                                        Reminder reminder = snapshot.toObject(Reminder.class);
                                        Log.d("ADD_REMINDER", "onSuccess " + reminder.toString());
                                        reminderList.add(reminder);
                                    }
                                }
                                // Invoke recyclerview
                            }
                            allReminders.postValue(reminderList);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("REPOSITORY", "onFailure Reminder" + e.toString());
                        }
                    });
        } else {
            collectionReference.whereEqualTo("userId", remindeyApi.getUserId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<Reminder> reminderList = new ArrayList<>();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    if (snapshot != null) {
                                        Reminder reminder = snapshot.toObject(Reminder.class);
                                        Log.d("ADD_REMINDER", "onSuccess " + reminder.toString());
                                        reminderList.add(reminder);
                                    }
                                }
                            }
                            allReminders.postValue(reminderList);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("REPOSITORY", "onFailure Reminder" + e.toString());
                        }
                    });
        }

        return allReminders;
    }

    private void getFromDbToday() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        Date todayStart = cal.getTime();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        Date todayEnd = cal.getTime();
        Log.d("REPOSITORY", "getFromDbToday " + todayStart + " " + todayEnd);
        collectionReference.whereEqualTo("userId", remindeyApi.getUserId())
                .whereGreaterThan("dueDate", todayStart)
                .whereLessThan("dueDate", todayEnd)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Reminder> reminderList = new ArrayList<>();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                if (snapshot != null) {
                                    Reminder reminder = snapshot.toObject(Reminder.class);
                                    Log.d("ADD_REMINDER", "onSuccess " + reminder.toString());
                                    reminderList.add(reminder);
                                }
                            }

                        }
                        allReminders.postValue(reminderList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d("REPOSITORY", "onFailure Reminder" + e.toString());
                    }
                });
    }

    private void getFromDbTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        Date todayStart = cal.getTime();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
        Date todayEnd = cal.getTime();
        Log.d("REPOSITORY", "getFromDbToday " + todayStart + " " + todayEnd);
        collectionReference.whereEqualTo("userId", remindeyApi.getUserId())
                .whereGreaterThan("dueDate", todayStart)
                .whereLessThan("dueDate", todayEnd)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Reminder> reminderList = new ArrayList<>();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                if (snapshot != null) {
                                    Reminder reminder = snapshot.toObject(Reminder.class);
                                    Log.d("ADD_REMINDER", "onSuccess " + reminder.toString());
                                    reminderList.add(reminder);
                                }
                            }
                            // Invoke recyclerview
                        }
//                            reminderList.sort(new ReminderComparator());
                        allReminders.postValue(reminderList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d("REPOSITORY", "onFailure Reminder" + e.toString());
                    }
                });
    }

    public MutableLiveData<List<Category>> getCategoryFromDb() {
        categoryCollectionReference.whereEqualTo("userId", remindeyApi.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Category> categoryList = new ArrayList<>();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                if (snapshot != null) {
                                    Category category = snapshot.toObject(Category.class);
                                    Log.d("ADD_CATEGORY", "onSuccess " + category.toString());
                                    categoryList.add(category);
                                }
                            }
                            // Invoke recyclerview
                        }
                        allCategories.postValue(categoryList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d("REPOSITORY", "onFailure Category" + e.toString());
                    }
                });
        return allCategories;
    }


    public MutableLiveData<Reminder> getReminderLiveData() {
        return reminderLiveData;
    }

    public MutableLiveData<List<Reminder>> getAllReminder() {
        return allReminders;
    }

    public MutableLiveData<List<Category>> getAllCategories() {
        return allCategories;
    }


    /* INSERT TO FIRESTORE*/
    public MutableLiveData<Reminder> insert(Reminder reminder) {
        collectionReference.add(reminder)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        reminder.setReminderID(documentReference.getId());
                        documentReference.update("reminderID", documentReference.getId());
                        Log.d("REPOSITORY", "insert onSuccess reminder " + reminder.toString());
                        reminderLiveData.postValue(reminder);
                        getFromDb();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d("ERROR_TAG", "onFailure" + e.toString());
                    }
                });
        return reminderLiveData;
    }

    public void insertCategory(Category category) {
        categoryCollectionReference.add(category)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        category.setCategoryID(documentReference.getId());
                        documentReference.update("categoryID", documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d("ERROR_TAG", "onFailure" + e.toString());
                    }
                });
        getCategoryFromDb();
    }

    /* DELETE FROM FIRESTORE*/
    public void delete(Reminder reminder) {
        if (reminder != null) {
            collectionReference.document(reminder.getReminderID()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("DELETE_REMINDER", "onSuccess " + reminder.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("DELETE_REMINDER", "onFailure " + e.toString());
                        }
                    });
        }
        getFromDb();
    }

    public void deleteCategory(Category category) {
        if (category != null) {
            categoryCollectionReference.document(category.getCategoryID()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("DELETE_REMINDER", "onSuccess " + category.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("DELETE_REMINDER", "onFailure " + e.toString());
                        }
                    });
        }
        getCategoryFromDb();
    }

    /* UPDATE FIRESTORE DOC*/
    public void update(Reminder reminder) {
        collectionReference.document(reminder.getReminderID()).update(
                "reminderID", reminder.getReminderID(),
                "reminder", reminder.getReminder(),
                "priority", reminder.getPriority(),
                "dueDate", reminder.getDueDate(),
                "dateCreated", reminder.getDateCreated(),
                "dueHour", reminder.getDueHour(),
                "dueMin", reminder.getDueMin(),
                "isDone", reminder.getDone(),
                "isRepeat", reminder.isRepeat(),
                "repeatType", reminder.getRepeatType(),
                "repeatEnd", reminder.getRepeatEnd(),
                "userId", reminder.getUserId(),
                "categoryID", reminder.getCategoryID(),
                "isOverdue", reminder.isOverdue()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("REPOSITORY", "onSuccess update " + reminder.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("REPOSITORY", "onFailure update " + e.toString());
            }
        });
        getFromDb();
    }

    public MutableLiveData<Category> getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(Category category) {
        filterCategory.setValue(category);
        isTodayFilter = false;
        isTomorrowFilter = false;
        getFromDb();
    }

    public void setTodayFilter(Boolean bool) {
        isTodayFilter = bool;
        filterCategory.setValue(null);
        isTomorrowFilter = false;
        getFromDb();
    }

    public void setTomorrowFilter(Boolean bool) {
        isTomorrowFilter = bool;
        filterCategory.setValue(null);
        isTodayFilter = false;
        getFromDb();
    }

}

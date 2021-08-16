package com.afeka.remindey.util;

import android.app.Application;

/**
 * Stores the current userID, email, for using across the entire app runtime
 */
public class RemindeyApi extends Application {
    private static RemindeyApi instance;
    private String username;
    private String userId;
    private String userEmail;

    public RemindeyApi() {
    }

    public static RemindeyApi getInstance() {
        if (instance == null)
            instance = new RemindeyApi();
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}

package com.afeka.remindey.logic;

import java.util.Date;

public class Reminder {
    public static int ID = 0;

    public String reminderID;
    public Priority priority;
    public Date dueDate;
    public Date dateCreated;
    public int dueHour;
    public int dueMin;
    private String reminder;
    private Boolean isDone;

    private boolean isRepeat;

    private RepeatType repeatType;

    private Date repeatEnd;

    private String categoryID;

    private String userId;

    private int notificationId;

    private boolean isOverdue;

    //empty ctor for Firestore
    public Reminder() {
    }

    public Reminder(String reminder, Priority priority, Date dueDate, Date dateCreated, int dueHour, int dueMin, boolean isDone, boolean isRepeat, RepeatType repeatType, Date repeatEnd) {
        this.reminder = reminder;
        this.priority = priority;
        this.dueDate = dueDate;
        this.dateCreated = dateCreated;
        this.dueHour = dueHour;
        this.dueMin = dueMin;
        this.isDone = isDone;
        this.isRepeat = isRepeat;
        this.repeatType = repeatType;
        this.repeatEnd = repeatEnd;
    }

    public static int getID() {
        return ID;
    }

    public String getReminderID() {
        return reminderID;
    }

    public void setReminderID(String reminderID) {
        this.reminderID = reminderID;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    public int getDueHour() {
        return dueHour;
    }

    public void setDueHour(int dueHour) {
        this.dueHour = dueHour;
    }

    public int getDueMin() {
        return dueMin;
    }

    public void setDueMin(int dueMin) {
        this.dueMin = dueMin;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public Date getRepeatEnd() {
        return repeatEnd;
    }

    public void setRepeatEnd(Date repeatEnd) {
        this.repeatEnd = repeatEnd;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "reminderID='" + reminderID + '\'' +
                ", reminder='" + reminder + '\'' +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", dateCreated=" + dateCreated +
                ", dueHour=" + dueHour +
                ", dueMin=" + dueMin +
                ", isDone=" + isDone +
                ", isRepeat=" + isRepeat +
                ", repeatType=" + repeatType +
                ", repeatEnd=" + repeatEnd +
                ", categoryID='" + categoryID + '\'' +
                ", userId='" + userId + '\'' +
                ", notificationId=" + notificationId +
                ", isOverdue=" + isOverdue +
                '}';
    }
}

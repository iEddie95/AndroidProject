package com.afeka.remindey.logic;

import java.util.Comparator;

/** Comparator for sorting the reminders by due date */

public class ReminderComparator implements Comparator<Reminder> {
    @Override
    public int compare(Reminder r1, Reminder r2) {
        if (r1.getDueDate().after(r2.getDueDate()))
            return 1;
        if (r1.getDueDate().before(r2.getDueDate()))
            return -1;
        return 0;
    }
}

package com.afeka.remindey;

import com.afeka.remindey.logic.Reminder;

/**
 * Interface listener that triggered when a reminder is done/added
 */
public interface OnTodoClickListener {

    void onTodoClick(Reminder reminder);

    void onTodoRadioButtonClick(Reminder reminder);

    void onAddTodoClick(Reminder reminder);
}

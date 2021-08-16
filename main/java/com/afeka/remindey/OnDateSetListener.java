package com.afeka.remindey;

import java.util.Date;

public interface OnDateSetListener {
    void onDateSet(Date date);

    void onDateSet(int year, int month, int day);

}

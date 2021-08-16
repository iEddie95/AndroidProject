package com.afeka.remindey.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.afeka.remindey.OnTodoClickListener;
import com.afeka.remindey.R;
import com.afeka.remindey.logic.Reminder;
import com.afeka.remindey.util.Utils;
import com.google.android.material.chip.Chip;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<Reminder> reminderList;
    private final OnTodoClickListener todoClickListener;

    public RecyclerViewAdapter(List<Reminder> reminderList, OnTodoClickListener todoClickListener) {
        this.reminderList = reminderList;
        this.todoClickListener = todoClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        if (reminder.getDueDate() != null) {
            String formatted = Utils.formatDate(reminder.getDueDate());
            holder.todayChip.setText(formatted);
        }

        ColorStateList colorStateList = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_enabled}
        },
                new int[]{
                        Color.LTGRAY, //disabled
                        Utils.priorityColor(reminder)
                });

        holder.reminder.setText(reminder.getReminder());
        holder.todayChip.setTextColor(Utils.priorityColor(reminder));
        holder.todayChip.setChipIconTint(colorStateList);
        holder.radioButton.setButtonTintList(colorStateList);
        if (reminder.getDueHour() == -1) {
            holder.timeChip.setVisibility(View.GONE);
        } else {
            holder.timeChip.setVisibility(View.VISIBLE);
            String formattedTime = Utils.formatTime(reminder.getDueDate());
            holder.timeChip.setText(formattedTime);
            holder.timeChip.setChipIconTint(colorStateList);
        }

    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public void updateData(List<Reminder> reminderList) {
        this.reminderList.clear();
        this.reminderList.addAll(reminderList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public AppCompatRadioButton radioButton;
        public AppCompatTextView reminder;
        public Chip todayChip;
        public Chip timeChip;

        OnTodoClickListener onTodoClickListener;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.todo_radio_button);
            reminder = itemView.findViewById(R.id.todo_row_todo);
            todayChip = itemView.findViewById(R.id.todo_row_chip);
            timeChip = itemView.findViewById(R.id.todo_time_chip);

            this.onTodoClickListener = todoClickListener;
            itemView.setOnClickListener(this);
            radioButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Reminder currReminder = reminderList.get(getAdapterPosition());
            int id = view.getId();
            if (id == R.id.todo_row_layout) {
                onTodoClickListener.onTodoClick(currReminder);
            } else if (id == R.id.todo_radio_button) {
                onTodoClickListener.onTodoRadioButtonClick(currReminder);
            }
        }
    }
}

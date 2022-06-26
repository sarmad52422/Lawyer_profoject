package com.fyp.lawyer_project.lawyer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainActivity;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Schedule;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LawyerScheduleManager extends RootFragment implements View.OnClickListener {
    private View rootView;
    private ArrayList<String> selectedDays;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.lawyer_schdule_fragment, container, false);
        initComponents();
        initActions();
        return rootView;
    }

    private void initComponents() {
        selectedDays = new ArrayList<>();

    }

    private void initActions() {
        ViewGroup daysButtonContainr = rootView.findViewById(R.id.dayPickerContainer);
        rootView.findViewById(R.id.toTime).setOnClickListener(this);
        rootView.findViewById(R.id.fromTime).setOnClickListener(this);
        rootView.findViewById(R.id.updateScheduleBtn).setOnClickListener(this);
        for (int i = 0; i < daysButtonContainr.getChildCount(); i++) {
            daysButtonContainr.getChildAt(i).setOnClickListener(new DaysButtonClickListener());
            daysButtonContainr.getChildAt(i).setTag("UnSelected");
        }
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.toTime) {
            openTimePickerDialogAndSetTime(view);

        } else if (view.getId() == R.id.fromTime) {
            openTimePickerDialogAndSetTime(view);

        } else if (view.getId() == R.id.updateScheduleBtn) {
            ViewGroup dayPickerContainer = rootView.findViewById(R.id.dayPickerContainer);
            StringBuilder days = new StringBuilder();
            for (String s : selectedDays) {
                ViewGroup parent = (ViewGroup) dayPickerContainer.getChildAt(Integer.parseInt(s) - 1);
                ViewGroup dayContainer = (ViewGroup) parent.getChildAt(0);
                TextView dayText = (TextView) dayContainer.getChildAt(0);
                days.append("-").append(dayText.getText().toString());
                Log.e("Selected Days", dayText.getText().toString());
            }

            String fromTime = ((TextView) rootView.findViewById(R.id.fromTime)).getText().toString();
            String toTime = ((TextView) rootView.findViewById(R.id.toTime)).getText().toString();
            FirebaseHelper.updateLawyerSchedule(new Schedule(fromTime, toTime, days.toString().trim()), new FirebaseHelper.FirebaseActions() {
                @Override
                public void onActionCompleted() {
                    Toast.makeText(rootView.getContext(), "Schedule Updated",Toast.LENGTH_LONG).show();

                }
            });

        }
    }

    private class DaysButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            View dayView = ((ViewGroup) view).getChildAt(0);
            if (view.getTag().equals("Selected")) {
                ((CardView) view).setCardBackgroundColor(Color.WHITE);
                view.setTag("UnSelected");
                selectedDays.remove(dayView.getTag().toString());


            } else {

                selectedDays.add(dayView.getTag().toString());
                ((CardView) view).setCardBackgroundColor(getResources().getColor(R.color.material_dynamic_primary50));
                view.setTag("Selected");

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private void openTimePickerDialogAndSetTime(View timeView) {

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(12).setMinute(0).
                setTitleText("Set Hours").build();

        timePicker.addOnPositiveButtonClickListener(view -> {
            String time = timePicker.getHour() + ":" + timePicker.getMinute();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm");


            try {
                Date d = dateFormat.parse(time);
                @SuppressLint("SimpleDateFormat") String tw = new SimpleDateFormat("KK:mm a").format(d);
                ((TextView) timeView).setText(tw);
            } catch (ParseException e) {
                Log.e("Date PR error::", e.getMessage());
            }
        });
        timePicker.show(getChildFragmentManager(), "Child");
    }
}

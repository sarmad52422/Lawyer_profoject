package com.fyp.lawyer_project.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DetailLawyerView extends BottomSheetDialog {
    private final Lawyer lawyer;

    public DetailLawyerView(@NonNull Context context, Lawyer lawyer) {
        super(context);
        setContentView(R.layout.lawyer_detail_window);
        this.lawyer = lawyer;
        initDetails();
    }


    @SuppressLint("SetTextI18n")
    private void initDetails() {
        ((TextView) findViewById(R.id.name)).setText(lawyer.getFullName());
        ((TextView) findViewById(R.id.practice)).setText(lawyer.getPracticeArea());
        ((TextView) findViewById(R.id.caseRate)).setText(lawyer.getStartPrice() + " - " + lawyer.getEndPrice());
        ((TextView) findViewById(R.id.workingDays)).setText(lawyer.getSchedule().getWorkingDays());
        ((TextView) findViewById(R.id.workingTime)).setText(lawyer.getSchedule().getFromTime() + " - " + lawyer.getSchedule().getToTime());
        findViewById(R.id.lawyerDetailCloseButton).setOnClickListener(view->dismiss());
        findViewById(R.id.appointment_booking_btn).setOnClickListener(view->bookAppointmentForCurrentUser());

    }
    private void showAppointmentBookingDialog(){

    }
    private void bookAppointmentForCurrentUser() {
        String fromTime = lawyer.getSchedule().getFromTime();
        String toTime = lawyer.getSchedule().getToTime();
        String [] workingDays = lawyer.getSchedule().getWorkingDays().split("-");
        try {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minuts = Calendar.getInstance().get(Calendar.MINUTE);
            @SuppressLint("SimpleDateFormat") Date d = (new SimpleDateFormat("hh:mm a")).parse(fromTime);
            Date current = (new SimpleDateFormat("H:mm")).parse(hour+":"+minuts);

//
//            String tw =new SimpleDateFormat("hh:mm a").format(d);
//            String stringTimeAv =new SimpleDateFormat("KK:mm a").format(curntTime);
//            Toast.makeText(getContext(),tw+" yeh avail time he",Toast.LENGTH_LONG).show();
//            Toast.makeText(getContext(),stringTimeAv+" yeh current time he",Toast.LENGTH_LONG).show();

        } catch (ParseException e) {
            e.printStackTrace();
        }
//        FirebaseHelper.bookMeetingIfAvaliable()
    }

}

package com.fyp.lawyer_project.client;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

import java.util.ArrayList;

public class MyAppointments extends Dialog {
    private final RecyclerView appointmentList;
    public MyAppointments(@NonNull Context context) {
        super(context);
        setContentView(R.layout.appointment_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appointmentList = findViewById(R.id.appointment_list);
        appointmentList.setLayoutManager(new LinearLayoutManager(context));
        loadClientAppointmentRecord();
    }
    private void loadClientAppointmentRecord(){
        String email = User.getCurrentLoggedInUser().getEmailAddress();
        String userName = "_"+email.substring(0,email.indexOf("@"));
        FirebaseHelper.loadAppointmentRecord(userName, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {

                AppointmentListAdapter adapter = new AppointmentListAdapter(getContext(),appointments);
                appointmentList.setAdapter(adapter);
                findViewById(R.id.loadingProgress).setVisibility(View.GONE);

            }
        });
    }

}

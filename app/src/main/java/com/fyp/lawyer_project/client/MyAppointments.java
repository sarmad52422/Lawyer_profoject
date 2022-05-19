package com.fyp.lawyer_project.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

import java.util.ArrayList;

public class MyAppointments extends Dialog implements AppointmentListAdapter.OnItemClickListener {
    private final RecyclerView appointmentList;

    public MyAppointments(@NonNull Context context) {
        super(context);
        setContentView(R.layout.appointment_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appointmentList = findViewById(R.id.appointment_list);
        appointmentList.setLayoutManager(new LinearLayoutManager(context));
        loadClientAppointmentRecord();
    }

    private void loadClientAppointmentRecord() {
        findViewById(R.id.loadingProgress).setVisibility(View.VISIBLE);

        String email = User.getCurrentLoggedInUser().getEmailAddress();
        String userName = "_" + email.substring(0, email.indexOf("@"));
        FirebaseHelper.loadAppointmentRecord(userName, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {

                AppointmentListAdapter adapter = new AppointmentListAdapter(getContext(), appointments);
                adapter.setOnItemClickListener(MyAppointments.this);
                appointmentList.setAdapter(adapter);
                findViewById(R.id.loadingProgress).setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onMarkCompleteButtonClicked(Appointment appointment) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Alert");
        dialog.setMessage("Mark Appointment As Complete ?");
        dialog.setPositiveButton("Yes", ((dialogInterface, i) ->{
            FirebaseHelper.markCompleteAppointment(appointment.getAppointmentId());
            loadClientAppointmentRecord();
        } ));
        dialog.setNegativeButton("No",((dialogInterface, i) -> dialogInterface.dismiss()));
        dialog.show();

    }

    @Override
    public void onCancelAppointmentButtonClicked(Appointment appointment) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Alert");
        dialog.setMessage("Cancel Appointment? ");
        dialog.setPositiveButton("Yes",((dialogInterface, i) -> {
            FirebaseHelper.cancelAppointment(appointment.getAppointmentId());
            loadClientAppointmentRecord();
        }));
        dialog.setNegativeButton("No",(dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }
}

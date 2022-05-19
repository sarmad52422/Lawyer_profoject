package com.fyp.lawyer_project.client;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailLawyerView extends BottomSheetDialog {
    private final Lawyer lawyer;

    public DetailLawyerView(@NonNull Context context, Lawyer lawyer) {
        super(context);
        setOwnerActivity((FragmentActivity) context);
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
        findViewById(R.id.lawyerDetailCloseButton).setOnClickListener(view -> dismiss());
        findViewById(R.id.appointment_booking_btn).setOnClickListener(view -> showAppointmentBookingDialog());
        findViewById(R.id.submitCaseRequest).setOnClickListener(new AppointmentDialogClickListener());

    }

    private void showAppointmentBookingDialog() {
        setContentView(R.layout.appointment_booking_dialog);
        findViewById(R.id.date_selection).setOnClickListener(new AppointmentDialogClickListener());
        findViewById(R.id.timeSlotSelector).setOnClickListener(new AppointmentDialogClickListener());
        findViewById(R.id.sendAppointmentBtn).setOnClickListener(new AppointmentDialogClickListener());



    }

    private class AppointmentDialogClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.date_selection) {
                showDatePicker(view);

            } else if (view.getId() == R.id.timeSlotSelector) {
                showTimePicker(view);
            } else if (view.getId() == R.id.sendAppointmentBtn) {
                sendAppointment();
            } else if (view.getId() == R.id.submitCaseRequest) {
                showCaseSubmissionDialog();
            }
        }


        private void sendAppointment() {
            String date = ((TextView) findViewById(R.id.date_selection)).getText().toString();
            String time = ((TextView) findViewById(R.id.timeSlotSelector)).getText().toString();
            String lawyerEmail = lawyer.getEmailAddress();
            String clientEmail = User.getCurrentLoggedInUser().getEmailAddress();
            String lawyerId = "_" + lawyerEmail.substring(0, lawyerEmail.indexOf("@"));
            String clientId = "_" + clientEmail.substring(0, clientEmail.indexOf("@"));
            String msg = ((EditText) findViewById(R.id.messageBox)).getText().toString();
            String appointmentId = lawyerId + clientId;
            SimpleDateFormat dateFormat = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US);
            try {
                Date d = dateFormat.parse(date + " " + time);
                String df = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US).format(d);
                Appointment appointment = new Appointment(appointmentId, lawyerId, clientId, df, msg);
                FirebaseHelper.sendAppointmentRequest(appointment, new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onAppointmentError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAppointSendComplete() {
                        dismiss();
                        Toast.makeText(getContext(), "Appointment Request Sent Successfully", Toast.LENGTH_LONG).show();
                    }
                });
                Log.e("APoint date = ", df);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        private void showDatePicker(View view) {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Pick Date").build();
            datePicker.show(((FragmentActivity) getOwnerActivity()).getSupportFragmentManager(), "Sopa");
            datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {
                    Date d = new Date(selection);
                    String date;
                    date = new SimpleDateFormat("EEEE dd-MMMM-yyyy", Locale.US).format(d);
                    ((TextView) view).setText(date);


                }
            });

        }
    }

    private void showTimePicker(View view) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(12).setMinute(0)
                .setTitleText("Select Time").build();
        timePicker.show(((FragmentActivity) getOwnerActivity()).getSupportFragmentManager(), "sopa");
        timePicker.addOnPositiveButtonClickListener(view1 -> {
            String time = timePicker.getHour() + ":" + timePicker.getMinute();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.US);
            try {
                Date d = sdf.parse(time);
                assert d != null;
                String selectedTime = new SimpleDateFormat("KK:mm a", Locale.US).format(d);
                ((TextView) view).setText(selectedTime);
            } catch (ParseException ignored) {
            }
        });
    }
    private void showCaseSubmissionDialog(){
        String clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.case_request_dialog);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.sendCaseRequestBtn).setOnClickListener(view->{
            String caseTitle = ((EditText)dialog.findViewById(R.id.caseTitleField)).getText().toString();
            String caseMessage = ((EditText)dialog.findViewById(R.id.caseDetailsField)).getText().toString();
            String budget = ((EditText)dialog.findViewById(R.id.caseBudgetField)).getText().toString();
            double dBudget = Double.parseDouble(budget.trim());
            String lawyerId = lawyer.getUserId();
            String clientID = "_"+clientEmail.substring(0,clientEmail.indexOf('@'));
            ClientCase clientCase = new ClientCase(caseTitle,caseMessage,lawyerId,clientID,"Not Accepted","Start","Nothing","no lawyer comment",dBudget);
            ProgressBar progressBar = new ProgressBar(getContext());
            dialog.setContentView(progressBar);
            FirebaseHelper.sendCaseRequest(clientCase, new FirebaseHelper.FirebaseActions() {
                @Override
                public void onActionCompleted() {
                    dialog.dismiss();
                    DetailLawyerView.this.dismiss();
                    Toast.makeText(getContext(),"Request Sent Successfully",Toast.LENGTH_LONG).show();
                }
            });
        });
        dialog.show();
    }





}

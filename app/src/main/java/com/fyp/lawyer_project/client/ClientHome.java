package com.fyp.lawyer_project.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.common.CommentAdapter;
import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Comment;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ClientHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener {
    private View rootView;
    private Client currentUser;
    private NavigationView navView;
    private MainFragmentActivity callBackHandel;
    private String msg = null;
    private String title = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.client_home, container, false);
        initClickListener();
        initHomeScreen();
        loadCurrentCases();
        return rootView;
    }

    private void initClickListener() {
        if (getArguments() != null) {
            String meetingId = getArguments().getString("ID");
            showMeetingConfirmationDialog(meetingId);
        }
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view -> openDrawer());
        rootView.findViewById(R.id.notifyIcon).setOnClickListener(view -> openMessageDialog());
    }

    private void loadCurrentCases() {
        RecyclerView casesRecyclerView = rootView.findViewById(R.id.lawyersList); // Repurpose this RecyclerView
        casesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));
        FirebaseHelper.loadCasesByUser(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {
                CasesAdapter adapter = new CasesAdapter(caseArrayList);
                casesRecyclerView.setAdapter(adapter);
            }
        });
    }

    private void openMessageDialog() {
        Log.d("ClientHome", "openMessageDialog called, initial msg = " + msg + ", title = " + title);
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));
        FirebaseHelper.loadAppointmentRecord(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {
                Appointment activeMeeting = null;
                for (Appointment appt : appointments) {
                    if (appt.getAppointmentStatus().equals(Appointment.STATUS_ACCEPTED)) {
                        if (activeMeeting == null || appt.getAppointmentDate().compareTo(activeMeeting.getAppointmentDate()) > 0) {
                            activeMeeting = appt;
                        }
                    }
                }

                String roomName = activeMeeting != null ? activeMeeting.getAppointmentId() : null;
                String dialogMessage = roomName != null ? "Active Meeting: " + roomName : "No active meeting found";
                if (msg != null && title != null && title.equals("meeting")) {
                    dialogMessage += "\nNotification: " + msg;
                }

                AlertDialog.Builder msgDialog = new AlertDialog.Builder(rootView.getContext());
                msgDialog.setTitle("Meeting Status");
                msgDialog.setMessage(dialogMessage);
                if (roomName != null) {
                    msgDialog.setPositiveButton("Join Meeting", (dialogInterface, i) -> {
                        joinMeeting(roomName);
                        rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.GONE);
                    });
                }
                msgDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                msgDialog.show();
            }
        });
    }

    private void joinMeeting(String meetingId) {
        try {
            String roomName = meetingId;
            Log.d("ClientHome", "Joining Jitsi meeting with roomName: " + roomName);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(roomName)
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .setFeatureFlag("lobby.enabled", false)
                    .build();
            JitsiMeetActivity.launch(getContext(), options);
        } catch (Exception e) {
            Log.e("Jitsi Error", e.getMessage());
            Toast.makeText(getContext(), "Failed to join meeting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAppointmentBookingDialog(ClientCase clientCase) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.appointment_booking_dialog);
        dialog.show();

        TextView dateSelection = dialog.findViewById(R.id.date_selection);
        TextView timeSlotSelector = dialog.findViewById(R.id.timeSlotSelector);
        EditText messageBox = dialog.findViewById(R.id.messageBox);

        dateSelection.setOnClickListener(v -> showDatePicker(dateSelection));
        timeSlotSelector.setOnClickListener(v -> showTimePicker(timeSlotSelector));
        dialog.findViewById(R.id.sendAppointmentBtn).setOnClickListener(v -> {
            String date = dateSelection.getText().toString();
            String time = timeSlotSelector.getText().toString();
            String lawyerId = clientCase.getLawyerID();
            String clientId = currentUser.getUserId();
            String msg = messageBox.getText().toString();
            String appointmentId = lawyerId + clientId + System.currentTimeMillis();

            SimpleDateFormat dateFormat = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US);
            try {
                Date d = dateFormat.parse(date + " " + time);
                String df = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US).format(d);
                Appointment appointment = new Appointment(appointmentId, lawyerId, clientId, df, "Meeting for case: " + clientCase.getCaseTitle() + " - " + msg);
                FirebaseHelper.sendAppointmentRequest(appointment, new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onAppointmentError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAppointSendComplete() {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Appointment Request Sent Successfully", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid date/time format", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void showCaseDetailsDialog(ClientCase clientCase) {
        Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.details_case_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ((TextView) dialog.findViewById(R.id.caseTitle)).setText(clientCase.getCaseTitle());
        Button viewProgressButton = dialog.findViewById(R.id.caseProgress); // Changed to Button
        viewProgressButton.setText("View Progress");
        viewProgressButton.setOnClickListener(view -> {
            Dialog caseProgressDialog = new Dialog(getContext());
            RecyclerView recyclerView = new RecyclerView(getContext());
            caseProgressDialog.setContentView(recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            caseProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ArrayList<Comment> comments = clientCase.getCaseProgressComment();
            CommentAdapter adapter = new CommentAdapter(getContext(), comments);
            recyclerView.setAdapter(adapter);
            caseProgressDialog.show();
        });
        ((TextView) dialog.findViewById(R.id.caseBudget)).setText(String.valueOf(clientCase.getCaseBudget()));
        ((TextView) dialog.findViewById(R.id.caseStatus)).setText(clientCase.getCaseStatus());
        ((TextView) dialog.findViewById(R.id.caseFeedback)).setText(clientCase.getClientFeedBack());
        ((TextView) dialog.findViewById(R.id.lawyerComment)).setText(clientCase.getLawyerComment());
        ((TextView) dialog.findViewById(R.id.clientID)).setText(clientCase.getClientID());
        ((TextView) dialog.findViewById(R.id.caseDetails)).setText(clientCase.getCaseDetails());
        dialog.findViewById(R.id.updateCaseProgressButton).setVisibility(View.GONE);
        dialog.show();
    }

    private void showDatePicker(TextView dateTextView) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pick Date")
                .build();
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date d = new Date(selection);
            String date = new SimpleDateFormat("EEEE dd-MMMM-yyyy", Locale.US).format(d);
            dateTextView.setText(date);
        });
    }

    private void showTimePicker(TextView timeTextView) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();
        timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        timePicker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.US, "%02d:%02d %s",
                    timePicker.getHour() % 12 == 0 ? 12 : timePicker.getHour() % 12,
                    timePicker.getMinute(),
                    timePicker.getHour() >= 12 ? "PM" : "AM");
            timeTextView.setText(time);
        });
    }

    public void showNotification(String message, String TYPE) {
        Log.d("ClientHome", "showNotification called with message: " + message + ", type: " + TYPE);
        title = TYPE;
        if (TYPE.equals("meeting")) {
            getActivity().runOnUiThread(() -> {
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.VISIBLE);
                msg = message;
                Log.d("ClientHome", "Notification set, msg = " + msg);
            });
        } else if (TYPE.equals(ClientCase.CASE_PROGRESS)) {
            getActivity().runOnUiThread(() -> {
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.VISIBLE);
                msg = message;
                Log.d("ClientHome", "Case progress set, msg = " + msg);
            });
        }
    }

    public void showMeetingConfirmationDialog(String meetingID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
        dialog.setTitle("Meeting");
        dialog.setMessage("Join Meeting?");
        dialog.setPositiveButton("Yes", (dialogInterface, i) -> joinMeeting(meetingID));
        dialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandel = callbacks;
    }

    private void openDrawer() {
        ((DrawerLayout) rootView.findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
    }

    private void initHomeScreen() {
        currentUser = (Client) User.getCurrentLoggedInUser();
        navView = rootView.findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);
        ((TextView) rootView.findViewById(R.id.lawyer_profile_name)).setText(currentUser.getFullName());
        ((TextView) rootView.findViewById(R.id.user_last_name)).setText(currentUser.getLastName());
        ((TextView) header.findViewById(R.id.drawer_user_namme)).setText(currentUser.getFullName());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.browserLawyers) {
            LawyerPickerDialog dialog = new LawyerPickerDialog(rootView.getContext());
            dialog.show();
            ((DrawerLayout) rootView.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        } else if (item.getItemId() == R.id.my_appointments) {
            MyAppointments appointments = new MyAppointments(rootView.getContext());
            appointments.show();
        } else if (item.getItemId() == R.id.my_cases_progress) {
            callBackHandel.openFragment(new MyCases(), MyCases.class.getName());
        } else if (item.getItemId() == R.id.signout) {
            callBackHandel.signOut();
        }
        return true;
    }

    // Adapter for displaying cases
    private class CasesAdapter extends RecyclerView.Adapter<CasesAdapter.CaseHolder> {
        private ArrayList<ClientCase> caseArrayList;

        public CasesAdapter(ArrayList<ClientCase> caseArrayList) {
            this.caseArrayList = caseArrayList;
        }

        @NonNull
        @Override
        public CaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.client_case_item, parent, false);
            return new CaseHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CaseHolder holder, int position) {
            ClientCase clientCase = caseArrayList.get(position);
            holder.caseTitle.setText(clientCase.getCaseTitle());
            holder.lawyerId.setText(clientCase.getLawyerID());
            holder.caseStatus.setText(clientCase.getCaseStatus());
            holder.itemView.setOnClickListener(v -> showCaseDetailsDialog(clientCase)); // Click case to show details
            holder.bookMeetingButton.setOnClickListener(v -> showAppointmentBookingDialog(clientCase));
        }

        @Override
        public int getItemCount() {
            return caseArrayList.size();
        }

        class CaseHolder extends RecyclerView.ViewHolder {
            TextView caseTitle, lawyerId, caseStatus;
            Button bookMeetingButton;

            public CaseHolder(@NonNull View itemView) {
                super(itemView);
                caseTitle = itemView.findViewById(R.id.case_title);
                lawyerId = itemView.findViewById(R.id.lawyer_id);
                caseStatus = itemView.findViewById(R.id.case_status);
                bookMeetingButton = itemView.findViewById(R.id.book_meeting_button);
            }
        }
    }
}
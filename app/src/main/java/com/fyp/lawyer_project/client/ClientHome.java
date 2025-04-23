package com.fyp.lawyer_project.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.fyp.lawyer_project.utils.Utilities;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
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

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClientHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener, LawyerPickerDialog.ListLoader {
    private View rootView;
    private Client currentUser;
    private NavigationView navView;
    private MainFragmentActivity callBackHandel;
    private String msg = null;
    private String title = null;
    private ProgressBar progressBar;
    private TextView emptyView;
    private BadgeDrawable notificationBadge;

    // PayPal Configuration
    private static final String PAYPAL_CLIENT_ID = "YOUR_SANDBOX_CLIENT_ID"; // Replace with your PayPal Sandbox Client ID
    private static final int PAYPAL_REQUEST_CODE = 7171;
    private static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PAYPAL_CLIENT_ID);

    private Appointment pendingAppointment;
    private BottomSheetDialog bookingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.client_home, container, false);
        initViews();
        initRecyclerView();
        initClickListener();
        if (!initHomeScreen()) {
            return rootView; // User is null, redirect handled in initHomeScreen
        }
        loadCurrentCases();
        // Start PayPal service
        Intent intent = new Intent(getContext(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        getContext().startService(intent);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop PayPal service
        getContext().stopService(new Intent(getContext(), PayPalService.class));
    }

    private void initViews() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        emptyView = rootView.findViewById(R.id.empty_view);
    }

    private void initRecyclerView() {
        RecyclerView casesRecyclerView = rootView.findViewById(R.id.lawyersList);
        casesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize adapter with empty list to avoid "No adapter attached" error
        casesRecyclerView.setAdapter(new CasesAdapter(new ArrayList<>()));
    }

    private void initClickListener() {
        if (getArguments() != null) {
            String meetingId = getArguments().getString("ID");
            showMeetingConfirmationDialog(meetingId);
        }

        // Toolbar actions
        ImageView drawerButton = rootView.findViewById(R.id.drawer_btn);
        ImageView notificationsButton = rootView.findViewById(R.id.action_notifications);

        if (drawerButton != null) {
            drawerButton.setOnClickListener(v -> openDrawer());
        } else {
            Log.e("ClientHome", "Drawer button ImageView is null");
        }

        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                openMessageDialog();
                if (notificationBadge != null) {
                    notificationBadge.setVisible(false);
                }
            });
        } else {
            Log.e("ClientHome", "Notifications button ImageView is null");
        }
    }

    private void loadCurrentCases() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_LONG).show();
            callBackHandel.signOut();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        RecyclerView casesRecyclerView = rootView.findViewById(R.id.lawyersList);
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));
        FirebaseHelper.loadCasesByUser(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {
                progressBar.setVisibility(View.GONE);
                if (caseArrayList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    casesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    casesRecyclerView.setVisibility(View.VISIBLE);
                    CasesAdapter adapter = new CasesAdapter(caseArrayList);
                    casesRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error loading cases: " + error);
                Toast.makeText(getContext(), "Failed to load cases: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMessageDialog() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_LONG).show();
            callBackHandel.signOut();
            return;
        }
        Log.d("ClientHome", "openMessageDialog called, initial msg = " + msg + ", title = " + title);
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.meeting_confirmation_dialog);
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        MaterialButton joinButton = dialog.findViewById(R.id.join_button);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_button);

        dialogTitle.setText("Notifications");
        dialogMessage.setText("Loading...");

        FirebaseHelper.loadAppointmentRecord(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {
                StringBuilder message = new StringBuilder();
                Appointment activeMeeting = null;
                for (Appointment appt : appointments) {
                    if (appt.getAppointmentStatus().equals(Appointment.STATUS_ACCEPTED)) {
                        if (activeMeeting == null || appt.getAppointmentDate().compareTo(activeMeeting.getAppointmentDate()) > 0) {
                            activeMeeting = appt;
                        }
                    }
                    String status = appt.getAppointmentStatus();
                    if (status.equals(Appointment.STATUS_ACCEPTED)) {
                        message.append("Appointment on ").append(appt.getAppointmentDate()).append(": Accepted\n");
                    } else if (status.equals(Appointment.STATUS_REJECTED)) {
                        message.append("Appointment on ").append(appt.getAppointmentDate()).append(": Rejected\n");
                    }
                }

                Appointment finalActiveMeeting = activeMeeting;
                FirebaseHelper.loadCasesByUser(userId, new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {
                        for (ClientCase c : caseArrayList) {
                            String status = c.getCaseStatus();
                            if (status.equals("Active")) {
                                message.append("Case '").append(c.getCaseTitle()).append("': Accepted\n");
                            } else if (status.equals("Rejected")) {
                                message.append("Case '").append(c.getCaseTitle()).append("': Rejected\n");
                            }
                        }

                        String roomName = finalActiveMeeting != null ? finalActiveMeeting.getAppointmentId() : null;
                        if (roomName != null) {
                            message.append("Active Meeting: ").append(roomName).append("\n");
                        }
                        if (msg != null && title != null) {
                            message.append("Notification: ").append(msg).append("\n");
                        }
                        if (message.length() == 0) {
                            message.append("No notifications");
                        }

                        dialogMessage.setText(message.toString());
                        if (roomName != null) {
                            joinButton.setVisibility(View.VISIBLE);
                            joinButton.setOnClickListener(v -> {
                                joinMeeting(roomName);
                                dialog.dismiss();
                            });
                        } else {
                            joinButton.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_LONG).show();
            callBackHandel.signOut();
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.appointment_booking_dialog);
        dialog.show();

        TextInputEditText dateSelection = dialog.findViewById(R.id.date_selection);
        TextInputEditText timeSlotSelector = dialog.findViewById(R.id.timeSlotSelector);
        TextInputEditText messageBox = dialog.findViewById(R.id.messageBox);
        MaterialButton sendAppointmentBtn = dialog.findViewById(R.id.sendAppointmentBtn);

        dateSelection.setOnClickListener(v -> showDatePicker(dateSelection));
        timeSlotSelector.setOnClickListener(v -> showTimePicker(timeSlotSelector));
        sendAppointmentBtn.setOnClickListener(v -> {
            String date = dateSelection.getText().toString();
            String time = timeSlotSelector.getText().toString();
            String lawyerId = clientCase.getLawyerID();
            String clientId = currentUser.getUserId();
            String msg = messageBox.getText().toString();
            String appointmentId = lawyerId + clientId + System.currentTimeMillis();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(getContext(), "Please select date and time", Toast.LENGTH_LONG).show();
                return;
            }

            ProgressBar paymentProgress = dialog.findViewById(R.id.payment_progress);
            paymentProgress.setVisibility(View.VISIBLE);
            sendAppointmentBtn.setEnabled(false);

            SimpleDateFormat dateFormat = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US);
            try {
                Date d = dateFormat.parse(date + " " + time);
                String df = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US).format(d);
                Appointment appointment = new Appointment(appointmentId, lawyerId, clientId, df, "Meeting for case: " + clientCase.getCaseTitle() + " - " + msg);

                pendingAppointment = appointment;
                bookingDialog = dialog;

                PayPalPayment payment = new PayPalPayment(
                        new java.math.BigDecimal("50.00"),
                        "USD",
                        "Lawyer Appointment Booking",
                        PayPalPayment.PAYMENT_INTENT_SALE
                );
                Intent intent = new Intent(getContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
                startActivityForResult(intent, PAYPAL_REQUEST_CODE);
            } catch (ParseException e) {
                paymentProgress.setVisibility(View.GONE);
                sendAppointmentBtn.setEnabled(true);
                Toast.makeText(getContext(), "Invalid date/time format", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ProgressBar paymentProgress = bookingDialog.findViewById(R.id.payment_progress);
        MaterialButton sendAppointmentBtn = bookingDialog.findViewById(R.id.sendAppointmentBtn);
        paymentProgress.setVisibility(View.GONE);
        sendAppointmentBtn.setEnabled(true);

        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        FirebaseHelper.sendAppointmentRequest(pendingAppointment, new FirebaseHelper.FirebaseActions() {
                            @Override
                            public void onAppointmentError(String error) {
                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onAppointSendComplete() {
                                FirebaseHelper.getUserToken(pendingAppointment.getLawyerID(), new FirebaseHelper.FirebaseActions() {
                                    @Override
                                    public void onUserTokenLoaded(String token) {
                                        Utilities.sendFCMPush(getContext(), token, "New Appointment Request",
                                                "New appointment request from " + pendingAppointment.getClientID() + " on " + pendingAppointment.getAppointmentDate());
                                    }
                                });
                                bookingDialog.dismiss();
                                Toast.makeText(getContext(), "Appointment Request Sent Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Payment processing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Payment confirmation failed", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Payment canceled", Toast.LENGTH_LONG).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(getContext(), "Invalid payment configuration", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showCaseDetailsDialog(ClientCase clientCase) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.details_case_dialog);

        ((TextView) dialog.findViewById(R.id.caseTitle)).setText(clientCase.getCaseTitle());
        ((TextView) dialog.findViewById(R.id.caseDetails)).setText(clientCase.getCaseDetails());
        ((TextView) dialog.findViewById(R.id.caseBudget)).setText(String.valueOf(clientCase.getCaseBudget()));
        ((TextView) dialog.findViewById(R.id.caseStatus)).setText(clientCase.getCaseStatus());
        ((TextView) dialog.findViewById(R.id.clientID)).setText(clientCase.getClientID());
        ((TextView) dialog.findViewById(R.id.lawyerComment)).setText(clientCase.getLawyerComment());
        ((TextView) dialog.findViewById(R.id.caseFeedback)).setText(clientCase.getClientFeedBack());
        dialog.findViewById(R.id.updateCaseProgressButton).setVisibility(View.GONE);

        MaterialButton viewProgressButton = dialog.findViewById(R.id.caseProgress);
        viewProgressButton.setOnClickListener(view -> {
            BottomSheetDialog caseProgressDialog = new BottomSheetDialog(getContext());
            RecyclerView recyclerView = new RecyclerView(getContext());
            caseProgressDialog.setContentView(recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            caseProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ArrayList<Comment> comments = clientCase.getCaseProgressComment();
            CommentAdapter adapter = new CommentAdapter(getContext(), comments);
            recyclerView.setAdapter(adapter);
            caseProgressDialog.show();
        });

        dialog.show();
    }

    private void showDatePicker(TextInputEditText dateTextView) {
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

    private void showTimePicker(TextInputEditText timeTextView) {
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

    public void showNotification(String message, String type) {
        Log.d("ClientHome", "showNotification called with message: " + message + ", type: " + type);
        title = type;
        msg = message;
        getActivity().runOnUiThread(() -> {
            if (notificationBadge == null) {
                notificationBadge = BadgeDrawable.create(getContext());
                notificationBadge.setNumber(1);
            }
            notificationBadge.setVisible(true);
            notificationBadge.setNumber(notificationBadge.getNumber() + 1);
            if (type.equals("Case Progress") || type.equals("Appointment Accepted") || type.equals("Appointment Rejected") ||
                    type.equals("Case Accepted") || type.equals("Case Rejected")) {
                loadCurrentCases();
            }
        });
    }

    public void showMeetingConfirmationDialog(String meetingID) {
        if (meetingID == null || meetingID.isEmpty()) {
            Toast.makeText(getContext(), "Invalid meeting ID", Toast.LENGTH_LONG).show();
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.meeting_confirmation_dialog);
        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        MaterialButton joinButton = dialog.findViewById(R.id.join_button);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_button);

        dialogMessage.setText("Join Meeting ID: " + meetingID);
        joinButton.setOnClickListener(v -> {
            joinMeeting(meetingID);
            dialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandel = callbacks;
    }

    private void openDrawer() {
        ((DrawerLayout) rootView.findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
    }

    private boolean initHomeScreen() {
        User user = User.getCurrentLoggedInUser();
        if (user == null || !(user instanceof Client)) {
            Log.e("ClientHome", "initHomeScreen: User is null or not a Client");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_LONG).show();
            if (callBackHandel != null) {
                callBackHandel.signOut();
            }
            return false;
        }
        currentUser = (Client) user;
        navView = rootView.findViewById(R.id.navView);
        if (navView == null) {
            Log.e("ClientHome", "initHomeScreen: NavigationView is null");
            Toast.makeText(getContext(), "Navigation drawer not found", Toast.LENGTH_LONG).show();
            return false;
        }
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);
        if (header == null) {
            Log.e("ClientHome", "initHomeScreen: Navigation header is null");
            Toast.makeText(getContext(), "Navigation header not found", Toast.LENGTH_LONG).show();
            return false;
        }
        TextView nameView = header.findViewById(R.id.drawer_user_name);
        TextView emailView = header.findViewById(R.id.drawer_user_email);
        CircleImageView avatarView = header.findViewById(R.id.drawer_user_avatar);
        if (nameView != null) {
            nameView.setText(currentUser.getFullName());
        } else {
            Log.e("ClientHome", "initHomeScreen: drawer_user_name TextView is null");
        }
        if (emailView != null) {
            emailView.setText(currentUser.getEmailAddress());
        } else {
            Log.e("ClientHome", "initHomeScreen: drawer_user_email TextView is null");
        }
        if (avatarView != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
                    .placeholder(R.drawable.lawyer)
                    .error(R.drawable.lawyer)
                    .into(avatarView);
        }
        // Load profile image into top bar
        CircleImageView topBarImage = rootView.findViewById(R.id.userProfileImage);
        if (topBarImage != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
                    .placeholder(R.drawable.lawyer)
                    .error(R.drawable.lawyer)
                    .into(topBarImage);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.browserLawyers) {
            LawyerPickerDialog dialog = new LawyerPickerDialog(rootView.getContext());
            dialog.setActionCallBack(this);
            dialog.show();
        } else if (item.getItemId() == R.id.my_appointments) {
            MyAppointments appointments = new MyAppointments(rootView.getContext());
            appointments.show();
        } else if (item.getItemId() == R.id.my_cases_progress) {
            callBackHandel.openFragment(new MyCases(), MyCases.class.getName());
        } else if (item.getItemId() == R.id.edit_profile) {
            getFragmentManager().beginTransaction()
                    .addToBackStack(ClientProfile.class.getName())
                    .replace(R.id.container, new ClientProfile())
                    .commit();
        } else if (item.getItemId() == R.id.signout) {
            callBackHandel.signOut();
        }
        ((DrawerLayout) rootView.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLawyersListLoaded(ArrayList<User> lawyers) {
        if (lawyers == null || lawyers.isEmpty()) {
            Toast.makeText(getContext(), "No lawyers found", Toast.LENGTH_LONG).show();
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.lawyer_list_dialog);
        RecyclerView recyclerView = dialog.findViewById(R.id.lawyer_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LawyerAdapter adapter = new LawyerAdapter(lawyers);
        adapter.setLawyerListDialog(dialog);
        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    private void showCreateCaseDialog(User lawyer, BottomSheetDialog lawyerListDialog) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.create_case_dialog);
        dialog.show();

        TextInputEditText caseTitleField = dialog.findViewById(R.id.case_title_field);
        TextInputEditText caseDetailsField = dialog.findViewById(R.id.case_details_field);
        TextInputEditText caseBudgetField = dialog.findViewById(R.id.case_budget_field);
        MaterialButton submitButton = dialog.findViewById(R.id.submit_case_button);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_case_button);

        submitButton.setOnClickListener(v -> {
            String caseTitle = caseTitleField.getText().toString().trim();
            String caseDetails = caseDetailsField.getText().toString().trim();
            String caseBudgetStr = caseBudgetField.getText().toString().trim();

            if (caseTitle.isEmpty()) {
                caseTitleField.setError("Case title is required");
                caseTitleField.requestFocus();
                return;
            }
            if (caseDetails.isEmpty()) {
                caseDetailsField.setError("Case details are required");
                caseDetailsField.requestFocus();
                return;
            }
            if (caseBudgetStr.isEmpty()) {
                caseBudgetField.setError("Budget is required");
                caseBudgetField.requestFocus();
                return;
            }

            double caseBudget;
            try {
                caseBudget = Double.parseDouble(caseBudgetStr);
            } catch (NumberFormatException e) {
                caseBudgetField.setError("Invalid budget format");
                caseBudgetField.requestFocus();
                return;
            }

            String caseId = "CASE_" + currentUser.getUserId() + "_" + System.currentTimeMillis();
            String lawyerId = lawyer.getUserId();
            String clientId = currentUser.getUserId();
            ClientCase clientCase = new ClientCase(caseId, caseTitle, caseDetails, caseBudget, lawyerId, clientId, ClientCase.NOT_ACCEPTED);

            FirebaseHelper.saveCase(clientCase, new FirebaseHelper.FirebaseActions() {
                @Override
                public void onCaseSaved() {
                    FirebaseHelper.getUserToken(lawyerId, new FirebaseHelper.FirebaseActions() {
                        @Override
                        public void onUserTokenLoaded(String token) {
                            Utilities.sendFCMPush(getContext(), token, "New Case Request",
                                    "New case '" + caseTitle + "' from " + clientId);
                        }
                    });
                    Toast.makeText(getContext(), "Case created successfully", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    lawyerListDialog.dismiss();
                    loadCurrentCases();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error creating case: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

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
            holder.lawyerId.setText("Lawyer ID: " + clientCase.getLawyerID());
            holder.caseStatus.setText("Status: " + clientCase.getCaseStatus());
            holder.viewDetailsButton.setOnClickListener(v -> showCaseDetailsDialog(clientCase));
            holder.bookMeetingButton.setOnClickListener(v -> showAppointmentBookingDialog(clientCase));
        }

        @Override
        public int getItemCount() {
            return caseArrayList.size();
        }

        class CaseHolder extends RecyclerView.ViewHolder {
            TextView caseTitle, lawyerId, caseStatus;
            MaterialButton viewDetailsButton, bookMeetingButton;

            public CaseHolder(@NonNull View itemView) {
                super(itemView);
                caseTitle = itemView.findViewById(R.id.case_title);
                lawyerId = itemView.findViewById(R.id.lawyer_id);
                caseStatus = itemView.findViewById(R.id.case_status);
                viewDetailsButton = itemView.findViewById(R.id.view_details_button);
                bookMeetingButton = itemView.findViewById(R.id.book_meeting_button);
            }
        }
    }

    private class LawyerAdapter extends RecyclerView.Adapter<LawyerAdapter.LawyerHolder> {
        private ArrayList<User> lawyers;
        private BottomSheetDialog lawyerListDialog;

        public LawyerAdapter(ArrayList<User> lawyers) {
            this.lawyers = lawyers;
            this.lawyerListDialog = null;
        }

        public void setLawyerListDialog(BottomSheetDialog dialog) {
            this.lawyerListDialog = dialog;
        }

        @NonNull
        @Override
        public LawyerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.lawyer_item, parent, false);
            return new LawyerHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LawyerHolder holder, int position) {
            User lawyer = lawyers.get(position);
            holder.lawyerName.setText(lawyer.getFullName());
            holder.lawyerEmail.setText(lawyer.getEmailAddress());
            holder.selectButton.setOnClickListener(v -> {
                if (lawyerListDialog == null) {
                    Log.e("ClientHome", "LawyerListDialog is null in LawyerAdapter");
                    Toast.makeText(getContext(), "Error: Unable to create case", Toast.LENGTH_LONG).show();
                    return;
                }
                showCreateCaseDialog(lawyer, lawyerListDialog);
            });
        }

        @Override
        public int getItemCount() {
            return lawyers.size();
        }

        class LawyerHolder extends RecyclerView.ViewHolder {
            TextView lawyerName, lawyerEmail;
            MaterialButton selectButton;

            public LawyerHolder(@NonNull View itemView) {
                super(itemView);
                lawyerName = itemView.findViewById(R.id.lawyer_name);
                lawyerEmail = itemView.findViewById(R.id.lawyer_email);
                selectButton = itemView.findViewById(R.id.select_lawyer_button);
            }
        }
    }
}
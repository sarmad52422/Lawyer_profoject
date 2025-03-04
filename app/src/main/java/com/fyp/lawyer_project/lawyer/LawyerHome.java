package com.fyp.lawyer_project.lawyer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.common.CaseRequestsDialog;
import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragment;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LawyerHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener, AppointmentRequestAdapter.AppointmentListCallBack {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    private Lawyer currentUser;
    private NavigationView navView;
    private String clientId = "";
    private AppointmentRequestAdapter requestAdapter;
    private UpcomingAppointmentAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.lawyer_home, container, false);
        initClickActions();
        initHomeScreen();
        loadAppointmentRequest();
        return rootView;
    }

    @Override
    public void onResume() {
        initHomeScreen();
        refreshFragment(); // Refresh on resume to ensure latest data
        super.onResume();
    }

    private void initClickActions() {
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view -> openDrawer());
        rootView.findViewById(R.id.refreshIcon).setOnClickListener(view -> refreshFragment());
    }

    private void refreshFragment() {
        Log.d("LawyerHome", "Refreshing appointment lists");
        loadAppointmentRequest(); // Reload data from Firebase
    }

    private void openDrawer() {
        ((DrawerLayout) rootView.findViewById(R.id.drawer_layout_lawyer)).openDrawer(GravityCompat.START);
    }

    private void initHomeScreen() {
        currentUser = (Lawyer) User.getCurrentLoggedInUser();
        navView = rootView.findViewById(R.id.navViewLawyer);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);
        ((TextView) rootView.findViewById(R.id.lawyer_profile_name)).setText(currentUser.getFullName());
        ((TextView) rootView.findViewById(R.id.user_last_name)).setText(currentUser.getLastName());
        ((TextView) header.findViewById(R.id.drawer_user_namme)).setText(currentUser.getFullName());
    }

    private void loadAppointmentRequest() {
        rootView.findViewById(R.id.requestLoadingProgress).setVisibility(View.VISIBLE);
        RecyclerView appoinmentRequestList = rootView.findViewById(R.id.appointment_request_list);
        RecyclerView upComingAppointments = rootView.findViewById(R.id.upcomingAppointmentList);
        appoinmentRequestList.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        upComingAppointments.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        ArrayList<Appointment> notAcceptedAppointments = new ArrayList<>();
        ArrayList<Appointment> acceptedAppointments = new ArrayList<>();
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));

        FirebaseHelper.loadAppointmentRequests(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {
                notAcceptedAppointments.clear(); // Clear existing data
                acceptedAppointments.clear();
                for (Appointment appointment : appointments) {
                    if (appointment.getAppointmentStatus().equals(Appointment.STATUS_NOT_ACCEPTED)) {
                        notAcceptedAppointments.add(appointment);
                    } else if (appointment.getAppointmentStatus().equals(Appointment.STATUS_ACCEPTED)) {
                        acceptedAppointments.add(appointment);
                    }
                }

                // Initialize or update adapters
                if (requestAdapter == null) {
                    requestAdapter = new AppointmentRequestAdapter(notAcceptedAppointments, rootView.getContext());
                    requestAdapter.setOnItemCallBack(LawyerHome.this);
                    appoinmentRequestList.setAdapter(requestAdapter);
                } else {
                    requestAdapter.updateAppointments(notAcceptedAppointments);
                }

                if (upcomingAdapter == null) {
                    upcomingAdapter = new UpcomingAppointmentAdapter(rootView.getContext(), acceptedAppointments);
                    upcomingAdapter.setOnItemClickListener(LawyerHome.this::onAppointmentItemClicked);
                    upComingAppointments.setAdapter(upcomingAdapter);
                } else {
                    upcomingAdapter.updateAppointments(acceptedAppointments);
                }

                rootView.findViewById(R.id.requestLoadingProgress).setVisibility(View.GONE);
                rootView.findViewById(R.id.appointmentLoadingProgress).setVisibility(View.GONE);
            }
        });
    }

    public void onAppointmentItemClicked(Appointment appointment) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
        dialog.setTitle("Meeting");
        dialog.setMessage("Start Meeting?");
        dialog.setPositiveButton("Start", (dialogInterface, i) -> startMeeting(appointment));
        dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }

    private void startMeeting(Appointment appointment) {
        this.clientId = appointment.getClientID();
        try {
            String roomName = "LawyerClientMeeting_" + clientId + "_" + System.currentTimeMillis();
            String compositeKey = appointment.getLawyerID() + clientId;

            FirebaseDatabase.getInstance().getReference(FirebaseHelper.APPOINTMENT_TABLE)
                    .child(compositeKey)
                    .child("appointmentId")
                    .setValue(roomName)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("LawyerHome", "Room name updated under Appointments/" + compositeKey + "/appointmentId: " + roomName);
                        JitsiMeetConferenceOptions options = null;
                        try {
                            options = new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(new URL("https://meet.jit.si"))
                                    .setRoom(roomName)
                                    .setAudioMuted(false)
                                    .setVideoMuted(false)
                                    .setFeatureFlag("lobby.enabled", false)
                                    .setFeatureFlag("prejoinpage.enabled", false)
                                    .build();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        JitsiMeetActivity.launch(getContext(), options);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            FirebaseHelper.sendMeetingNotification(rootView.getContext(), roomName, "", clientId);
                            Log.d("LawyerHome", "Notification sent to client: " + roomName);
                        }, 3000);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase Error", "Failed to update room name: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to save meeting details", Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Log.e("Jitsi Error", e.getMessage());
            Toast.makeText(getContext(), "Failed to start meeting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setCallBackAction(MainFragmentActivity fragmentActivity) {
        this.callBackHandler = fragmentActivity;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.my_profile_btn) {
            getFragmentManager().beginTransaction().addToBackStack(LawyerProfile.class.getName()).replace(R.id.container, new LawyerProfile()).commit();
        } else if (item.getItemId() == R.id.mng_schedules) {
            getFragmentManager().beginTransaction().addToBackStack(LawyerScheduleManager.class.getName()).replace(R.id.container, new LawyerScheduleManager()).commit();
        } else if (item.getItemId() == R.id.signout) {
            FirebaseAuth.getInstance().signOut();
            callBackHandler.signOut();
        } else if (item.getItemId() == R.id.case_req_btn) {
            CaseRequestsDialog dialog = new CaseRequestsDialog(getContext(), currentUser.getUserId());
            dialog.show();
        } else if (item.getItemId() == R.id.my_clients_btn) {
            callBackHandler.openFragment(new MyClientsFragment(), MyClientsFragment.class.getName());
        }
        return true;
    }

    @Override
    public void onAppointmentAccepted(Appointment appointment) {
        RecyclerView appoinmentRequestList = rootView.findViewById(R.id.appointment_request_list);
        RecyclerView upComingAppointments = rootView.findViewById(R.id.upcomingAppointmentList);
        AppointmentRequestAdapter adapter = (AppointmentRequestAdapter) appoinmentRequestList.getAdapter();
        UpcomingAppointmentAdapter upAdapter = (UpcomingAppointmentAdapter) upComingAppointments.getAdapter();
        adapter.removeItem(appointment);
        upAdapter.addItem(appointment);
        FirebaseHelper.updateAppointmentStatus(appointment.getAppointmentId(), Appointment.STATUS_ACCEPTED);
    }

    @Override
    public void onAppointmentRejected(Appointment appointment) {
        RecyclerView appoinmentRequestList = rootView.findViewById(R.id.appointment_request_list);
        AppointmentRequestAdapter adapter = (AppointmentRequestAdapter) appoinmentRequestList.getAdapter();
        FirebaseHelper.updateAppointmentStatus(appointment.getAppointmentId(), Appointment.STATUS_REJECTED);
        adapter.removeItem(appointment); // Remove from UI
        Toast.makeText(getContext(), "Appointment rejected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserImageClicked(String clientID) {}
}
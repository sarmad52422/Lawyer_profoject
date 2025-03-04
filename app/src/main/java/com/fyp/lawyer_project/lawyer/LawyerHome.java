package com.fyp.lawyer_project.lawyer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.fyp.lawyer_project.utils.Utilities;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.ArrayList;

public class LawyerHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener, AppointmentRequestAdapter.AppointmentListCallBack {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    private Lawyer currentUser;
    private NavigationView navView;
    private String clientId = "";

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
        super.onResume();
    }

    private void initClickActions() {
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view -> openDrawer());
        rootView.findViewById(R.id.refreshIcon).setOnClickListener(view -> refreshFragment());
    }

    private void refreshFragment() {
        loadAppointmentRequest();
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
        upComingAppointments.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        appoinmentRequestList.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        ArrayList<Appointment> notAcceptedAppointments = new ArrayList<>();
        ArrayList<Appointment> acceptedAppointments = new ArrayList<>();
        String userId = "_" + currentUser.getEmailAddress().substring(0, currentUser.getEmailAddress().indexOf("@"));
        FirebaseHelper.loadAppointmentRequests(userId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {
                for (Appointment appointment : appointments) {
                    if (appointment.getAppointmentStatus().equals(Appointment.STATUS_NOT_ACCEPTED)) {
                        notAcceptedAppointments.add(appointment);
                    } else if (appointment.getAppointmentStatus().equals(Appointment.STATUS_ACCEPTED)) {
                        acceptedAppointments.add(appointment);
                    }
                }
                AppointmentRequestAdapter adapter = new AppointmentRequestAdapter(notAcceptedAppointments, rootView.getContext());
                UpcomingAppointmentAdapter upcomingAppointmentAdapter = new UpcomingAppointmentAdapter(rootView.getContext(), acceptedAppointments);
                adapter.setOnItemCallBack(LawyerHome.this);
                upcomingAppointmentAdapter.setOnItemClickListener(LawyerHome.this::onAppointmentItemClicked);
                appoinmentRequestList.setAdapter(adapter);
                upComingAppointments.setAdapter(upcomingAppointmentAdapter);

                rootView.findViewById(R.id.requestLoadingProgress).setVisibility(View.GONE);
                rootView.findViewById(R.id.appointmentLoadingProgress).setVisibility(View.GONE);
            }
        });
    }

    public void onAppointmentItemClicked(Appointment appointment) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
        dialog.setTitle("Meeting");
        dialog.setMessage("Start Meeting?");
        dialog.setPositiveButton("Yes", (dialogInterface, i) -> startMeeting(appointment.getClientID()));
        dialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }

    private void startMeeting(String clientId) {
        this.clientId = clientId;

        try {
            // Generate a unique room name using clientId and a timestamp
            String roomName = "LawyerClientMeeting_" + clientId + "_" + System.currentTimeMillis();

            // Configure Jitsi options
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si")) // Public server
                    .setRoom(roomName) // Unique room for this meeting
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            // Launch the meeting
            JitsiMeetActivity.launch(getContext(), options);

            // Send meeting notification to client via Firebase
            FirebaseHelper.sendMeetingNotification(rootView.getContext(), roomName, roomName, clientId);
        } catch (Exception e) {
            Log.e("Jitsi Error", e.getMessage());
            Toast.makeText(getContext(), "Failed to start meeting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        // Remove Zoom login/logout item since Jitsi doesn't need authentication

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
        // Removed Zoom login/logout logic as Jitsi doesn't require it
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
    }

    @Override
    public void onAppointmentRejected(Appointment appointment) {
        // No changes needed here
    }

    @Override
    public void onUserImageClicked(String clientID) {
        // No changes needed here
    }
}
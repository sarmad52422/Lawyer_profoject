package com.fyp.lawyer_project.lawyer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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

import java.util.ArrayList;

import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.ZoomApiError;
import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;

public class LawyerHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener, AppointmentRequestAdapter.AppointmentListCallBack,MeetingServiceListener {
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

    private void initClickActions() {
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view -> openDrawer());
        rootView.findViewById(R.id.refreshIcon).setOnClickListener(view->refreshFragment());
    }
    private void refreshFragment(){
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
    public void onAppointmentItemClicked(Appointment appointment){
        AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
        dialog.setTitle("Meeting");
        dialog.setMessage("Start Meeting?");
        dialog.setPositiveButton("Yes", (dialogInterface, i) ->startMeeting(appointment.getClientID()));
        dialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();

    }
    private void loginZoom(String email,String password) {
        int result = ZoomSDK.getInstance().loginWithZoom("sarmad.52422@gmail.com", "SHIppud3N");
        if (result == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
            ZoomSDK.getInstance().addAuthenticationListener(authenticationListener);
        }
    }

    private final ZoomSDKAuthenticationListener authenticationListener = new ZoomSDKAuthenticationListener() {
        @Override
        public void onZoomSDKLoginResult(long l) {
            if (l == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
                Toast.makeText(rootView.getContext(), "Zoom Login Successful", Toast.LENGTH_LONG).show();

            }
        }

        @Override
        public void onZoomSDKLogoutResult(long l) {

        }

        @Override
        public void onZoomIdentityExpired() {

        }

        @Override
        public void onZoomAuthIdentityExpired() {

        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.loginZoom);
        if(ZoomSDK.getInstance().isLoggedIn())
            item.setTitle("Logout Zoom");
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
        }
        else if(item.getItemId() == R.id.case_req_btn){
            CaseRequestsDialog dialog = new CaseRequestsDialog(getContext(),currentUser.getUserId());
            dialog.show();

        }
        else if(item.getItemId() == R.id.my_clients_btn){
            callBackHandler.openFragment(new MyClientsFragment(),MyClientsFragment.class.getName());
        }
        else if (item.getItemId() == R.id.loginZoom) {
            if(ZoomSDK.getInstance().isLoggedIn())
            {
                ZoomSDK.getInstance().logoutZoom();
            }
            else {
                showLoginWindow();
                item.setTitle("Logout Zoom");
            }
        }
        return true;
    }
    private void showLoginWindow(){
        CustomDialogs.ZoomLoginDialog dialog = new CustomDialogs.ZoomLoginDialog(rootView.getContext(), new CustomDialogs.ZoomLoginDialog.LoginWindowCallBack() {
            @Override
            public void onLoginButtonClicked(String userName, String password) {
                if(!ZoomSDK.getInstance().isLoggedIn())
                    loginZoom(userName,password);

            }
        });
        dialog.show();
    }
    private void startMeeting(String clientId){
        this.clientId = clientId;

        if(ZoomSDK.getInstance().isLoggedIn()) {
            MeetingService meetingService = ZoomSDK.getInstance().getMeetingService();
            StartMeetingOptions options = new StartMeetingOptions();
            meetingService.startInstantMeeting(rootView.getContext(),options);
            meetingService.addListener(this);
        }
        else{
            Toast.makeText(rootView.getContext(),"Please Login Zoom ID first",Toast.LENGTH_LONG).show();
        }

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

    }

    @Override
    public void onUserImageClicked(String clientID) {

    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int i, int i1) {
        if(MeetingStatus.MEETING_STATUS_INMEETING == meetingStatus){
            Log.e("Meeting with ",clientId);
            long meetingId = ZoomSDK.getInstance().getInMeetingService().getCurrentMeetingNumber();
            String meetingPassword = ZoomSDK.getInstance().getInMeetingService().getMeetingPassword();
            FirebaseHelper.sendMeetingNotification(rootView.getContext(),meetingId,meetingPassword,clientId);
        }
    }
}

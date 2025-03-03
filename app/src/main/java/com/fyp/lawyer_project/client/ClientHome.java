package com.fyp.lawyer_project.client;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.ArrayList;

public class ClientHome extends RootFragment implements LawyerPickerDialog.ListLoader, LawyersListAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {
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

        return rootView;
    }

    private void initClickListener() {
        if (getArguments() != null) {
            String meetingId = getArguments().getString("ID");
            String meetingPassword = getArguments().getString("PASSWORD"); // Ignored by Jitsi
            showMeetingConfirmationDialog(meetingId);
        }
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view -> openDrawer());
        rootView.findViewById(R.id.notifyIcon).setOnClickListener(view -> openMessageDialog());
    }

    private void openMessageDialog() {
        if (msg == null) return;

        if (title.equals(ClientCase.CASE_PROGRESS)) {
            AlertDialog.Builder msgDialog = new AlertDialog.Builder(rootView.getContext());
            msgDialog.setTitle("Message Alert");
            msgDialog.setMessage(msg);
            msgDialog.setPositiveButton("View Message", (dialogInterface, i) -> {
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.GONE);
            });
            msgDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            msgDialog.show();
        } else {
            AlertDialog.Builder msgDialog = new AlertDialog.Builder(rootView.getContext());
            msgDialog.setTitle("Message Alert");
            msgDialog.setMessage(msg);
            msgDialog.setPositiveButton("Join Meeting", (dialogInterface, i) -> {
                String[] id_pass = getIdPasswordFromNotificationMessage(msg);
                joinMeeting(id_pass[0]); // Only use meetingId
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.GONE);
            });
            msgDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            msgDialog.show();
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

    private String[] getIdPasswordFromNotificationMessage(String msgValue) {
        String[] id_pass = msgValue.split("\n");
        return new String[]{
                id_pass[0].substring(id_pass[0].lastIndexOf(" ")).trim(),
                id_pass[1].substring(id_pass[1].lastIndexOf(" ")).trim()
        };
    }

    private void joinMeeting(String meetingId) {
        try {
            // Use the meetingId as the room name (or adjust based on LawyerHome's format)
            String roomName = meetingId; // Expecting "LawyerClientMeeting_<clientId>_<timestamp>" from LawyerHome

            // Configure Jitsi options
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si")) // Public server
                    .setRoom(roomName) // Use the provided meetingId as room
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            // Launch the meeting
            JitsiMeetActivity.launch(getContext(), options);
        } catch (Exception e) {
            Log.e("Jitsi Error", e.getMessage());
            Toast.makeText(getContext(), "Failed to join meeting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void showNotification(String message, String TYPE) {
        title = TYPE;
        if (TYPE.equals("meeting")) {
            getActivity().runOnUiThread(() -> {
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.VISIBLE);
                msg = message;
            });
        } else if (TYPE.equals(ClientCase.CASE_PROGRESS)) {
            getActivity().runOnUiThread(() -> {
                rootView.findViewById(R.id.notifyIconAlert).setVisibility(View.VISIBLE);
                msg = message;
            });
        }
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
    public void onLawyersListLoaded(ArrayList<User> lawyers) {
        RecyclerView recyclerView = rootView.findViewById(R.id.lawyersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        LawyersListAdapter adapter = new LawyersListAdapter(rootView.getContext(), lawyers);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClicked(Lawyer lawyer) {
        BottomSheetDialog dialog = new DetailLawyerView(rootView.getContext(), lawyer);
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.browserLawyers) {
            LawyerPickerDialog dialog = new LawyerPickerDialog(rootView.getContext());
            dialog.setActionCallBack(this);
            dialog.show();
            ((DrawerLayout) rootView.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        }
        if (item.getItemId() == R.id.my_appointments) {
            MyAppointments appointments = new MyAppointments(rootView.getContext());
            appointments.show();
        } else if (item.getItemId() == R.id.my_cases_progress) {
            callBackHandel.openFragment(new MyCases(), MyCases.class.getName());
        }
        if (item.getItemId() == R.id.signout) {
            callBackHandel.signOut();
        }
        return true;
    }
}
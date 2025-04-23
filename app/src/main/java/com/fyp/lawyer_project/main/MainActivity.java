package com.fyp.lawyer_project.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.client.ClientHome;
import com.fyp.lawyer_project.lawyer.LawyerHome;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class MainActivity extends MainFragmentActivity {
    private String meetingID = "NOT SET";
    private String meetingPassword = "NOT SET"; // Unused with Jitsi public server
    public static boolean active = false;
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        initMainFragment();
        MaterialToolbar toolbar = findViewById(R.id.simple_title_bar_layout);
        toolbar.setNavigationOnClickListener(v -> goBack());
        mainActivity = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivity = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private String[] getIdPasswordFromNotificationMessage(String msgValue) {
        String[] id_pass = msgValue.split("\n");
        return new String[]{
                id_pass[0].substring(id_pass[0].lastIndexOf(" ")).trim(),
                id_pass[1].substring(id_pass[1].lastIndexOf(" ")).trim()
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Log.e(MainActivity.class.getName(), " Created!!!!!");
            try {
                String[] idPassword = getIdPasswordFromNotificationMessage(getIntent().getExtras().getString("text"));
                meetingID = idPassword[0];
                meetingPassword = idPassword[1]; // Kept for compatibility, unused by Jitsi
                Log.e("Meeting ID", meetingID);
                Log.e("Meeting Password", meetingPassword);
                askForMeetingIfFragmentIsOpen();
                setIntent(null);
            } catch (Exception ignore) {
                Log.e("ERROR = ", ignore.getMessage());
            }
        }
    }

    public void showMeetingNotification(String notificationMessage, String title) {
        RootFragment fragment = (RootFragment) getSupportFragmentManager().findFragmentByTag("CLIENT");
        if (fragment != null && fragment.isVisible()) {
            if (title.equals(ClientCase.CASE_PROGRESS)) {
                ((ClientHome) fragment).showNotification(notificationMessage, title);
            } else {
                ((ClientHome) fragment).showNotification(notificationMessage, "meeting");
            }
        }
    }

    private void askForMeetingIfFragmentIsOpen() {
        RootFragment fragment = (RootFragment) getSupportFragmentManager().findFragmentByTag("CLIENT");
        if (fragment != null && fragment.isVisible()) {
            // Launch Jitsi meeting directly or via ClientHome dialog
            launchJitsiMeeting(meetingID);
        }
    }

    private void launchJitsiMeeting(String meetingId) {
        try {
            // Use meetingID from notification, or fallback to a default
            String roomName = "LawyerClientMeeting_" + (meetingId.equals("NOT SET") ? java.util.UUID.randomUUID().toString() : meetingId);

            // Configure Jitsi options
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si")) // Public server
                    .setRoom(roomName) // Unique room name
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            // Launch the meeting
            JitsiMeetActivity.launch(this, options);
        } catch (Exception e) {
            Log.e("Jitsi Error", e.getMessage());
            Toast.makeText(this, "Failed to start meeting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initMainFragment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseHelper.loadUser(user.getEmail(), new ProgressDialog(this), new FirebaseHelper.FirebaseActions() {
                @Override
                public void onUserLoaded(User user) {
                    if (user == null) {
                        Toast.makeText(getApplicationContext(), "No User Found", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        openLoginFragment();
                    } else {
                        User.setCurrentLoggedInUser(user);
                        if (user.getUserType().equals(User.TYPE_CLIENT)) {
                            openClientFragment();
                        } else if (user.getUserType().equals(User.TYPE_LAWYER)) {
                            openFragment(new LawyerHome(), "LAWYER");
                        }
                        Log.e("User Typessss=", user.getUserType());
                        findViewById(R.id.simple_title_bar_layout).setVisibility(View.GONE);
                    }
                }
            });
        } else {
            openLoginFragment();
        }
    }

    private void openLoginFragment() {
        MainFragment fragment = new MainFragment();
        fragment.setCallBackAction(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        FirebaseMessaging.getInstance().deleteToken();
        openLoginFragment();
        findViewById(R.id.simple_title_bar_layout).setVisibility(View.VISIBLE);
    }

    private void goBack() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void openFragment(RootFragment fragment, String TAG) {
        fragment.setCallBackAction(this);
        getSupportFragmentManager().beginTransaction().addToBackStack(fragment.getClass().getName()).replace(R.id.container, fragment, TAG).commit();
    }

    @Override
    public void onUserLoggedIn(User user) {
        if (user == null) {
            Log.e("MainActivity", "onUserLoggedIn: User is null");
            Toast.makeText(this, "Login failed: User data not found", Toast.LENGTH_LONG).show();
            openLoginFragment();
            return;
        }
        Log.e("User loggedin", user.getEmailAddress());
        User.setCurrentLoggedInUser(user);
        if (user instanceof Lawyer) {
            openFragment(new LawyerHome(), "LAWYER");
        } else if (user instanceof Client) {
            openClientFragment();
        }
        findViewById(R.id.simple_title_bar_layout).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void openClientFragment() {
        if (meetingID.equals("NOT SET")) {
            openFragment(new ClientHome(), "CLIENT");
        } else {
            ClientHome clientHome = new ClientHome();
            Bundle bundle = new Bundle();
            bundle.putString("ID", meetingID);
            bundle.putString("PASSWORD", meetingPassword); // Kept for compatibility, unused by Jitsi
            clientHome.setArguments(bundle);
            openFragment(clientHome, "CLIENT");
            // Optionally launch meeting immediately
            launchJitsiMeeting(meetingID);
        }
    }
}
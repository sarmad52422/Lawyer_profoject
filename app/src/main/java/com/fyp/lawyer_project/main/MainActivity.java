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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class MainActivity extends MainFragmentActivity {
    private String meetingID = "NOT SET";
    private String meetingPassword = "NOT SET";
    public static boolean active = false;
    private boolean meetingDisconnecting = false;
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        initMainFragment();
        findViewById(R.id.backBtn).setOnClickListener(view -> goBack());
        initalizeZoom();
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
                meetingPassword = idPassword[1];
                Log.e("Meeting ID", idPassword[0] + "");
                Log.e("Meeting Password", idPassword[1] + "");
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
                ((ClientHome)fragment).showNotification(notificationMessage,title);

            } else {
                ((ClientHome) fragment).showNotification(notificationMessage, "meeting");
            }
        }

    }

    private void askForMeetingIfFragmentIsOpen() {
        RootFragment fragment = (RootFragment) getSupportFragmentManager().findFragmentByTag("CLIENT");

        if (fragment != null && fragment.isVisible() && !meetingDisconnecting) {

            ((ClientHome) fragment).showMeetingConfirmationDialog(meetingID, meetingPassword);
        }

    }

    private final MeetingServiceListener meetingServiceListener = new MeetingServiceListener() {
        @Override
        public void onMeetingStatusChanged(MeetingStatus meetingStatus, int i, int i1) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
                meetingDisconnecting = true;
            }
        }
    };
    private final ZoomSDKAuthenticationListener authenticationListener = new ZoomSDKAuthenticationListener() {
        @Override
        public void onZoomSDKLoginResult(long l) {
            if (l == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {

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

    private void initalizeZoom() {
        ZoomSDK sdk = ZoomSDK.getInstance();
        ZoomSDKInitParams params = new ZoomSDKInitParams();
        params.appKey = "TexfGqKTu4PPCptnHorbTzTf03GUeb5blaaT";
        params.appSecret = "wTlN8BPhjExgZdIS3uJQxcQRZkLL0UhEgXeW";
        params.domain = "zoom.us";
        params.enableLog = true;
        ZoomSDKInitializeListener listener = new ZoomSDKInitializeListener() {
            @Override
            public void onZoomSDKInitializeResult(int i, int i1) {

            }

            @Override
            public void onZoomAuthIdentityExpired() {

            }
        };
        sdk.initialize(this, listener, params);
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
            MainFragment fragment = new MainFragment();
            fragment.setCallBackAction(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

    @Override
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        FirebaseMessaging.getInstance().deleteToken();
        MainFragment fragment = new MainFragment();
        fragment.setCallBackAction(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
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
        } else
            super.onBackPressed();
    }

    private void openClientFragment() {
        if (meetingID.equals("NOT SET") && meetingPassword.equals("NOT SET"))
            openFragment(new ClientHome(), "CLIENT");
        else {
            ClientHome clientHome = new ClientHome();
            Bundle bundle = new Bundle();
            bundle.putString("ID", meetingID);
            bundle.putString("PASSWORD", meetingPassword);
            clientHome.setArguments(bundle);
            openFragment(clientHome, "CLIENT");
        }
    }
}
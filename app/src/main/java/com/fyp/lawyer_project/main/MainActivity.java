package com.fyp.lawyer_project.main;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.client.ClientHome;
import com.fyp.lawyer_project.lawyer.LawyerHome;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends MainFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        initMainFragment();
        findViewById(R.id.backBtn).setOnClickListener(view -> goBack());



    }


    private void initMainFragment(){
        MainFragment fragment = new MainFragment();
        fragment.setCallBackAction(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        FirebaseAuth.getInstance().signOut();
    }
    private void goBack() {
        getSupportFragmentManager().popBackStack();
    }
    @Override
    public void openFragment(RootFragment fragment){
        fragment.setCallBackAction(this);
        getSupportFragmentManager().beginTransaction().addToBackStack(fragment.getClass().getName()).replace(R.id.container,fragment).commit();
    }
    @Override
    public void onUserLoggedIn(User user) {
        User.setCurrentLoggedInUser(user);
        if(user instanceof Lawyer){

            openFragment(new LawyerHome());
        }
        else if(user instanceof Client){
            openFragment(new ClientHome());
        }
        findViewById(R.id.simple_title_bar_layout).setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            goBack();
        }
        else
            super.onBackPressed();
    }
}
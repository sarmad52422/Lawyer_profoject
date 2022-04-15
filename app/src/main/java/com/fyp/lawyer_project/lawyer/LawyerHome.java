package com.fyp.lawyer_project.lawyer;

import android.os.Bundle;
import android.view.Gravity;
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
import androidx.fragment.app.Fragment;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragment;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.google.android.material.navigation.NavigationView;

public class LawyerHome extends RootFragment implements NavigationView.OnNavigationItemSelectedListener {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    private Lawyer currentUser;
    private NavigationView navView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView  = inflater.inflate(R.layout.lawyer_home,container,false);
        initClickActions();
        initHomeScreen();
        return rootView;
    }
    private void initClickActions(){
        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view->openDrawer());
    }
    private void openDrawer(){
        ((DrawerLayout)rootView.findViewById(R.id.drawer_layout_lawyer)).openDrawer(GravityCompat.START);
    }
    private void initHomeScreen(){
        currentUser = (Lawyer) User.getCurrentLoggedInUser();
        navView = rootView.findViewById(R.id.navViewLawyer);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);

        ((TextView)rootView.findViewById(R.id.lawyer_profile_name)).setText(currentUser.getFullName());
        ((TextView)rootView.findViewById(R.id.user_last_name)).setText(currentUser.getLastName());
        ((TextView)header.findViewById(R.id.drawer_user_namme)).setText(currentUser.getFullName());



    }

    @Override
    public void setCallBackAction(MainFragmentActivity fragmentActivity) {
        this.callBackHandler = fragmentActivity;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.my_profile_btn){
            getParentFragmentManager().beginTransaction().addToBackStack(LawyerProfile.class.getName()).replace(R.id.container,new LawyerProfile()).commit();
        }
        if(item.getItemId() == R.id.mng_schedules){
            getParentFragmentManager().beginTransaction().addToBackStack(LawyerScheduleManager.class.getName()).replace(R.id.container,new LawyerScheduleManager()).commit();
        }
        return true;
    }
}

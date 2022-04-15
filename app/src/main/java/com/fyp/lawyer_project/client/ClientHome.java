package com.fyp.lawyer_project.client;

import android.os.Bundle;
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
import com.fyp.lawyer_project.lawyer.LawyerProfile;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.GridEqualSpacing;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class ClientHome extends RootFragment implements LawyerPickerDialog.ListLoader, LawyersListAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {
    private View rootView;
    private Client currentUser;
    private NavigationView navView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      rootView = inflater.inflate(R.layout.client_home,container,false);
      initClickListener();
      initHomeScreen();
      return rootView;

    }

    private void initClickListener(){

        rootView.findViewById(R.id.drawer_btn).setOnClickListener(view->openDrawer());

    }
    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {

    }
    private void openDrawer(){
        ((DrawerLayout)rootView.findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
    }

    private void initHomeScreen(){
        currentUser = (Client) User.getCurrentLoggedInUser();
        navView = rootView.findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);

        ((TextView)rootView.findViewById(R.id.lawyer_profile_name)).setText(currentUser.getFullName());
        ((TextView)rootView.findViewById(R.id.user_last_name)).setText(currentUser.getLastName());
        ((TextView)header.findViewById(R.id.drawer_user_namme)).setText(currentUser.getFullName());



    }
    private void initClickActions(){

    }
    @Override
    public void onLawyersListLoaded(ArrayList<User> lawyers) {
        RecyclerView recyclerView = rootView.findViewById(R.id.lawyersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        LawyersListAdapter adapter = new LawyersListAdapter(rootView.getContext(),lawyers);
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

        if(item.getItemId() == R.id.browserLawyers){
            LawyerPickerDialog dialog = new LawyerPickerDialog(rootView.getContext());
            dialog.setActionCallBack(this);
            dialog.show();
            ((DrawerLayout)rootView.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        }
        return true;
    }
}

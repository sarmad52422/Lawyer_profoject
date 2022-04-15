package com.fyp.lawyer_project.client;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

public class LoginClient extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBacks;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.client_login,container,false);
        init();
        return rootView;
    }
    private void init(){

        rootView.findViewById(R.id.login_btn).setOnClickListener(view -> login());
        rootView.findViewById(R.id.signup_btn).setOnClickListener(view->signup());
    }
    private void login(){
        String email = ((EditText)rootView.findViewById(R.id.email)).getText().toString();
        String password = ((EditText)rootView.findViewById(R.id.password)).getText().toString();
        FirebaseHelper.loginUser(email, password,User.TYPE_CLIENT, new ProgressDialog(rootView.getContext()),new FirebaseHelper.FirebaseActions() {
            @Override
            public void onLogin(String status, User user) {
                Toast.makeText(rootView.getContext(),status,Toast.LENGTH_LONG).show();
                callBacks.onUserLoggedIn(user);
            }
        });
    }
    private void signup(){
        getParentFragmentManager().beginTransaction().addToBackStack(ClientSignupFragment.class.getName()).replace(R.id.container,new ClientSignupFragment()).commit();
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBacks = callbacks;
    }
}

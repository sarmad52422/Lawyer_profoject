package com.fyp.lawyer_project.lawyer;

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
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class LawyerLogin extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_lawyer_login,container,false);
        rootView.findViewById(R.id.signup_btn).setOnClickListener(view-> callBackHandler.openFragment(new LawyerSignupFragment(),LawyerSignupFragment.class.getName()));
        rootView.findViewById(R.id.login_btn).setOnClickListener(view->login());
        return rootView;
    }

    private void login(){
        ProgressDialog dialog = new ProgressDialog(rootView.getContext());
        dialog.setMessage("Please Wait....");
        String email = ((EditText)rootView.findViewById(R.id.emailfield)).getText().toString();
        String password = ((EditText)rootView.findViewById(R.id.passwfield)).getText().toString();
        FirebaseHelper.loginUser(email, password, User.TYPE_LAWYER, dialog,new FirebaseHelper.FirebaseActions() {
            @Override
            public void onLogin(String status, User user) {
                Toast.makeText(rootView.getContext(), status,Toast.LENGTH_LONG).show();
                if(status.equals("Successful")) {
                    callBackHandler.onUserLoggedIn(user);
                }
                else if(status.equals("Already Logged In")){
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseHelper.getUserInformation(auth.getCurrentUser().getEmail(), User.TYPE_LAWYER, dialog, new FirebaseHelper.FirebaseActions() {
                        @Override
                        public void onUserFound(User user) {
                            callBackHandler.onUserLoggedIn(user);
                        }
                    });

                }
            }
        });
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandler = callbacks;
    }
}

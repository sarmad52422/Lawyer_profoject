package com.fyp.lawyer_project.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

public class ClientSignupFragment extends Fragment {
    private View rootView;
    private final static String TAG = ClientSignupFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_client_signup, container, false);
        rootView.findViewById(R.id.submit_details_btn).setOnClickListener(view -> signup());
        listUsers();
        return rootView;
    }
    private void listUsers(){
        FirebaseHelper.listUsers(User.TYPE_LAWYER,new FirebaseHelper.FirebaseActions() {
            @Override
            public void onUserFound(User user) {
                Log.e(TAG,user.getEmailAddress());
            }
        });
    }

    private void signup() {
        String name = ((EditText) rootView.findViewById(R.id.usernamefield)).getText().toString();
        String email = ((EditText) rootView.findViewById(R.id.emailfield)).getText().toString();
        String phoneNumber = ((EditText) rootView.findViewById(R.id.phoneNumberField)).getText().toString();
        String password = ((EditText) rootView.findViewById(R.id.passwfield)).getText().toString();
        String confirmPassword = ((EditText) rootView.findViewById(R.id.confirmfield)).getText().toString();
        if (!name.isEmpty() && !email.isEmpty() && !phoneNumber.isEmpty() && !password.isEmpty()) {
            if (password.equals(confirmPassword)) {
                Client client = new Client(name, phoneNumber, email, password,"Koi case v ho sakda");
                FirebaseHelper.signUpUser(client, new ProgressDialog(rootView.getContext()),new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onSignupComplete(String status) {
                        Toast.makeText(getContext(),status,Toast.LENGTH_LONG).show();
                        Log.e(TAG,status);
                    }
                });
            } else
                Toast.makeText(rootView.getContext(), "Password does not match", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(rootView.getContext(), "Please Fill all Fields", Toast.LENGTH_LONG).show();


    }
}

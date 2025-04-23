package com.fyp.lawyer_project.client;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class ClientSignupFragment extends Fragment {
    private View rootView;
    private static final String TAG = ClientSignupFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_client_signup, container, false);
        rootView.findViewById(R.id.submit_details_btn).setOnClickListener(view -> signup());
        return rootView;
    }

    private void signup() {
        TextInputEditText nameField = rootView.findViewById(R.id.username_field);
        TextInputEditText emailField = rootView.findViewById(R.id.email_field);
        TextInputEditText phoneField = rootView.findViewById(R.id.phone_number_field);
        TextInputEditText passwordField = rootView.findViewById(R.id.password_field);
        TextInputEditText confirmPasswordField = rootView.findViewById(R.id.confirm_password_field);

        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phoneNumber = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        // Input validation
        if (name.isEmpty()) {
            nameField.setError("Name is required");
            nameField.requestFocus();
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid email is required");
            emailField.requestFocus();
            return;
        }
        if (phoneNumber.isEmpty() || !phoneNumber.matches("\\d{10,13}")) {
            phoneField.setError("Valid phone number is required (10-13 digits)");
            phoneField.requestFocus();
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match");
            confirmPasswordField.requestFocus();
            return;
        }

        // Split name into firstName and lastName
        String firstName = name;
        String lastName = "";
        int spaceIndex = name.lastIndexOf(" ");
        if (spaceIndex != -1) {
            firstName = name.substring(0, spaceIndex).trim();
            lastName = name.substring(spaceIndex + 1).trim();
        }

        // Generate userId
        String userId = "_" + email.substring(0, email.indexOf("@"));

        // Create Client object
        Client client = new Client(email,password,User.TYPE_CLIENT,firstName,lastName,userId,phoneNumber);

        // Sign up with Firebase
        FirebaseHelper.signUpUser(client, new ProgressDialog(rootView.getContext()), new FirebaseHelper.FirebaseActions() {
            @Override
            public void onSignupComplete(String status) {
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                Log.e(TAG, status);
                getFragmentManager().popBackStack();
                FirebaseAuth.getInstance().signOut();
            }
        });
    }
}
package com.fyp.lawyer_project.lawyer;

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
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

public class LawyerProfile extends RootFragment {
    private View rootView;
    Lawyer user = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.lawyer_profile_layout,container,false);
        initLawyerProfile();
        initActions();
        return rootView;
    }
    private void initActions(){
        rootView.findViewById(R.id.back_btn).setOnClickListener(view->{
            getParentFragmentManager().popBackStack();
        });
        rootView.findViewById(R.id.save_profile_btn).setOnClickListener(view->saveProfileUpdates());

    }
    private void saveProfileUpdates(){
        user.setPracticeArea(((EditText)rootView.findViewById(R.id.profileExpertiseField)).getText().toString());
        user.setPhoneNumber(((EditText)rootView.findViewById(R.id.profilePhoneNumberField)).getText().toString());
        user.setFullName(((EditText)rootView.findViewById(R.id.profileNameField)).getText().toString());
        String password = ((EditText)rootView.findViewById(R.id.profilePasswordField)).getText().toString();
        String confirmPassword = ((EditText)rootView.findViewById(R.id.profileConfirmPasswordField)).getText().toString();
        if(!user.getPracticeArea().isEmpty()&&!user.getPhoneNumber().isEmpty()&&!user.getFullName().isEmpty()){

            if(!password.isEmpty()) {
                user.setPassword(password);
                if (user.getPassword().equals(confirmPassword)) {
                    FirebaseHelper.updateUser(User.TYPE_LAWYER, user);
                } else {
                    Toast.makeText(rootView.getContext(), "Password Does Not Match", Toast.LENGTH_LONG).show();
                }
            }
            else{
                FirebaseHelper.updateUser(User.TYPE_LAWYER,user);
            }
        }
        else{
            Toast.makeText(rootView.getContext(),"Please Fill In ALl Fileds",Toast.LENGTH_LONG).show();
        }
    }
    private void initLawyerProfile(){
         user =(Lawyer) User.getCurrentLoggedInUser();
        ((EditText)rootView.findViewById(R.id.profileNameField)).setText(user.getFullName());
        ((EditText)rootView.findViewById(R.id.profileEmailField)).setText(user.getEmailAddress());
        ((EditText)rootView.findViewById(R.id.profilePhoneNumberField)).setText(user.getPhoneNumber());
        ((EditText)rootView.findViewById(R.id.profileExpertiseField)).setText(user.getPracticeArea());

    }
    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {

    }
}

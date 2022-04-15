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
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.utils.FirebaseHelper;

public class LawyerSignupFragment extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBackHandler ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       rootView = inflater.inflate(R.layout.activity_lawyer_signup,container,false);
       rootView.findViewById(R.id.submit_details_btn).setOnClickListener(view -> signUp());
       return  rootView;
    }
    private void signUp(){
        String name = ((EditText)rootView.findViewById(R.id.lawyerusernamefield)).getText().toString().trim();
        String email = ((EditText)rootView.findViewById(R.id.lawyeremailfield)).getText().toString().trim();
        String phoneNumber = ((EditText)rootView.findViewById(R.id.phoneNumberField)).getText().toString().trim();
        String fieldArea = ((EditText)rootView.findViewById(R.id.practiceField)).getText().toString().trim();
        String pass = ((EditText)rootView.findViewById(R.id.passwfield)).getText().toString().trim();
        String confirmPass = ((EditText)rootView.findViewById(R.id.confirmfield)).getText().toString().trim();
        if(!name.isEmpty()&&!email.isEmpty()&&!phoneNumber.isEmpty()&&!fieldArea.isEmpty()&&!pass.isEmpty()){
            if(pass.equals(confirmPass)) {
                Lawyer lawyer = new Lawyer(name, email, pass,phoneNumber, fieldArea,55000,60000);
                FirebaseHelper.signUpUser(lawyer, new ProgressDialog(rootView.getContext()),new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onSignupComplete(String status) {
                        Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                Toast.makeText(getContext(),"Password does not match",Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(getContext(),"Fill in all fields",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandler = callbacks;
    }
}

package com.fyp.lawyer_project.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.admin.AdminFragment;
import com.fyp.lawyer_project.client.LoginClient;
import com.fyp.lawyer_project.lawyer.LawyerLogin;

public class MainFragment extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_screen,container,false);
        rootView.findViewById(R.id.lawyer_btn).setOnClickListener(view->callBackHandler.openFragment(new LawyerLogin(),"LAWYER"));
        rootView.findViewById(R.id.client_btn).setOnClickListener(view->callBackHandler.openFragment(new LoginClient(),"CLIENT"));
        rootView.findViewById(R.id.admin_btn).setOnClickListener(view->callBackHandler.openFragment(new AdminFragment(),"ADMIN"));
        return rootView;
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandler = callbacks;
    }
}

package com.fyp.lawyer_project.main;

import androidx.fragment.app.FragmentActivity;

import com.fyp.lawyer_project.modal_classes.User;

public abstract class MainFragmentActivity  extends FragmentActivity {
    public abstract void onUserLoggedIn(User user);
    public abstract void openFragment(RootFragment fragment);
}

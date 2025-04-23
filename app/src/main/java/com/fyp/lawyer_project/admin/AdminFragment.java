package com.fyp.lawyer_project.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;

import java.util.ArrayList;

public class AdminFragment extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBackHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isUserLoaddedIn = getArguments() != null && getArguments().getBoolean("ISLOGIN");
        if (!isUserLoaddedIn) {
            rootView = inflater.inflate(R.layout.admin_login_layout, container, false);
            initLoginAdmin();
        } else {
            rootView = inflater.inflate(R.layout.admin_layout, container, false);
            initClicks();
            loadUsers(User.TYPE_CLIENT);
        }
        return rootView;
    }

    private void initLoginAdmin() {
        initClickListener();
    }

    private void initClickListener() {
        rootView.findViewById(R.id.login_btn).setOnClickListener(view -> loginAdmin());

    }
    private void initClicks(){
        rootView.findViewById(R.id.clientsBTN).setOnClickListener(vi->loadUsers(User.TYPE_CLIENT));
        rootView.findViewById(R.id.lawyers).setOnClickListener(vi->loadUsers(User.TYPE_LAWYER));
    }
    private void loadUsers(String userType) {
        RecyclerView userList = rootView.findViewById(R.id.usersList);
        userList.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        FirebaseHelper.loadAllUsers(new ProgressDialog(rootView.getContext()), userType, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onUsersLoaded(ArrayList<User> users) {
                UserAdapter userAdapter = new UserAdapter(users, userType);
                userList.setAdapter(userAdapter);
            }

            @Override
            public void onUserUpdated(String status) {

            }

            @Override
            public void onError(String error) {

            }
        });


    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        callBackHandler = callbacks;
    }

    private void loginAdmin() {
        String id = ((EditText) rootView.findViewById(R.id.adminID)).getText().toString();
        String password = ((EditText) rootView.findViewById(R.id.password)).getText().toString();
        if (id.equalsIgnoreCase("admin") && password.equalsIgnoreCase("admin")) {
            AdminFragment adminFragment = new AdminFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("ISLOGIN", true);
            adminFragment.setArguments(bundle);
            callBackHandler.openFragment(adminFragment, "ADMIN");
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

        private final ArrayList<User> users;
        private final String loadUserType;

        public UserAdapter(ArrayList<User> users, String loadUserType) {
            this.users = users;
            this.loadUserType = loadUserType;
        }

        @NonNull
        @Override
        public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(rootView.getContext()).inflate(R.layout.user_list_item, parent, false);
            return new UserHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull UserHolder holder, int position) {
            User user = users.get(position);
            holder.userID.setText(user.getUserId());
            String userStatus = user.getUserStatus() == User.NOT_APPROVED ? "NOT APPROVED" : "APPROVED";
            holder.userStatus.setText(userStatus);
            holder.itemView.setOnClickListener(view -> {
                showActionApproveDialog(position);
            });
        }

        private void showActionApproveDialog(int pos) {
            User user = users.get(pos);
            Log.e("user = ",user.toString());
            AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
            dialog.setTitle("User Status");
            dialog.setMessage("Do you want to Approve User?");
            dialog.setPositiveButton("Yes", (interFace, i) -> {
                FirebaseHelper.approveUser(loadUserType,user.getUserId() );
                notifyDataSetChanged();
            });
            dialog.setNegativeButton("NO", (inTerface, i) -> inTerface.dismiss());
            dialog.show();
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserHolder extends RecyclerView.ViewHolder {
            TextView userID;
            TextView userStatus;

            public UserHolder(@NonNull View itemView) {
                super(itemView);
                userID = itemView.findViewById(R.id.userIdField);
                userStatus = itemView.findViewById(R.id.userStatusField);
            }
        }
    }
}

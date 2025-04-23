package com.fyp.lawyer_project.lawyer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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

import com.fyp.common.CommentAdapter;
import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Comment;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.fyp.lawyer_project.utils.Utilities;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MyClientsFragment extends RootFragment {
    MainFragmentActivity callBacks;

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBacks = callbacks;
    }

    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.myclients_layout, container, false);
        initClientList();
        return rootView;
    }

    @SuppressLint("SetTextI18n")
    private void openCaseDialog(ClientCase clientCase) {
        Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.details_case_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ((TextView) dialog.findViewById(R.id.caseTitle)).setText(clientCase.getCaseTitle());
        ((TextView) dialog.findViewById(R.id.caseProgress)).setText("View Progresses");
        dialog.findViewById(R.id.caseProgress).setOnClickListener(view->{
            Dialog caseProgressDialog = new Dialog(getContext());
            RecyclerView recyclerView = new RecyclerView(getContext());
            caseProgressDialog.setContentView(recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            caseProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ArrayList<Comment> comments = clientCase.getCaseProgressComment();
            CommentAdapter adapter = new CommentAdapter(getContext(),comments);
            recyclerView.setAdapter(adapter);
            caseProgressDialog.show();

        });
        ((TextView) dialog.findViewById(R.id.caseBudget)).setText(clientCase.getCaseBudget() + "");
        ((TextView) dialog.findViewById(R.id.caseStatus)).setText(clientCase.getCaseStatus());
        ((TextView) dialog.findViewById(R.id.caseFeedback)).setText(clientCase.getClientFeedBack());
        ((TextView) dialog.findViewById(R.id.lawyerComment)).setText(clientCase.getLawyerComment());
        ((TextView) dialog.findViewById(R.id.clientID)).setText(clientCase.getClientID());
        ((TextView) dialog.findViewById(R.id.caseDetails)).setText(clientCase.getCaseDetails());
        dialog.findViewById(R.id.updateCaseProgressButton).setOnClickListener(view -> {
            AlertDialog.Builder inputDialog = new AlertDialog.Builder(rootView.getContext());
            EditText editText = new EditText(rootView.getContext());
            inputDialog.setView(editText);
            inputDialog.setTitle("Update Progress");
            inputDialog.setMessage("Enter Current Progress Message");
            inputDialog.setPositiveButton("Update", ((dialogInterface, i) -> {
                FirebaseHelper.updateCaseProgress(clientCase.getCaseId(), editText.getText().toString());
                FirebaseHelper.getUserToken(clientCase.getClientID(), new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onUserTokenLoaded(String token) {
                        Log.e("Sending FCM =",token);
                        Utilities.sendFCMPush(getContext(),token,"Case Progress",editText.getText().toString());
                    }

                    @Override
                    public void onUserUpdated(String status) {

                    }

                    @Override
                    public void onError(String error) {

                    }
                });
                dialogInterface.dismiss();
            }));
            inputDialog.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.dismiss()));
            inputDialog.show();
        });
        dialog.show();

    }

    private void initClientList() {
        RecyclerView recyclerView = rootView.findViewById(R.id.myclientsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String lawyerId = "_" + email.substring(0, email.indexOf('@'));
        FirebaseHelper.loadClientsCases(lawyerId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {
                if(caseArrayList.isEmpty()){
                    recyclerView.setVisibility(View.GONE);
                    rootView.findViewById(R.id.no_cl).setVisibility(View.VISIBLE);
                }
                else {
                    ClientsAdapter adapter = new ClientsAdapter(caseArrayList);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientHolder> {
        private ArrayList<ClientCase> caseArrayList;

        public ClientsAdapter(ArrayList<ClientCase> caseArrayList) {
            this.caseArrayList = caseArrayList;
        }

        @NonNull
        @Override
        public ClientHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.client_list_item, parent, false);
            return new ClientHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ClientHolder holder, int position) {
            ClientCase clientCase = caseArrayList.get(position);
            holder.userIdView.setText(clientCase.getClientID());
            holder.caseTitleView.setText(clientCase.getCaseTitle());
            holder.itemView.setOnClickListener(view -> openCaseDialog(clientCase));
        }

        @Override
        public int getItemCount() {
            return caseArrayList.size();
        }

        class ClientHolder extends RecyclerView.ViewHolder {
            TextView userIdView;
            TextView caseTitleView;

            public ClientHolder(@NonNull View itemView) {
                super(itemView);
                userIdView = itemView.findViewById(R.id.clientID);
                caseTitleView = itemView.findViewById(R.id.caseTitleField);
            }
        }
    }
}

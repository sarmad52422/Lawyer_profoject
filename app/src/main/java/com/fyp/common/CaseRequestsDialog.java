package com.fyp.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.fyp.lawyer_project.utils.Utilities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CaseRequestsDialog extends Dialog {
    private String lawyerId;
    private ArrayList<ClientCase> caseArrayList;
    private RecyclerView recyclerView;
    private TextView emptyView;

    public CaseRequestsDialog(@NonNull Context context, String lawyerId) {
        super(context);
        this.lawyerId = lawyerId;
        this.caseArrayList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.case_requests_dialog);
        recyclerView = findViewById(R.id.case_requests_list);
        emptyView = findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadCaseRequests();
    }

    private void loadCaseRequests() {
        FirebaseHelper.loadCases(lawyerId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onCasesLoaded(ArrayList<ClientCase> cases) {
                caseArrayList.clear();
                for (ClientCase c : cases) {
                    if (c.getCaseStatus().equals(ClientCase.NOT_ACCEPTED)) {
                        caseArrayList.add(c);
                    }
                }
                if (caseArrayList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "No pending case requests", Toast.LENGTH_SHORT).show();
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    CaseRequestAdapter adapter = new CaseRequestAdapter(caseArrayList);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("CaseRequestsDialog", "Error loading cases: " + error);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error loading cases: " + error);
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load case requests: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    class CaseRequestAdapter extends RecyclerView.Adapter<CaseRequestAdapter.CaseHolder> {
        private ArrayList<ClientCase> cases;

        public CaseRequestAdapter(ArrayList<ClientCase> cases) {
            this.cases = cases;
        }

        @NonNull
        @Override
        public CaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.case_request_item, parent, false);
            return new CaseHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CaseHolder holder, int position) {
            ClientCase clientCase = cases.get(position);
            holder.caseTitle.setText(clientCase.getCaseTitle());
            holder.clientId.setText("Client ID: " + clientCase.getClientID());
            holder.caseDetails.setText("Case Details: " + clientCase.getCaseDetails());
            holder.caseBudget.setText("Budget: " + NumberFormat.getCurrencyInstance(Locale.US).format(clientCase.getCaseBudget()));
            holder.caseStatus.setText("Status: " + clientCase.getCaseStatus());

            holder.acceptButton.setOnClickListener(v -> {
                FirebaseHelper.acceptCase(clientCase.getCaseId());
                FirebaseHelper.getUserToken(clientCase.getClientID(), new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onUserTokenLoaded(String token) {
                        Utilities.sendFCMPush(getContext(), token, "Case Accepted",
                                "Your case '" + clientCase.getCaseTitle() + "' was accepted.");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("CaseRequestsDialog", "Error fetching FCM token: " + error);
                    }
                });
                cases.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cases.size());
                updateEmptyView();
            });

            holder.rejectButton.setOnClickListener(v -> {
                FirebaseHelper.rejectCase(clientCase.getCaseId());
                FirebaseHelper.getUserToken(clientCase.getClientID(), new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onUserTokenLoaded(String token) {
                        Utilities.sendFCMPush(getContext(), token, "Case Rejected",
                                "Your case '" + clientCase.getCaseTitle() + "' was rejected.");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("CaseRequestsDialog", "Error fetching FCM token: " + error);
                    }
                });
                cases.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cases.size());
                updateEmptyView();
            });
        }

        @Override
        public int getItemCount() {
            return cases.size();
        }

        private void updateEmptyView() {
            if (cases.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }

        class CaseHolder extends RecyclerView.ViewHolder {
            TextView caseTitle;
            TextView clientId;
            TextView caseDetails;
            TextView caseBudget;
            TextView caseStatus;
            View acceptButton;
            View rejectButton;

            public CaseHolder(@NonNull View itemView) {
                super(itemView);
                caseTitle = itemView.findViewById(R.id.case_title);
                clientId = itemView.findViewById(R.id.client_id);
                caseDetails = itemView.findViewById(R.id.case_details);
                caseBudget = itemView.findViewById(R.id.case_budget);
                caseStatus = itemView.findViewById(R.id.case_status);
                acceptButton = itemView.findViewById(R.id.accept_case_button);
                rejectButton = itemView.findViewById(R.id.reject_case_button);
            }
        }
    }
}
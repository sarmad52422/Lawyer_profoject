package com.fyp.common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class CaseRequestsDialog extends BottomSheetDialog {
    public CaseRequestsDialog(@NonNull Context context, String lawyerId) {
        super(context);
        setContentView(R.layout.cases_request_list_dialog);
        RecyclerView recyclerView = findViewById(R.id.casesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseHelper.loadCases(lawyerId, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {
                CaseListAdapter adapter = new CaseListAdapter(getContext(), caseArrayList);
                recyclerView.setAdapter(adapter);
                adapter.setOnCaseItemClickListener(new CaseItemClickListener() {
                    @Override
                    public void onCaseResponseButtonClicked(ClientCase clientCase) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("Case Request");
                        dialog.setMessage("Accept Or Reject Case");
                        dialog.setNegativeButton("Reject", (dialogInterface, i) -> {
                            FirebaseHelper.rejectCase(clientCase.getCaseId());
                            adapter.remove(clientCase);
                            dialogInterface.dismiss();
                        });
                        dialog.setPositiveButton("Accept", (dialogInterface, i) -> {
                            FirebaseHelper.acceptCase(clientCase.getCaseId());
                            dialogInterface.dismiss();
                            adapter.remove(clientCase);
                        });
                        dialog.setNeutralButton("Cancel", ((dialogInterface, i) -> dialogInterface.dismiss()));
                        dialog.show();
                    }

                    @Override
                    public void onViewCaseDetailsButtonClicked(ClientCase clientCase) {

                    }
                });

            }
        });


    }

    private class CaseListAdapter extends RecyclerView.Adapter<CaseListAdapter.CaseHolder> {
        private final ArrayList<ClientCase> caseArrayList;
        private final Context iContext;
        CaseItemClickListener itemClickListener;

        public CaseListAdapter(Context iContext, ArrayList<ClientCase> caseArrayList) {
            this.caseArrayList = caseArrayList;
            this.iContext = iContext;
        }

        @NonNull
        @Override
        public CaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(iContext).inflate(R.layout.case_list_item, parent, false);
            return new CaseHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CaseHolder holder, int position) {

            ClientCase clientCase = caseArrayList.get(position);
            holder.clientNameView.setText(clientCase.getClientID());
            holder.caseTitleView.setText(clientCase.getCaseTitle());
            holder.acceptCaseBtn.setOnClickListener(view -> itemClickListener.onCaseResponseButtonClicked(clientCase));
            holder.viewCaseBtn.setOnClickListener(view -> itemClickListener.onViewCaseDetailsButtonClicked(clientCase));

        }

        public void setOnCaseItemClickListener(CaseItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;

        }
        @SuppressLint("NotifyDataSetChanged")
        public void remove(ClientCase clientCase){
            caseArrayList.remove(clientCase);
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            return caseArrayList.size();
        }

        class CaseHolder extends RecyclerView.ViewHolder {
            TextView clientNameView;
            TextView caseTitleView;
            View viewCaseBtn;
            View acceptCaseBtn;

            public CaseHolder(@NonNull View itemView) {
                super(itemView);
                clientNameView = itemView.findViewById(R.id.clientName);
                caseTitleView = itemView.findViewById(R.id.caseTitle);
                viewCaseBtn = itemView.findViewById(R.id.viewDetailsButton);
                acceptCaseBtn = itemView.findViewById(R.id.acceptCaseButton);
            }
        }
    }

    interface CaseItemClickListener {
        void onCaseResponseButtonClicked(ClientCase clientCase);

        void onViewCaseDetailsButtonClicked(ClientCase clientCase);
    }
}

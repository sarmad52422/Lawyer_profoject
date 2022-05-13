package com.fyp.lawyer_project.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;

import java.util.ArrayList;

public class LawyersListAdapter extends RecyclerView.Adapter<LawyersListAdapter.LawyerHolder> {

    private Context iContext;
    private ArrayList<User> lawyers;
    private OnItemClickListener onItemClickListener;
    public LawyersListAdapter(Context iContext,ArrayList<User> lawyers){
        this.iContext = iContext;
        this.lawyers = lawyers;
    }
    @NonNull
    @Override
    public LawyerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(iContext).inflate(R.layout.lawyers_list_item,parent,false);
        return new LawyerHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LawyerHolder holder, int position) {
        final Lawyer lawyer =(Lawyer)  lawyers.get(position);
        holder.lawyerBudget.setText(lawyer.getStartPrice()+" - "+lawyer.getEndPrice());
        holder.lawyerName.setText(lawyer.getFullName());
        holder.lawyerPractice.setText(lawyer.getPracticeArea());
        holder.itemView.setOnClickListener(view->onItemClickListener.onItemClicked(lawyer));
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return lawyers.size();
    }
    interface OnItemClickListener {
        void onItemClicked(Lawyer lawyer);
    }
    static class LawyerHolder extends RecyclerView.ViewHolder{
        TextView lawyerName;
        TextView lawyerPractice;
        TextView lawyerBudget;
        ImageFilterView lawyerProfilePicture;
        public LawyerHolder(@NonNull View itemView) {
            super(itemView);
            this.lawyerName = itemView.findViewById(R.id.lawyerName);
            this.lawyerPractice = itemView.findViewById(R.id.areOfPractice);
            this.lawyerBudget = itemView.findViewById(R.id.lawyerRate);
        }
    }
}

package com.fyp.lawyer_project.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;

import java.util.ArrayList;

public class AppointmentListAdapter extends RecyclerView.Adapter<AppointmentListAdapter.AppointmentHolder> {
    private Context iContext;
    private final ArrayList<Appointment> appointments;
    public AppointmentListAdapter(Context iContext, ArrayList<Appointment> appointments){
        this.appointments = appointments;
        this.iContext = iContext;

    }
    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(iContext).inflate(R.layout.appointment_item,parent,false);
        return new AppointmentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {

        holder.lawyerName.setText(appointments.get(position).getLawyerID());
        holder.appointmentCancelButton.setOnClickListener(new HandelClick(position));
        holder.appointmentDate.setText(appointments.get(position).getAppointmentDate());

    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class HandelClick implements View.OnClickListener{

        int position;
        public HandelClick(int pos){
            this.position = pos;
        }
        @Override
        public void onClick(View view) {

        }
    }
    class AppointmentHolder extends RecyclerView.ViewHolder{
        TextView lawyerName;
        TextView appointmentDate;
        ImageView markCompleteButton;
        ImageView appointmentCancelButton;
        public AppointmentHolder(@NonNull View itemView) {
            super(itemView);
            lawyerName = itemView.findViewById(R.id.lawyerName);
            appointmentDate = itemView.findViewById(R.id.appointmentDateAndTime);
            appointmentCancelButton = itemView.findViewById(R.id.cancelAppointmentButton);
        }
    }
}

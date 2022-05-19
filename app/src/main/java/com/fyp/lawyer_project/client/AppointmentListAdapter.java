package com.fyp.lawyer_project.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;

import java.util.ArrayList;

public class AppointmentListAdapter extends RecyclerView.Adapter<AppointmentListAdapter.AppointmentHolder> {
    private Context iContext;
    private final ArrayList<Appointment> appointments;
    private OnItemClickListener onItemClickListener;

    public AppointmentListAdapter(Context iContext, ArrayList<Appointment> appointments) {
        this.appointments = appointments;
        this.iContext = iContext;

    }

    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(iContext).inflate(R.layout.appointment_item, parent, false);
        return new AppointmentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {

        holder.lawyerName.setText(appointments.get(position).getLawyerID());
        holder.appointmentCancelButton.setOnClickListener(new HandelClick(position));
        holder.appointmentDate.setText(appointments.get(position).getAppointmentDate());
        holder.appointmentStatusView.setText(appointments.get(position).getAppointmentStatus());
        holder.markCompleteButton.setOnClickListener(new HandelClick(position));

    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class HandelClick implements View.OnClickListener {

        int position;

        public HandelClick(int pos) {
            this.position = pos;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.cancelAppointmentButton) {
                onItemClickListener.onCancelAppointmentButtonClicked(appointments.get(position));

            } else if (view.getId() == R.id.markAppointmentComplete) {
                onItemClickListener.onMarkCompleteButtonClicked(appointments.get(position));

            }
        }
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    class AppointmentHolder extends RecyclerView.ViewHolder {
        TextView lawyerName;
        TextView appointmentDate;
        LinearLayoutCompat markCompleteButton;
        LinearLayoutCompat appointmentCancelButton;
        TextView appointmentStatusView;

        public AppointmentHolder(@NonNull View itemView) {
            super(itemView);
            lawyerName = itemView.findViewById(R.id.lawyerName);
            appointmentDate = itemView.findViewById(R.id.appointmentDateAndTime);
            appointmentCancelButton = itemView.findViewById(R.id.cancelAppointmentButton);
            markCompleteButton = itemView.findViewById(R.id.markAppointmentComplete);
            appointmentStatusView = itemView.findViewById(R.id.statusView);
        }
    }

    interface OnItemClickListener {
        void onMarkCompleteButtonClicked(Appointment appointment);

        void onCancelAppointmentButtonClicked(Appointment appointment);
    }
}

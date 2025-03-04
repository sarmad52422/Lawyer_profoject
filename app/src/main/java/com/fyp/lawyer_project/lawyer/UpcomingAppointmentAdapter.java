package com.fyp.lawyer_project.lawyer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpcomingAppointmentAdapter extends RecyclerView.Adapter<UpcomingAppointmentAdapter.AppointmentHolder> {
    private Context iContext;
    private ArrayList<Appointment> appointments;
    private OnItemClickListener onItemClickListener;

    public UpcomingAppointmentAdapter(Context iContext, ArrayList<Appointment> appointments) {
        this.iContext = iContext;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(iContext).inflate(R.layout.up_coming_appointment_item, parent, false);
        return new AppointmentHolder(v);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.clientIDView.setText(appointment.getClientID());
        holder.dateView.setText(appointment.getAppointmentDate());
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClicked(appointment);
            }
        });
    }

    public void addItem(Appointment appointment) {
        appointments.add(appointment);
        notifyItemInserted(appointments.size() - 1);
    }

    public void updateAppointments(ArrayList<Appointment> newAppointments) {
        appointments.clear();
        appointments.addAll(newAppointments);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentHolder extends RecyclerView.ViewHolder {
        TextView clientIDView;
        TextView dateView;
        CircleImageView profileImage;

        public AppointmentHolder(@NonNull View itemView) {
            super(itemView);
            clientIDView = itemView.findViewById(R.id.clientIdView);
            dateView = itemView.findViewById(R.id.appointmentDateView);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }

    interface OnItemClickListener {
        void onItemClicked(Appointment appointment);
    }
}
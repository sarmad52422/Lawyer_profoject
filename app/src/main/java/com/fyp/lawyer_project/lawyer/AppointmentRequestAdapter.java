package com.fyp.lawyer_project.lawyer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.fyp.lawyer_project.utils.Utilities;

import java.util.ArrayList;

public class AppointmentRequestAdapter extends RecyclerView.Adapter<AppointmentRequestAdapter.AppointmentHolder> {
    private ArrayList<Appointment> appointmentArrayList;
    private Context iContext;
    private Dialog confirmationDialog;
    private AppointmentListCallBack callBack;

    public AppointmentRequestAdapter(ArrayList<Appointment> appointments, Context iContext) {
        this.appointmentArrayList = appointments;
        this.iContext = iContext;
        confirmationDialog = new Dialog(iContext);
        confirmationDialog.setContentView(R.layout.confirmation_dialog);
        int screenWidth = getScreenWidth();
        confirmationDialog.getWindow().setLayout(screenWidth - 40, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private int getScreenWidth() {
        DisplayMetrics metrics = iContext.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    @NonNull
    @Override
    public AppointmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(iContext).inflate(R.layout.appointment_card, parent, false);
        return new AppointmentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentHolder holder, int position) {
        Appointment appointment = appointmentArrayList.get(position);
        ClickHandler handler = new ClickHandler(position);
        holder.viewMessageButton.setOnClickListener(handler);
        holder.declineButton.setOnClickListener(handler);
        holder.acceptButton.setOnClickListener(handler);
        holder.clientFirstNameView.setText(appointment.getClientID());
        holder.dateAndTime.setText(appointment.getAppointmentDate());
    }

    public void removeItem(Appointment appointment) {
        int position = appointmentArrayList.indexOf(appointment);
        if (position != -1) {
            appointmentArrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointmentArrayList.size());
        }
    }

    public void updateAppointments(ArrayList<Appointment> newAppointments) {
        appointmentArrayList.clear();
        appointmentArrayList.addAll(newAppointments);
        notifyDataSetChanged();
    }

    public void setOnItemCallBack(AppointmentListCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public int getItemCount() {
        return appointmentArrayList.size();
    }

    class ClickHandler implements View.OnClickListener {
        private int position;

        public ClickHandler(int pos) {
            this.position = pos;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onClick(View view) {
            Appointment appointment = appointmentArrayList.get(position);
            if (view.getId() == R.id.viewAppointmentMessageButton) {
                openAppointmentMessage(appointment);
            } else if (view.getId() == R.id.cancel_appointment_button) {
                ((TextView) confirmationDialog.findViewById(R.id.confirmationMessageView)).setText(
                        "Are You Sure You Want To Reject Appointment?"
                );
                confirmationDialog.findViewById(R.id.positiveButton).setOnClickListener(v1 -> {
                    FirebaseHelper.updateAppointmentStatus(appointment.getAppointmentId(), Appointment.STATUS_REJECTED);
                    FirebaseHelper.getUserToken(appointment.getClientID(), new FirebaseHelper.FirebaseActions() {
                        @Override
                        public void onUserTokenLoaded(String token) {
                            Utilities.sendFCMPush(iContext, token, "Appointment Rejected",
                                    "Your appointment on " + appointment.getAppointmentDate() + " was rejected.");
                        }
                    });
                    confirmationDialog.dismiss();
                    callBack.onAppointmentRejected(appointment);
                });
                confirmationDialog.findViewById(R.id.negtiveButton).setOnClickListener(v2 -> confirmationDialog.dismiss());
                confirmationDialog.show();
            } else if (view.getId() == R.id.accept_appointment_button) {
                ((TextView) confirmationDialog.findViewById(R.id.confirmationMessageView)).setText(
                        "Are You Sure You Want To Accept Appointment?"
                );
                confirmationDialog.findViewById(R.id.positiveButton).setOnClickListener(v1 -> {
                    FirebaseHelper.updateAppointmentStatus(appointment.getAppointmentId(), Appointment.STATUS_ACCEPTED);
                    FirebaseHelper.getUserToken(appointment.getClientID(), new FirebaseHelper.FirebaseActions() {
                        @Override
                        public void onUserTokenLoaded(String token) {
                            Utilities.sendFCMPush(iContext, token, "Appointment Accepted",
                                    "Your appointment on " + appointment.getAppointmentDate() + " was accepted.");
                        }
                    });
                    confirmationDialog.dismiss();
                    callBack.onAppointmentAccepted(appointment);
                });
                confirmationDialog.findViewById(R.id.negtiveButton).setOnClickListener(v2 -> confirmationDialog.dismiss());
                confirmationDialog.show();
            }
        }
    }

    private void openAppointmentMessage(Appointment appointment) {
        Dialog dialog = new Dialog(iContext);
        dialog.setContentView(R.layout.msg_view_dialog);
        ((TextView) dialog.findViewById(R.id.msgView)).setText(appointment.getAppointmentMessage());
        dialog.show();
    }

    class AppointmentHolder extends RecyclerView.ViewHolder {
        TextView dateAndTime;
        TextView clientFirstNameView;
        TextView clientLastNameView;
        View acceptButton;
        View declineButton;
        View viewMessageButton;

        public AppointmentHolder(@NonNull View itemView) {
            super(itemView);
            dateAndTime = itemView.findViewById(R.id.appointment_date_and_time);
            clientFirstNameView = itemView.findViewById(R.id.client_first_name);
            clientLastNameView = itemView.findViewById(R.id.client_last_name);
            acceptButton = itemView.findViewById(R.id.accept_appointment_button);
            declineButton = itemView.findViewById(R.id.cancel_appointment_button);
            viewMessageButton = itemView.findViewById(R.id.viewAppointmentMessageButton);
        }
    }

    public interface AppointmentListCallBack {
        void onAppointmentAccepted(Appointment appointment);
        void onAppointmentRejected(Appointment appointment);
        void onUserImageClicked(String clientID);
    }
}
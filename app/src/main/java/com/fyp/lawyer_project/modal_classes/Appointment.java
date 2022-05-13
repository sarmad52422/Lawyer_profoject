package com.fyp.lawyer_project.modal_classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class Appointment {
    public static final String DATE_FORMAT = "EEEE dd-MMMM-yyyy KK:mm a";
    private String lawyerID;
    private String clientID;
    private String appointmentDate;
    private String appointmentMessage;
    private String appointmentId;
    private String appointmentStatus = "Not Accepted";

    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_NOT_ACCEPTED = "Not Accepted";
    public static final String STATUS_REJECTED = "REJECTED";

    public Appointment(String appointmentId, String lawyerID, String clientID, String appointmentDate, String appointmentMessage) {
        this.lawyerID = lawyerID;
        this.clientID = clientID;
        this.appointmentDate = appointmentDate;
        this.appointmentMessage = appointmentMessage;
        this.appointmentId = appointmentId;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public Appointment() {
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setLawyerID(@NonNull String lawyerID) {
        this.lawyerID = lawyerID;
    }

    public void setClientID(@NonNull String clientID) {
        this.clientID = clientID;
    }

    public void setAppointmentDate(@NonNull String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentMessage(@Nullable String appointmentMessage) {
        this.appointmentMessage = appointmentMessage;
    }

    public String getLawyerID() {
        return lawyerID;
    }

    public String getClientID() {
        return clientID;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentMessage() {
        return appointmentMessage;
    }
}

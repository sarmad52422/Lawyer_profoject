package com.fyp.lawyer_project.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.fyp.lawyer_project.modal_classes.Appointment;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.ClientCase;
import com.fyp.lawyer_project.modal_classes.Comment;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.Schedule;
import com.fyp.lawyer_project.modal_classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class FirebaseHelper {
    public static final String CLIENT_TABLE = "Users/Client";
    public static final String LAWYER_TABLE = "Users/Lawyer";
    public static final String APPOINTMENT_TABLE = "Appointments";
    public static final String CASE_TABLE = "Cases";

    public static void loginUser(String email, String password, String userType, Dialog progressBarView, FirebaseActions actions) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressBarView.show();
        if (auth.getCurrentUser() == null) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    getUserInformation(email, userType, progressBarView, new FirebaseActions() {
                        @Override
                        public void onUserFound(User user) {
                            if(user.getUserStatus() == User.APPROVED) {
                                actions.onLogin("Successful", user);
                                updateUserToken(user);
                                progressBarView.dismiss();
                            }
                            else{
                                actions.onLogin("User Not Approved By Admin",null);
                                auth.signOut();
                            }
                        }
                    });

                } else {
                    actions.onLogin("Error " + task.getException().getMessage(), null);
                    progressBarView.dismiss();
                }
            });
        }
    }

    private static void updateUserToken(User user) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();
                user.setToken(token);
                if (user.getUserType().equals(User.TYPE_CLIENT))
                    FirebaseDatabase.getInstance().getReference(CLIENT_TABLE).child(user.getUserId()).child("token").setValue(token);
                else if (user.getUserType().equals(User.TYPE_LAWYER)) {
                    FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).child(user.getUserId()).child("token").setValue(token);

                }
            }
        });
    }

    public static void loadAppointmentRecord(String userName, FirebaseActions actions) {
        ArrayList<Appointment> appointments = new ArrayList<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE);
        database.orderByChild("clientID").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mSnap : snapshot.getChildren()) {

                    Appointment appointment = mSnap.getValue(Appointment.class);
                    Log.e("Key = ", appointment.getLawyerID());
                    appointments.add(appointment);
                }
                actions.onAppointmentRecordLoaded(appointments);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void sendMeetingNotification(Context iContext, String meetingId, String meetingPassword, String clientId) {
        String meetingTitle = "Meeting Join Request";
        String meetingMsg = "Meeting ID: " + meetingId + "\n" + "Meeting Password: " + meetingPassword;
        FirebaseDatabase.getInstance().getReference(CLIENT_TABLE).child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String token = snapshot.child("token").getValue().toString();
                    Log.e("Sender Token = ", token);
                    Utilities.sendFCMPush(iContext, token, meetingTitle, meetingMsg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public static void loadAppointmentRequests(String lawyerId, FirebaseActions actions) {
        ArrayList<Appointment> appointments = new ArrayList<>();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE);
        database.orderByChild("lawyerID").equalTo(lawyerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mSnap : snapshot.getChildren()) {

                    Appointment appointment = mSnap.getValue(Appointment.class);
                    appointments.add(appointment);
                }
                actions.onAppointmentRecordLoaded(appointments);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getClientData(String userId, FirebaseActions actions) {

    }

    public static void signUpUser(User client, Dialog progressBarView, FirebaseActions actions) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressBarView.show();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> { // generate uniqe token for messging for eache user
            String token = tokenTask.getResult();
            if (auth.getCurrentUser() == null) {
                auth.createUserWithEmailAndPassword(client.getEmailAddress(), client.getPassword()).addOnCompleteListener(signupTask -> {
                    if (signupTask.isSuccessful()) {
                        actions.onSignupComplete("Signup Successful");
                        client.setToken(token);
                        insertUserRecord(client);
                        progressBarView.dismiss();
                    } else
                        actions.onSignupComplete("Error" + signupTask.getException().getMessage());
                    progressBarView.dismiss();
                });
            }
        });

    }

    public static void sendAppointmentRequest(Appointment appointment, FirebaseActions action) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(APPOINTMENT_TABLE).child(appointment.getAppointmentId());
        checkIfAlreadyAppointmentBooked(appointment, new FirebaseActions() {
            @Override
            public void appointmentCheckComplete(boolean isAlreadyBooked) {
                if (isAlreadyBooked) {
                    action.onAppointmentError("Appointment Already Booked With this lawyer");
                } else {
                    checkIfLawyerAvailable(appointment, new FirebaseActions() {
                        @Override
                        public void lawyerAvailabilityStatus(String status, int code) {
                            if (code == 1) {// means lawyer is available
                                reference.setValue(appointment);
                                action.onAppointSendComplete();
                            } else if (code == 11) {
                                action.onAppointmentError("Lawyer Not available on selected Time");
                            }
                        }
                    });

                }
            }
        });
//
    }

    private static void checkIfLawyerAvailable(Appointment appointment, FirebaseActions actions) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).child(appointment.getLawyerID());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Lawyer lawyer = snapshot.getValue(Lawyer.class);
                assert lawyer != null;
                String[] workingDays = lawyer.getSchedule().getWorkingDays().split("-");
                Log.e("SECHULDE TABLE",getDayFromDate(appointment.getAppointmentDate()));
                Log.e("SCHULE = ",timeInRange(lawyer.getSchedule().getFromTime(),lawyer.getSchedule().getToTime(),appointment.getAppointmentDate())+"");

                if (Arrays.asList(workingDays).contains(getDayFromDate(appointment.getAppointmentDate())) && timeInRange(lawyer.getSchedule().getFromTime(), lawyer.getSchedule().getToTime(), appointment.getAppointmentDate())) {
                    checkIfLawyerAlreadyBookedWithOtherClient(appointment, lawyer, actions);
                } else {
                    actions.lawyerAvailabilityStatus("Not available", 11);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private static void checkIfLawyerAlreadyBookedWithOtherClient(Appointment appointment, Lawyer lawyer, FirebaseActions actions) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE);
        String id = "_" + lawyer.getEmailAddress().substring(0, lawyer.getEmailAddress().indexOf("@"));
        reference.orderByChild("lawyerID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Appointment foundAppointment = snapshot.getChildren().iterator().next().getValue(Appointment.class);
                    boolean result = checkTimeSpan(appointment.getAppointmentDate(), foundAppointment.getAppointmentDate());
                    if (result) { // if true means lawyer has an appointment with someone else on this time
                        actions.lawyerAvailabilityStatus("Not Available", 11);
                    } else {
                        actions.lawyerAvailabilityStatus("Available", 1);
                    }
                } else {
                    actions.lawyerAvailabilityStatus("avaiable", 1);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        actions.lawyerAvailabilityStatus("Lawyer not avaiable", 11);


    }

    private static boolean checkTimeSpan(String selectedDate, String alreadyBookedAppointmentDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US);
        try {

            Date sDate = sdf.parse(selectedDate);
            Date aDate = sdf.parse(alreadyBookedAppointmentDate);
            Date interVal1Date = new Date(aDate.getTime() - (60 * 60 * 1000));
            Date interval2Date = new Date(aDate.getTime() + (60 * 60 * 1000));
            DateTime start = new DateTime(interVal1Date);
            DateTime end = new DateTime(interval2Date);
            DateTime instance = new DateTime(sDate);
            Interval interval = new Interval(start, end);
            return interval.contains(instance);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean timeInRange(String lawyerFromTime, String lawyerToTime, String selectedAppointmentDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(Appointment.DATE_FORMAT, Locale.US);
        try {
            Date selectedTimeD = sdf.parse(selectedAppointmentDate);
            Date selectedTime = covertDateToTime(selectedTimeD);
            Date toTime = AmPmTo24(lawyerToTime);
            Date fromTime = AmPmTo24(lawyerFromTime);
            Log.e("result = ",""+(selectedTime.after(fromTime)));
            Log.e("Result 2 = ",""+(selectedTime.before(fromTime)));
            Log.e("time ranges",selectedTime.toString()+" =====  "+toTime);
            if (selectedTime.after(fromTime) && selectedTime.before(toTime)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;

    }

    private static Date covertDateToTime(Date d) throws ParseException {
        String time = new SimpleDateFormat("HH:mm", Locale.US).format(d);
        return new SimpleDateFormat("HH:mm", Locale.US).parse(time);
    }

    private static Date AmPmTo24(String time) throws ParseException {
        Date d = new SimpleDateFormat("KK:mm a", Locale.US).parse(time);
        String formatedTime = new SimpleDateFormat("HH:mm", Locale.US).format(d);
        return new SimpleDateFormat("HH:mm", Locale.US).parse(formatedTime);
    }

    private static String getDayFromDate(String date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE", Locale.US);
        try {
            Date d = df.parse(date);
            String day = df.format(d);
            Log.e("Formated Day =", day);
            return day;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void checkIfAlreadyAppointmentBooked(Appointment appointment, FirebaseActions action) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE);
        reference.orderByChild("appointmentId").equalTo(appointment.getAppointmentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    action.appointmentCheckComplete(true);
                } else {
                    action.appointmentCheckComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void updateLawyerSchedule(Schedule schedule, FirebaseActions actions) {
        User user = User.getCurrentLoggedInUser();
        String userId = "_" + user.getEmailAddress().substring(0, user.getEmailAddress().indexOf('@'));
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).child(userId).child("schedule");
        database.setValue(schedule);
        actions.onActionCompleted();

    }

    public static void loadAllUsers(ProgressDialog dialog, String userType, FirebaseActions actions) {
        dialog.show();
        ArrayList<User> users = new ArrayList<>();
        String table = userType.equals(User.TYPE_CLIENT) ? CLIENT_TABLE : LAWYER_TABLE;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(table);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnap : snapshot.getChildren()) {
                        User user = null;
                        if (userType.equals(User.TYPE_LAWYER))
                            user = childSnap.getValue(Lawyer.class);
                        else if(userType.equals(User.TYPE_CLIENT))
                            user = childSnap.getValue(Client.class);

                        if(user.getUserStatus()!= User.APPROVED)
                        users.add(user);
                    }

                }
                dialog.dismiss();
                actions.onUsersLoaded(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public static void approveUser(String userType,String userID){
        String userTable = userType.equals(User.TYPE_CLIENT)?CLIENT_TABLE:LAWYER_TABLE;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(userTable).child(userID);
        Log.e("Approving User",userID);
        ref.child("userStatus").setValue(User.APPROVED);
    }
    public static void findLawyers(String caseType, double startPrice, double endPrice, FirebaseActions actions) {
        ArrayList<User> lawyers = new ArrayList<>();
        Query reference = FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).orderByChild("practiceArea").equalTo(caseType);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        Lawyer lawyer = snapshot1.getValue(Lawyer.class);
                        if (lawyer.getEndPrice() <= endPrice && lawyer.getStartPrice() >= startPrice)
                            lawyers.add(lawyer);
                    }

                } else {
                    Log.e("Not Lawyer Found of ", caseType);
                }
                actions.onUserListLoaded(lawyers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static final void updateAppointmentStatus(String appointmentId, String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE).child(appointmentId);
        reference.child("appointmentStatus").setValue(status);

    }

    public static void listUsers(String userType, FirebaseActions actions) {
        String userTable = getUserTable(userType);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(userTable);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.exists()) {
                        if (userType.equals(User.TYPE_LAWYER)) {
                            actions.onUserFound(snap.getValue(Lawyer.class));
                        } else if (userType.equals(User.TYPE_CLIENT)) {
                            actions.onUserFound(snap.getValue(Client.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getUserInformation(String emailAddress, String userType, Dialog progressBarView, FirebaseActions actions) {
        progressBarView.show();
        String usertable = getUserTable(userType);
        String userKey = "_".concat(emailAddress.substring(0, emailAddress.indexOf('@')));
        Query queryRef = FirebaseDatabase.getInstance().getReference(usertable).orderByKey().equalTo(userKey);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                    if (userType.equals(User.TYPE_CLIENT)) {
                        actions.onUserFound(userSnap.getValue(Client.class));
                    } else {
                        actions.onUserFound(userSnap.getValue(Lawyer.class));
                    }
                    progressBarView.dismiss();
                } else {
                    actions.onUserFound(null);
                    progressBarView.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //    public static void LoginUser(String email,String password,String userType,FirebaseActions actionListener){
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if(task.isSuccessful()){
//                    findUser(email, userType, new FirebaseActions() {
//                        @Override
//                        public void onUserFound(User user) {
//                            actionListener.onUserLoggedIn(user);
//                        }
//                    });
//                }
//                else{
//                    actionListener.onUserFound();
//                }
//            }
//        });
//    }
    public static void updateUser(String userType, User user) {
        String userTable = userType.equals(User.TYPE_LAWYER) ? LAWYER_TABLE : CLIENT_TABLE;
        String userName = "_".concat(user.getEmailAddress().substring(0, user.getEmailAddress().indexOf('@')));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(userTable).child(userName);
        ref.setValue(user);
    }

    private static void insertUserRecord(User user) {
        String tableName = user instanceof Client ? CLIENT_TABLE : LAWYER_TABLE;
        String id = "_".concat(user.getEmailAddress().substring(0, user.getEmailAddress().indexOf("@")));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(tableName).child(id);
        reference.setValue(user);
//        reference.child("Phone_Number").setValue(user.getPhoneNumber());


    }

    public static void acceptCase(String caseId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE).child(caseId);
        reference.child("caseStatus").setValue(ClientCase.Active);

    }

    public static void rejectCase(String caseId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE).child(caseId);
        reference.child("caseStatus").setValue("Rejected");

    }

    public static void loadCases(String lawyerId, FirebaseActions actions) {
        ArrayList<ClientCase> caseArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE);
        reference.orderByChild("lawyerID").equalTo(lawyerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    ClientCase clientCase = childSnap.getValue(ClientCase.class);
                    if (clientCase != null) {
                        if (clientCase.getCaseStatus().equals(ClientCase.NOT_ACCEPTED))
                            caseArrayList.add(clientCase);
                    }
                }
                actions.onCasesLoaded(caseArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void loadClientsCases(String lawyerID, FirebaseActions actions) {
        ArrayList<ClientCase> caseArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE);
        reference.orderByChild("lawyerID").equalTo(lawyerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s1 : snapshot.getChildren()) {

                    ClientCase clientCase = s1.getValue(ClientCase.class);
                    ArrayList<Comment> caseComments = new ArrayList<>();
                    for (DataSnapshot s2 : s1.child("caseProgress").getChildren()) {
                        Comment comment = new Comment();
                        comment.setComment(s2.child("progressComment").getValue().toString());
                        comment.setDate(s2.child("Date").getValue().toString());
                        caseComments.add(comment);
                        clientCase.setCaseProgressComment(caseComments);
                    }


                    if (clientCase.getCaseStatus().equals(ClientCase.Active)) {
                        caseArrayList.add(clientCase);
                    }
                }
                actions.onCasesLoaded(caseArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void loadCasesByUser(String userId, FirebaseActions actions) {
        ArrayList<ClientCase> caseArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE);
        reference.orderByChild("clientID").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s1 : snapshot.getChildren()) {

                    ClientCase clientCase = s1.getValue(ClientCase.class);
                    ArrayList<Comment> caseComments = new ArrayList<>();
                    for (DataSnapshot s2 : s1.child("caseProgress").getChildren()) {
                        Comment comment = new Comment();
                        comment.setComment(s2.child("progressComment").getValue().toString());
                        comment.setDate(s2.child("Date").getValue().toString());
                        caseComments.add(comment);
                        clientCase.setCaseProgressComment(caseComments);
                    }


                    if (clientCase.getCaseStatus().equals(ClientCase.Active)) {
                        caseArrayList.add(clientCase);
                    }
                }
                actions.onCasesLoaded(caseArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getUserToken(String userId, FirebaseActions actions) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CLIENT_TABLE).child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.child("token").getValue().toString();
                actions.onUserTokenLoaded(token);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void updateCaseProgress(String caseId, String updateMessage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE).child(caseId);
        DatabaseReference progressReference = reference.child("caseProgress").push();
        progressReference.child("progressComment").setValue(updateMessage);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US);
        String currentDate = dateFormat.format(new Date());
        progressReference.child("Date").setValue(currentDate);

    }

    public static void markCompleteAppointment(String appointmentID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE).child(appointmentID);
        reference.child("appointmentStatus").setValue("Completed");
    }

    public static void cancelAppointment(String appointmentID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(APPOINTMENT_TABLE).child(appointmentID);
        reference.child("appointmentStatus").setValue("Canceled");
    }

    public static void sendCaseRequest(ClientCase clientCase, FirebaseActions actions) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(CASE_TABLE).child(clientCase.getClientID() + clientCase.getLawyerID());
        reference.setValue(clientCase).addOnCompleteListener(task -> actions.onActionCompleted());

    }

    public static void loadUser(String email, ProgressDialog dialog, FirebaseActions actions) {
        String userName = "_" + email.substring(0, email.indexOf("@"));
        dialog.show();
        getUserType(email, new FirebaseActions() {
            @Override
            public void onUserTypeFound(String userType) {
                if (userType == null) {
                    actions.onUserLoaded(null);
                    dialog.dismiss();
                    return;
                }
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users/" + userType).child(userName);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = null;
                            if (userType.equals(User.TYPE_CLIENT))
                                user = snapshot.getValue(Client.class);
                            else
                                user = snapshot.getValue(Lawyer.class);
                            actions.onUserLoaded(user);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Nothing found", error.getMessage());
                    }
                });
            }
        });

    }

    public static void getUserType(String emailAddress, FirebaseActions actions) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot tableSnaps : snapshot.getChildren()) {
                        boolean userFound = tableSnaps.child("_" + emailAddress.substring(0, emailAddress.indexOf("@"))).getValue() != null;
                        if (userFound) {
                            actions.onUserTypeFound(tableSnaps.getKey()); // user table name is user type
                            break;
                        }

                    }
                } else {
                    actions.onUserTypeFound(null);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private static String getUserTable(String userType) {
        if (userType.equals(User.TYPE_LAWYER)) {
            return LAWYER_TABLE;
        } else if (userType.equals(User.TYPE_CLIENT)) {
            return CLIENT_TABLE;
        }
        return CLIENT_TABLE;
    }

    public interface FirebaseActions {
        default void onUsersLoaded(ArrayList<User> users) {
        }

        default void onUserTokenLoaded(String token) {
        }

        default void onCasesLoaded(ArrayList<ClientCase> caseArrayList) {

        }

        default void onActionCompleted() {

        }

        default void lawyerAvailabilityStatus(String status, int statusCode) {
        }

        default void onAppointmentError(String error) {
        }

        default void onAppointSendComplete() {
        }

        default void appointmentCheckComplete(boolean isAlreadyBooked) {
        }

        default void onUserLoaded(User user) {

        }

        default void onUserTypeFound(String userType) {

        }

        default void onAppointmentRecordLoaded(ArrayList<Appointment> appointments) {
            Log.e("Appointment Loader", "Method Not Defenid");
        }

        default void onUserListLoaded(ArrayList<User> users) {
            Log.e("List Method: ", "Not Implemented");
        }

        default void onUserFound(User user) {
            Log.e("Search User Method", "Method Not Implemented");

        }

        default void onLogin(String status, User user) {

            Log.e("Login Method", "Method Not Implemented");
        }

        default void onSignupComplete(String status) {

            Log.e("Signup Method", "Method Not Implemented");
        }


    }
}

package com.fyp.lawyer_project.utils;

import android.app.Dialog;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.Schedule;
import com.fyp.lawyer_project.modal_classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseHelper {
    public static final String CLIENT_TABLE = "Users/Client";
    public static final String LAWYER_TABLE = "Users/Lawyer";

    public static void loginUser(String email, String password, String userType, Dialog progressBarView, FirebaseActions actions) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressBarView.show();
        if (auth.getCurrentUser() == null) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    getUserInformation(email, userType, progressBarView, new FirebaseActions() {
                        @Override
                        public void onUserFound(User user) {
                            actions.onLogin("Successful", user);
                            progressBarView.dismiss();
                        }
                    });

                } else {
                    actions.onLogin("Error " + task.getException().getMessage(), null);
                    progressBarView.dismiss();
                }
            });
        }
    }

    public static void signUpUser(User client, Dialog progressBarView, FirebaseActions actions) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressBarView.show();
        if (auth.getCurrentUser() == null) {
            auth.createUserWithEmailAndPassword(client.getEmailAddress(), client.getPassword()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    actions.onSignupComplete("Signup Successful");
                    insertUserRecord(client);
                    progressBarView.dismiss();
                } else
                    actions.onSignupComplete("Error" + task.getException().getMessage());
                progressBarView.dismiss();
            });
        }

    }

    public static void updateLawyerSchedule(Schedule schedule) {
        User user = User.getCurrentLoggedInUser();
        String userId = "_" + user.getEmailAddress().substring(0, user.getEmailAddress().indexOf('@'));
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).child(userId).child("schedule");
        database.setValue(schedule);

    }

    public static void findLawyers(String caseType, double startPrice,double endPrice,FirebaseActions actions) {
        ArrayList<User> lawyers = new ArrayList<>();
        Query reference = FirebaseDatabase.getInstance().getReference(LAWYER_TABLE).orderByChild("practiceArea").equalTo(caseType);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for(DataSnapshot snapshot1:snapshot.getChildren()) {

                        Lawyer lawyer = snapshot1.getValue(Lawyer.class);
                        if(lawyer.getEndPrice() <=endPrice && lawyer.getStartPrice() >= startPrice)
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


    private static String getUserTable(String userType) {
        if (userType.equals(User.TYPE_LAWYER)) {
            return LAWYER_TABLE;
        } else if (userType.equals(User.TYPE_CLIENT)) {
            return CLIENT_TABLE;
        }
        return CLIENT_TABLE;
    }

    public interface FirebaseActions {
        default void onUserListLoaded(ArrayList<User> users){
            Log.e("List Method: ","Not Implemented");
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

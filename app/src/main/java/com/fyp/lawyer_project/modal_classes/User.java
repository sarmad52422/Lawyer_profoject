package com.fyp.lawyer_project.modal_classes;

import androidx.annotation.Nullable;

import java.io.Serializable;

public  abstract class User{
    public static final String TYPE_LAWYER = "Lawyer";
    public static final String TYPE_CLIENT = "Client";

    public User(String emailAddress,String password, String userType) {
        this.userType = userType;
        this.emailAddress = emailAddress;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "User ID = "+getUserId()+"\n"+
                ", userStatus=" + userStatus +
                ", userType='" + userType + '\'' +
                ", fullName='" + fullName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    private String token;
    public static final int NOT_APPROVED = 403;
    public static final int APPROVED = 200;
    public String getToken() {
        return token;
    }
    private int userStatus = NOT_APPROVED;
    public void setToken(String token) {
        this.token = token;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserId(){
        return "_".concat(emailAddress.substring(0,emailAddress.indexOf('@')));
    }
    private String userType = "User";
    private static  User currentUser;
    private String fullName;
    private String emailAddress;
    private String password;
    private String phoneNumber;
    public User(String userType,String fullName, String emailAddress, String password, String phoneNumber) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public User(){}
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public static void setCurrentLoggedInUser(User user){
        currentUser = user;
    }
    public static  User getCurrentLoggedInUser(){
        return currentUser;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getLastName(){
        try {
            return fullName.split(" ")[1];
        }catch (Exception ex){
            return " ";
        }
    }
    public String getFirstName(){
        try {
            return fullName.split(" ")[0];
        }catch (Exception ex){
            return " ";
        }
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }
}

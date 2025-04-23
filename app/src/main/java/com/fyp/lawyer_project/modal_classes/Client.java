package com.fyp.lawyer_project.modal_classes;

import com.google.firebase.database.PropertyName;


public class Client extends User {
    @PropertyName("firstName")
    private String firstName;

    @PropertyName("lastName")
    private String lastName;

    @PropertyName("userId")
    private String userId;

    @PropertyName("phoneNumber")
    private String phoneNumber;

    @PropertyName("profileImageUrl")
    private String profileImageUrl;

    // Required for Firebase deserialization
    public Client() {
    }

    /**
     * Constructor for creating a Client instance.
     *
     * @param emailAddress    User's email address
     * @param userType        Type of user (e.g., "Client")
     * @param firstName       Client's first name
     * @param lastName        Client's last name
     * @param userId          Unique identifier for the client
     * @param phoneNumber     Client's phone number
     * @param profileImageUrl URL of the client's profile image
     */
    public Client(String emailAddress,String password ,String userType, String firstName, String lastName, String userId, String phoneNumber, String profileImageUrl) {
        super(emailAddress,password, userType);
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }
    public Client(String emailAddress,String password ,String userType, String firstName, String lastName, String userId, String phoneNumber) {
        super(emailAddress,password, userType);
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = "";
    }

    // Getters and setters with Firebase annotations
    @PropertyName("firstName")
    public String getFirstName() {
        return firstName != null ? firstName : "";
    }

    @PropertyName("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @PropertyName("lastName")
    public String getLastName() {
        return lastName != null ? lastName : "";
    }

    @PropertyName("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId != null ? userId : "";
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : "";
    }

    @PropertyName("phoneNumber")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @PropertyName("profileImageUrl")
    public String getProfileImageUrl() {
        return profileImageUrl != null ? profileImageUrl : "";
    }

    @PropertyName("profileImageUrl")
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }

    @Override
    public String toString() {
        return "Client{" +
                "emailAddress='" + getEmailAddress() + '\'' +
                ", userType='" + getUserType() + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userId='" + userId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
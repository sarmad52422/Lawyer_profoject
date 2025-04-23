package com.fyp.lawyer_project.modal_classes;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;

public class ClientCase {
    public static final String NOT_ACCEPTED = "Not Accepted";
    public static final String Active = "Active";
    public static final String CASE_PROGRESS = "Case Progress";

    @PropertyName("caseId")
    private String caseId;
    @PropertyName("caseTitle")
    private String caseTitle;
    @PropertyName("caseDetails")
    private String caseDetails;
    @PropertyName("lawyerId")
    private String lawyerID;
    @PropertyName("clientId")
    private String clientID;
    @PropertyName("caseStatus")
    private String caseStatus;
    @PropertyName("caseProgress")
    private ArrayList<Comment> caseProgress;
    @PropertyName("clientFeedBack")
    private String clientFeedBack;
    @PropertyName("lawyerComment")
    private String lawyerComment;
    @PropertyName("caseBudget")
    private double caseBudget;

    // Required for Firebase deserialization
    public ClientCase() {
    }

    // Constructor for case creation
    public ClientCase(String caseId, String caseTitle, String caseDetails, double caseBudget, String lawyerID, String clientID, String caseStatus) {
        this.caseId = caseId;
        this.caseTitle = caseTitle;
        this.caseDetails = caseDetails;
        this.caseBudget = caseBudget;
        this.lawyerID = lawyerID;
        this.clientID = clientID;
        this.caseStatus = caseStatus;
        this.caseProgress = new ArrayList<>();
        this.clientFeedBack = "";
        this.lawyerComment = "";
    }

    // Existing constructor
    public ClientCase(String caseTitle, String caseDetails, String lawyerID, String clientID, String caseStatus, ArrayList<Comment> caseProgressComment, String clientFeedBack, String lawyerComment, double caseBudget) {
        this.caseId = clientID.concat(lawyerID);
        this.caseTitle = caseTitle;
        this.caseDetails = caseDetails;
        this.lawyerID = lawyerID;
        this.clientID = clientID;
        this.caseStatus = caseStatus;
        this.caseProgress = caseProgressComment;
        this.clientFeedBack = clientFeedBack;
        this.lawyerComment = lawyerComment;
        this.caseBudget = caseBudget;

    }

    @PropertyName("caseId")
    public String getCaseId() {
        return caseId != null ? caseId : clientID.concat(lawyerID);
    }

    @PropertyName("caseId")
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @PropertyName("caseTitle")
    public String getCaseTitle() {
        return caseTitle;
    }

    @PropertyName("caseTitle")
    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    @PropertyName("caseDetails")
    public String getCaseDetails() {
        return caseDetails;
    }

    @PropertyName("caseDetails")
    public void setCaseDetails(String caseDetails) {
        this.caseDetails = caseDetails;
    }

    @PropertyName("lawyerId")
    public String getLawyerID() {
        return lawyerID;
    }

    @PropertyName("lawyerId")
    public void setLawyerID(String lawyerID) {
        this.lawyerID = lawyerID;
    }

    @PropertyName("clientId")
    public String getClientID() {
        return clientID;
    }

    @PropertyName("clientId")
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @PropertyName("caseStatus")
    public String getCaseStatus() {
        return caseStatus;
    }

    @PropertyName("caseStatus")
    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    @PropertyName("caseProgress")
    public ArrayList<Comment> getCaseProgressComment() {
        return caseProgress != null ? caseProgress : new ArrayList<>();
    }

    @PropertyName("caseProgress")
    public void setCaseProgressComment(ArrayList<Comment> caseProgress) {
        this.caseProgress = caseProgress;
    }

    @PropertyName("clientFeedBack")
    public String getClientFeedBack() {
        return clientFeedBack != null ? clientFeedBack : "";
    }

    @PropertyName("clientFeedBack")
    public void setClientFeedBack(String clientFeedBack) {
        this.clientFeedBack = clientFeedBack;
    }

    @PropertyName("lawyerComment")
    public String getLawyerComment() {
        return lawyerComment != null ? lawyerComment : "";
    }

    @PropertyName("lawyerComment")
    public void setLawyerComment(String lawyerComment) {
        this.lawyerComment = lawyerComment;
    }

    @PropertyName("caseBudget")
    public double getCaseBudget() {
        return caseBudget;
    }

    @PropertyName("caseBudget")
    public void setCaseBudget(double caseBudget) {
        this.caseBudget = caseBudget;
    }
}
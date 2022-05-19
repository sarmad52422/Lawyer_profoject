package com.fyp.lawyer_project.modal_classes;

public class ClientCase {
    private String caseTitle;
    private String caseDetails;
    private String lawyerID;
    private String clientID;
    private String caseStatus;
    private String caseProgress;
    private String clientFeedBack;
    private String lawyerComment;
    private double caseBudget;
    public static final String ACCEPTED = "Accepted";

    public ClientCase(String caseTitle, String caseDetails, String lawyerID, String clientID, String caseStatus, String caseProgress, String clientFeedBack, String lawyerComment,double caseBudget) {
        this.caseTitle = caseTitle;
        this.caseDetails = caseDetails;
        this.lawyerID = lawyerID;
        this.clientID = clientID;
        this.caseStatus = caseStatus;
        this.caseProgress = caseProgress;
        this.clientFeedBack = clientFeedBack;
        this.lawyerComment = lawyerComment;
        this.caseBudget = caseBudget;
    }
    public ClientCase(){

    }
    public String getCaseId(){
        return clientID.concat(lawyerID);
    }
    public double getCaseBudget() {
        return caseBudget;
    }

    public void setCaseBudget(double caseBudget) {
        this.caseBudget = caseBudget;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public void setCaseDetails(String caseDetails) {
        this.caseDetails = caseDetails;
    }

    public void setLawyerID(String lawyerID) {
        this.lawyerID = lawyerID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public void setCaseProgress(String caseProgress) {
        this.caseProgress = caseProgress;
    }

    public void setClientFeedBack(String clientFeedBack) {
        this.clientFeedBack = clientFeedBack;
    }

    public void setLawyerComment(String lawyerComment) {
        this.lawyerComment = lawyerComment;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public String getCaseDetails() {
        return caseDetails;
    }

    public String getLawyerID() {
        return lawyerID;
    }

    public String getClientID() {
        return clientID;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public String getCaseProgress() {
        return caseProgress;
    }

    public String getClientFeedBack() {
        return clientFeedBack;
    }

    public String getLawyerComment() {
        return lawyerComment;
    }
}

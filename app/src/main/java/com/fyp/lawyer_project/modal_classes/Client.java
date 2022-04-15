package com.fyp.lawyer_project.modal_classes;

public class Client extends User{
    private String caseType = "Farigh case";
    public Client(String fullName, String phoneNumber,String emailAddress, String password,String caseType) {
        super(fullName, emailAddress, password,phoneNumber);
        this.caseType = caseType;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public Client(){
        super("","","","");

    }
}

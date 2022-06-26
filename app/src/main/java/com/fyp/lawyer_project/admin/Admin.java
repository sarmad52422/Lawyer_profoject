package com.fyp.lawyer_project.admin;

public class Admin {
    public Admin(String adminID, String adminPassword) {
        this.adminID = adminID;
        this.adminPassword = adminPassword;
    }

    public String getAdminID() {
        return adminID;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    private String adminID;
    private String adminPassword;
}

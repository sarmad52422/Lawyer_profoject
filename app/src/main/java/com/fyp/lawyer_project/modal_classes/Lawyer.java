package com.fyp.lawyer_project.modal_classes;

public class Lawyer extends User {
    private String practiceArea = "Murder cases";
    private Schedule schedule;
    private double startPrice = 0.0d;
    private double endPrice = 0.0d;

    public Lawyer(String userType, Schedule schedule, String fullName, String emailAddress, String password, String phoneNumber, String practiceArea, double startPrice, double endPrice) {
        super(userType, fullName, emailAddress, password, phoneNumber);
        this.practiceArea = practiceArea;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        if (null == schedule)
            this.schedule = new Schedule("01:00 PM", "03:00 PM", "Monday-Tuesday-Sunday");
        else
            this.schedule = schedule;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public void setEndPrice(double endPrice) {
        this.endPrice = endPrice;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getEndPrice() {
        return endPrice;
    }

    public Lawyer() {
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getPracticeArea() {
        return practiceArea;
    }


    public void setPracticeArea(String practiceArea) {
        this.practiceArea = practiceArea;
    }
}

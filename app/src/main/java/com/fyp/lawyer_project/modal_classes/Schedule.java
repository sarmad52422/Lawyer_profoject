package com.fyp.lawyer_project.modal_classes;

public class Schedule {
    private String FromTime;
    private String ToTime;
    private String WorkingDays;
    public Schedule(){}
    public Schedule(String fromTime,String toTime,String workingDays){
        this.FromTime = fromTime;
        this.ToTime = toTime;
        this.WorkingDays = workingDays;
    }

    public void setFromTime(String fromTime) {
        FromTime = fromTime;
    }

    public void setToTime(String toTime) {
        ToTime = toTime;
    }

    public void setWorkingDays(String workingDays) {
        WorkingDays = workingDays;
    }

    public String getFromTime() {
        return FromTime;
    }

    public String getToTime() {
        return ToTime;
    }

    public String getWorkingDays() {
        return WorkingDays;
    }
}

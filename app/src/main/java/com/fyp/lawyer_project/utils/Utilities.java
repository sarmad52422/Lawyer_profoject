package com.fyp.lawyer_project.utils;

public class Utilities {
    public enum DAYS{

        MONDAY("Monday"),
        TUESDAY("Tuesday"),
        WEDNESDAY("Wednesday"),
        THURSDAY("Thursday"),
        FRIDAY("Friday"),
        SATURDAY("Saturday"),
        SUNDAY("Sunday");
        private final String day;
        private DAYS(String day){
            this.day = day;
        }
    }

}

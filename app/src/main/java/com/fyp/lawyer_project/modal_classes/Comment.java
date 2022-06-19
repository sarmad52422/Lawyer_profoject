package com.fyp.lawyer_project.modal_classes;

public class Comment {
    String comment;
    String date;

    public Comment(String comment, String date) {
        this.comment = comment;
        this.date = date;
    }
    @Override
    public String toString(){
        return "Comment = "+comment+"\n"+"Date = "+date;
    }
    public Comment(){}

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }
}

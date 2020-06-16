package com.madhuban.sahaayak;

public class Essen_issuePOJO {

    String userName;
    String userEmailId;
    String userMessage;
    //String userDeviceId;
    String time;
    String location;
    String issueId;
    String issueStatus;
    String isAdmin;
    String adminToken;


    public Essen_issuePOJO() {
    }

    public Essen_issuePOJO(String userName, String userEmailId, String userMessage,String time,String location,String issueId,String issueStatus,String isAdmin,String adminToken) {
        this.userName = userName;
        this.userEmailId = userEmailId;
        this.userMessage = userMessage;
        //this.userDeviceId = userDeviceId;
        this.time=time;
        this.location=location;
        this.issueId=issueId;
        this.issueStatus=issueStatus;
        this.isAdmin=isAdmin;
        this.adminToken=adminToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmailId() {
        return userEmailId;
    }

    public void setUserEmailId(String userEmailId) {
        this.userEmailId = userEmailId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

//    public String getUserDeviceId() {
//        return userDeviceId;
//    }
//
//    public void setUserDeviceId(String userDeviceId) {
//        this.userDeviceId = userDeviceId;
//    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.time = issueId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIssueStatus(String location) {
        this.issueStatus = issueStatus;
    }

    public String getIssueStatus() {
        return issueStatus;
    }

    public String getIsAdmin() {
        return isAdmin;
    }
    public void setIsAdmin(String location) {
        this.isAdmin = isAdmin;
    }

    public String getAdminToken() {
        return adminToken;
    }
    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }


}

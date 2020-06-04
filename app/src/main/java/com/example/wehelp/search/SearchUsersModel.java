package com.example.wehelp.search;


import com.google.firebase.Timestamp;

public class SearchUsersModel  {
    private String user_id;
    private String firstname;
    private String lastname;
    private String profile_image;
    private String contact, email;
    private boolean isAdmin;
    private Timestamp dob,datejoined;

    public SearchUsersModel(){}
    public SearchUsersModel(String user_id, String fname, String lname, String photo_url,String contact, String email, boolean isAdmin, Timestamp dob, Timestamp datejoined){
        this.firstname=fname;
        this.lastname=lname;
        this.user_id=user_id;
        this.profile_image=photo_url;
        this.datejoined=datejoined;
        this.dob=dob;
        this.email=email;
        this.contact=contact;
        this.isAdmin=isAdmin;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public Timestamp getDate_joined() {
        return datejoined;
    }

    public void setDate_joined(Timestamp date_joined) {
        this.datejoined = date_joined;
    }

    public Timestamp getDob() {
        return dob;
    }

    public void setDob(Timestamp dob) {
        this.dob = dob;
    }
}

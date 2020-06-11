package com.example.wehelp.categories;

import com.google.firebase.Timestamp;

public class Categorylist_model {
    private String description;
    private String category;
    private Timestamp dateadded;
    private String addedby;
    public Categorylist_model(){}

    public Categorylist_model(String description, String category) {
        this.description = description;
        this.category = category;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public String getAddedby() {
        return addedby;
    }

    public void setAddedby(String addedby) {
        this.addedby = addedby;
    }

    public Timestamp getDateadded() {
        return dateadded;
    }

    public void setDateadded(Timestamp dateadded) {
        this.dateadded = dateadded;
    }
}

package com.example.wehelp;

import java.util.Date;

public class PostsModel {
    private String category, photo_url, description, user_id;
    private Date date_added;

    public PostsModel(){}

    public PostsModel(String category, String photo_url, String description, String user_id, Date date_added) {
        this.category = category;
        this.photo_url = photo_url;
        this.description = description;
        this.user_id = user_id;
        this.date_added = date_added;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getDate_added() {
        return date_added;
    }

    public void setDate_added(Date date_added) {
        this.date_added = date_added;
    }


}

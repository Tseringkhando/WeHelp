package com.example.wehelp.categories;

public class Categorylist_model {
    private String description;
    private String category;
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

}

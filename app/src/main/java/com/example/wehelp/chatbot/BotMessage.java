package com.example.wehelp.chatbot;

import java.io.Serializable;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;

public class BotMessage implements Serializable {
    String id, message, url, title, description;
    Type type;
    public BotMessage() {
        this.type = Type.TEXT;
    }
    public BotMessage(RuntimeResponseGeneric r) {
        this.message = "";
        this.title = r.title();
        this.description = r.description();
        this.url = r.source();
        this.id = "2";
        this.type = Type.IMAGE;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUrl() {
        return url;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public enum Type {
        TEXT,
        IMAGE
    }
}

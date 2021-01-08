package com.adnan.photoblog;

import java.util.Date;

public class Comment {
    private String message;
    private  String user_id;
    private Date timestamp;
    private String blogPostId;

    public Comment() {
    }

    public Comment(String message, String user_id, Date timestamp) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public Comment(String message, String user_id, Date timestamp, String blogPostId) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.blogPostId = blogPostId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getBlogPostId() {
        return blogPostId;
    }

    public void setBlogPostId(String blogPostId) {
        this.blogPostId = blogPostId;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "message='" + message + '\'' +
                ", user_id='" + user_id + '\'' +
                ", timestamp=" + timestamp +
                ", blogPostId='" + blogPostId + '\'' +
                '}';
    }
}

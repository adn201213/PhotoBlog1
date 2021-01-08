package com.adnan.photoblog;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;


public class BlogPost extends BlogPostId {

  private String  user_id, desc, imageOriginalUri,   imageThumbUri;
   Date timestamp;

    public BlogPost() {
    }

    public BlogPost(String user_id, String desc, String imageOriginalUri, String imageThumbUri, Date timestamp) {
        this.user_id = user_id;
        this.desc = desc;
        this.imageOriginalUri = imageOriginalUri;
        this.imageThumbUri = imageThumbUri;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageOriginalUri() {
        return imageOriginalUri;
    }

    public void setImageOriginalUri(String imageOriginalUri) {
        this.imageOriginalUri = imageOriginalUri;
    }

    public String getImageThumbUri() {
        return imageThumbUri;
    }

    public void setImageThumbUri(String imageThumbUri) {
        this.imageThumbUri = imageThumbUri;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BlogPost{" +
                "user_id='" + user_id + '\'' +
                ", desc='" + desc + '\'' +
                ", imageOriginalUri='" + imageOriginalUri + '\'' +
                ", imageThumbUri='" + imageThumbUri + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

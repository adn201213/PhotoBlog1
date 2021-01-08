package com.adnan.photoblog;

public class BlogUser {
    private String username;
    private String userPhotoPath;

    public BlogUser() {
    }

    public BlogUser(String username, String userPhotoPath) {
        this.username = username;
        this.userPhotoPath = userPhotoPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPhotoPath() {
        return userPhotoPath;
    }

    public void setUserPhotoPath(String userPhotoPath) {
        this.userPhotoPath = userPhotoPath;
    }

    @Override
    public String toString() {
        return "BlogUser{" +
                "username='" + username + '\'' +
                ", userPhotoPath='" + userPhotoPath + '\'' +
                '}';
    }
}

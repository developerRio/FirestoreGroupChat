package com.originalstocks.groupchatfirebase;

import android.app.Application;

import com.originalstocks.groupchatfirebase.Models.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}

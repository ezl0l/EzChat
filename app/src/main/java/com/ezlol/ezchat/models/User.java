package com.ezlol.ezchat.models;

public class User {
    public int id;
    public String username;
    public String password_hash;
    public int time;

    public User(int id, String username, String password_hash, int time) {
        this.id = id;
        this.username = username;
        this.password_hash = password_hash;
        this.time = time;
    }
}

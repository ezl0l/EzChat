package com.ezlol.ezchat.models;

public class Friend {
    public int id;
    public int user_id;
    public int friend_id;
    public String status;
    public int time;

    public Friend(int id, int user_id, int friend_id, String status, int time) {
        this.id = id;
        this.user_id = user_id;
        this.friend_id = friend_id;
        this.status = status;
        this.time = time;
    }
}

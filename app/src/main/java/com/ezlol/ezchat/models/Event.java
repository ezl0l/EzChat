package com.ezlol.ezchat.models;

public class Event {
    public String type;
    public Message model;
    public Integer[] affected_users;
    public int time;

    public Event(String type, Message model, Integer[] affected_users, int time) {
        this.type = type;
        this.model = model;
        this.affected_users = affected_users;
        this.time = time;
    }
}

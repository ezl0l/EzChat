package com.ezlol.ezchat.models;

public class Event {
    public static final String MESSAGE_SEND = "message_send";
    public static final String MESSAGE_CHANGE_STATUS = "message_change_status";

    public String type;
    public Object model;
    public Integer[] affected_users;
    public int time;

    public Event(String type, Message model, Integer[] affected_users, int time) {
        this.type = type;
        this.model = model;
        this.affected_users = affected_users;
        this.time = time;
    }
}

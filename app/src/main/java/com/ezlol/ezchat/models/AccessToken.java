package com.ezlol.ezchat.models;

public class AccessToken {
    public int id;
    public String value;
    public int user_id;
    public String scopes;
    public int expired;
    public int time;

    public AccessToken(int id, String value, int user_id, String scopes, int expired, int time) {
        this.id = id;
        this.value = value;
        this.user_id = user_id;
        this.scopes = scopes;
        this.expired = expired;
        this.time = time;
    }
}

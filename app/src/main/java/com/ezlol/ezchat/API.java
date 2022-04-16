package com.ezlol.ezchat;

import android.util.Log;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Chat;
import com.ezlol.ezchat.models.Event;
import com.ezlol.ezchat.models.Friend;
import com.ezlol.ezchat.models.Message;
import com.ezlol.ezchat.models.User;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class API {
    static class AuthException extends Exception {}

    //public static final String SERVER_URL = "http://192.168.1.17:8083/";
    public static final String SERVER_URL = "http://85.12.218.165:8083/";

    AccessToken accessToken;
    Map<String, String> headers = new HashMap<>();

    private int lastErrorCode;

    public API(String username, String password) throws AuthException {
        accessToken = login(username, password);
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", accessToken.value);
    }

    public API(AccessToken accessToken) {
        this.accessToken = accessToken;
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", accessToken.value);
    }

    public static AccessToken login(String username, String password_hash) throws AuthException {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password_hash);

        Response response = Requests.post(SERVER_URL + "auth/login", new JSONObject(map).toString());
        if(response == null || response.getStatusCode() / 200 != 1)
            throw new AuthException();
        return new Gson().fromJson(response.toString(), AccessToken.class);
    }

    public User getUser(int user_id) {
        Response response = Requests.get(SERVER_URL + "user/" + user_id, headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), User.class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public boolean editProfile(User user) {
        Response response = Requests.post(SERVER_URL + "user/profile", new Gson().toJson(user, User.class));
        if(response == null)
            return false;
        if(response.getStatusCode() / 200 == 1)
            return true;

        lastErrorCode = response.getStatusCode();
        return false;
    }

    public Friend sendFriendRequest(int user_id) {
        JSONObject json = new JSONObject(new HashMap<String, Integer>(){{
            put("user_id", user_id);
        }});
        Response response = Requests.post(SERVER_URL + "friend", json.toString(), headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Friend.class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public boolean changeFriendRequest(int friend_id, String status) {
        Map<String, String> map = new HashMap<>();
        map.put("friend_id", String.valueOf(friend_id));
        map.put("status", status);

        Response response = Requests.put(SERVER_URL + "friend", new JSONObject(map).toString());
        if(response == null)
            return false;
        if(response.getStatusCode() / 200 == 1)
            return true;

        lastErrorCode = response.getStatusCode();
        return false;
    }

    public Friend getFriendRequest(int friend_id) {
        Response response = Requests.get(SERVER_URL + "friend/" + friend_id, headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Friend.class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Friend[] getFriends(int user_id) {
        Response response = Requests.get(SERVER_URL + "friends/" + user_id, headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Friend[].class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Chat getChat(int chat_id) {
        Response response = Requests.get(SERVER_URL + "chats/get/" + chat_id, headers);
        if(response == null)
            return null;

        Log.d("My", response.toString());
        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Chat.class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Chat createChat(Integer[] members_id) {
        Map<String, Integer[]> map = new HashMap<>();
        map.put("members", members_id);
        Response response = Requests.post(SERVER_URL + "chats/create", new JSONObject(map).toString());
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Chat.class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Chat[] getUserChats() {
        Response response = Requests.get(SERVER_URL + "chats/get", headers);
        if(response == null)
            return null;

        Log.d("My", response.toString());
        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Chat[].class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Message[] getMessages(int chat_id, int count, int offset) {
        Response response = Requests.get(SERVER_URL + "messages/get/" + chat_id + "/" + count + "/" + offset, headers);
        if(response == null)
            return null;

        Log.d("My", response.toString());
        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Message[].class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Message[] getMessages(int chat_id, int count) {
        return getMessages(chat_id, count, 0);
    }

    public Message[] getMessages(int chat_id) {
        return getMessages(chat_id, 20, 0);
    }

    public Message[] getMessagesFromChats(Integer[] chats_id, int count, int offset) {
        StringBuilder ids = new StringBuilder();
        for(Integer id : chats_id)
            ids.append(id);

        Response response = Requests.get(SERVER_URL + "messages/getMany/" + ids.substring(0, ids.length() - 1) + "/" + count + "/" + offset, headers);
        if(response == null)
            return null;

        Log.d("My", response.toString());
        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Message[].class);

        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Message[] getMessagesFromChats(Integer[] chats_id, int count) {
        return getMessagesFromChats(chats_id, count, 0);
    }

    public Message[] getMessagesFromChats(Integer[] chats_id) {
        return getMessagesFromChats(chats_id, 20, 0);
    }

    public Message sendMessage(Message message) {
        Response response = Requests.post(SERVER_URL + "message/send", new Gson().toJson(message, Message.class), headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Message.class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Message readMessage(Message message) {
        Response response = Requests.get(SERVER_URL + "message/read/" + message.id, headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Message.class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Message deleteMessage(Message message) {
        Response response = Requests.get(SERVER_URL + "message/delete/" + message.id, headers);
        if(response == null)
            return null;

        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Message.class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public Event[] events() {
        Response response = Requests.get(SERVER_URL + "event", headers);
        if(response == null)
            return null;

        Log.d("Content", response.toString());
        if(response.getStatusCode() / 200 == 1)
            return new Gson().fromJson(response.toString(), Event[].class);
        lastErrorCode = response.getStatusCode();
        return null;
    }

    public int getLastErrorCode() {
        return lastErrorCode;
    }
}

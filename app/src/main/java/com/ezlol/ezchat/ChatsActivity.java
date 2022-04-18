package com.ezlol.ezchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Chat;
import com.ezlol.ezchat.models.Event;
import com.ezlol.ezchat.models.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ChatsActivity extends AppCompatActivity {
    API api;

    LinearLayout chatsLayout;
    FloatingActionButton fab;

    BroadcastReceiver broadcastReceiver;

    Map<Chat, View> chatViewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        chatsLayout = findViewById(R.id.chats);
        fab = findViewById(R.id.fab);

        api = new API(new Gson().fromJson(getIntent().getExtras().getString("accessToken"),
                AccessToken.class));

        fab.setOnClickListener(view -> startActivity(new Intent(this, SearchActivity.class)
                .putExtra("accessToken",
                        new Gson().toJson(api.accessToken, AccessToken.class))));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Event[] events = new Gson().fromJson(intent.getExtras().getString("events"), Event[].class);
                for(Event event : events) {
                    switch(event.type) {
                        case "chat_create":
                            new ChatsTask().execute();
                            break;

                        case "message_send":
                            Gson gson = new Gson();
                            Message message = gson.fromJson(gson.toJson(event.model), Message.class);


                            Chat chat;
                            View view;
                            for(Map.Entry<Chat, View> entry : chatViewMap.entrySet()) {
                                chat = entry.getKey();
                                view = entry.getValue();

                                if(chat.id.equals(message.chat_id)) {
                                    chat.last_message = message;
                                    chatViewMap.replace(chat, chat.getView(ChatsActivity.this));
                                    break;
                                }
                            }
                            drawChats(chatViewMap.keySet().toArray(new Chat[0]));
                            break;
                    }
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter(LongPollService.BROADCAST_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void drawChats(Chat[] chats) {
        chatsLayout.removeAllViews();
        for(Chat chat : chats) {
            View view = chat.getView(ChatsActivity.this);
            view.setOnClickListener(view1 -> {
                startActivity(new Intent(ChatsActivity.this, DialogActivity.class)
                        .putExtra("accessToken", new Gson().toJson(api.accessToken, AccessToken.class))
                        .putExtra("peer_id", chat.id));
            });
            chatsLayout.addView(view);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ChatsTask().execute();
    }

    class ChatsTask extends AsyncTask<Void, Void, Chat[]> {
        @Override
        protected Chat[] doInBackground(Void... voids) {
            return api.getUserChats();
        }

        @Override
        protected void onPostExecute(Chat[] chats) {
            super.onPostExecute(chats);
            if(chats == null) {
                Log.e("ChatsTask", "Error");
                Snackbar.make(chatsLayout, "Couldn't get chats. Try again later.", Snackbar.LENGTH_LONG).show();
                return;
            }
            drawChats(chats);
        }
    }
}

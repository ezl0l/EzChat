package com.ezlol.ezchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Chat;
import com.ezlol.ezchat.models.Event;
import com.ezlol.ezchat.models.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class ChatsActivity extends AppCompatActivity {
    static boolean isShow = false;

    API api;

    LinearLayout chatsLayout;
    FloatingActionButton fab;

    ImageView logoutButton;

    BroadcastReceiver broadcastReceiver;

    Map<Chat, View> chatViewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        logoutButton = findViewById(R.id.logout_button);

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
                Gson gson = new Gson();

                Event[] events = new Gson().fromJson(intent.getExtras().getString("events"), Event[].class);
                for(Event event : events) {
                    switch(event.type) {
                        case "chat_create":
                            new ChatsTask().execute();
                            break;

                        case Event.MESSAGE_SEND:
                            Message message = gson.fromJson(gson.toJson(event.model), Message.class);

                            for(Chat chat : chatViewMap.keySet()) {
                                if(chat.id.equals(message.chat_id)) {
                                    chat.last_message = message;
                                    chatViewMap.replace(chat, chat.getView(ChatsActivity.this));
                                    break;
                                }
                            }
                            drawChats(chatViewMap.keySet().toArray(new Chat[0]));
                            break;

                        case Event.MESSAGE_CHANGE_STATUS:
                            message = gson.fromJson(gson.toJson(event.model), Message.class);
                            if(message.status.equals(Message.DELETED)) {
                                for (Chat chat : chatViewMap.keySet()) {
                                    if (chat.id.equals(message.chat_id)) {
                                        new ChatUpdateLastMsgTask(chat).execute();
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(LongPollService.BROADCAST_ACTION));

        logoutButton.setOnClickListener(view -> {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("accessToken", "")
                    .apply();
            startActivity(new Intent(this, StartActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isShow = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void drawChats(Chat[] chats) {
        chatsLayout.removeAllViews();
        chatViewMap.clear();
        for(Chat chat : chats) {
            View view = chat.getView(ChatsActivity.this);

            chatViewMap.put(chat, view);

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

    class ChatUpdateLastMsgTask extends AsyncTask<Void, Void, Message[]> {
        Chat chat;

        public ChatUpdateLastMsgTask(Chat chat) {
            this.chat = chat;
        }

        @Override
        protected Message[] doInBackground(Void... voids) {
            return api.getMessages(chat.id, 1);
        }

        @Override
        protected void onPostExecute(Message[] messages) {
            super.onPostExecute(messages);
            if(messages == null || messages.length == 0) return;
            chat.last_message = messages[0];
            chatViewMap.replace(chat, chat.getView(ChatsActivity.this));
        }
    }
}

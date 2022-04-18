package com.ezlol.ezchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Chat;
import com.ezlol.ezchat.models.Event;
import com.ezlol.ezchat.models.Message;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class DialogActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int CONTEXT_MENU_MESSAGE_DELETE = 1;
    public static final int CONTEXT_MENU_MESSAGE_EDIT = 2;

    API api;
    Chat chat;

    ScrollView scrollView;
    LinearLayout messagesLayout;

    EditText messageEditText;
    ImageView sendMessageButton, toolbarBack, toolbarDialogImage;

    RelativeLayout progressCircular;

    BroadcastReceiver broadcastReceiver;

    Map<View, Message> messageViewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        toolbarBack = findViewById(R.id.toolbar_back);
        toolbarDialogImage = findViewById(R.id.toolbar_dialog_image);

        scrollView = findViewById(R.id.scrollView);
        messagesLayout = findViewById(R.id.messages);

        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        progressCircular = findViewById(R.id.progress_circular);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Event[] events = new Gson().fromJson(intent.getExtras().getString("events"), Event[].class);
                Log.d("DialogActivity", "Received events count: " + events.length);
                for(Event event : events) {
                    if(event.type.startsWith("message")) {
                        new ChatMessagesTask().execute();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(LongPollService.BROADCAST_ACTION));

        toolbarBack.setOnClickListener((View v) -> finish());

        sendMessageButton.setOnClickListener(this);

        sendMessageButton.setEnabled(false);

        Bundle bundle = getIntent().getExtras();
        api = new API(new Gson().fromJson(bundle.getString("accessToken"), AccessToken.class));

        new ChatInfoTask().execute(bundle.getInt("peer_id"));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Message message = messageViewMap.get(v);
        if(message == null)
            return;

        menu.add(CONTEXT_MENU_MESSAGE_EDIT, message.id, message.chat_id, "Edit");
        menu.add(CONTEXT_MENU_MESSAGE_DELETE, message.id, message.chat_id, "Delete");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.d("ContextMenu", item.getItemId() + " " + item.getOrder() + " " + item.getGroupId());
        Message message = new Message(item.getItemId(), null, null, null, null, null);
        new DeleteMessageTask().execute(message);
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.sendMessageButton) {
            Message message = new Message(null, null, chat.id, messageEditText.getText().toString(), null, null);
            new SendMessageTask().execute(message);
        }
    }

    private void drawMessages(Message[] messages) {
        Log.d("DialogActivity.drawMessages", new Gson().toJson(messages, Message[].class));
        messagesLayout.removeAllViews();
        for(Message message : messages) {
            View view = message.getView(DialogActivity.this, message.user_id == api.accessToken.user_id);
            if(view != null) {
                messageViewMap.put(view, message);
                registerForContextMenu(view);
                messagesLayout.addView(view);
            }
        }
        scrollView.post(() -> {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            messagesLayout.setVisibility(View.VISIBLE);
        });
    }

    class DeleteMessageTask extends AsyncTask<Message, Void, Message> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Message doInBackground(Message... messages) {
            return api.deleteMessage(messages[0]);
        }

        @Override
        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            new ChatMessagesTask().execute();
        }
    }

    class SendMessageTask extends AsyncTask<Message, Void, Message> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            messageEditText.setText(null);
        }

        @Override
        protected Message doInBackground(Message... messages) {
            return api.sendMessage(messages[0]);
        }

        @Override
        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            Log.d("SendMessage", api.getLastErrorCode() + "");
            if(message != null) {
                View view = message.getView(DialogActivity.this, true);
                if(view != null) {
                    messageViewMap.put(view, message);
                    registerForContextMenu(view);

                    messagesLayout.addView(view);
                }
            }
        }
    }

    class ChatInfoTask extends AsyncTask<Integer, Void, Chat> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            messagesLayout.setVisibility(View.INVISIBLE);
            progressCircular.setVisibility(View.VISIBLE);
        }

        @Override
        protected Chat doInBackground(Integer... integers) {
            return api.getChat(integers[0]);
        }

        @Override
        protected void onPostExecute(Chat chat) {
            super.onPostExecute(chat);
            DialogActivity.this.chat = chat;

            sendMessageButton.setEnabled(true);

            Log.d("ChatInfoTask", new Gson().toJson(chat, Chat.class));
            new ChatMessagesTask().execute();
        }
    }

    class ChatMessagesTask extends AsyncTask<Void, Void, Message[]> {
        @Override
        protected Message[] doInBackground(Void... voids) {
            return api.getMessages(chat.id, 100, 0);
        }

        @Override
        protected void onPostExecute(Message[] messages) {
            super.onPostExecute(messages);
            progressCircular.setVisibility(View.GONE);
            if(messages == null)
                return;
            drawMessages(messages);
        }
    }
}
package com.ezlol.ezchat;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.RemoteInput;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Message;
import com.google.gson.Gson;

public class NotificationCallbackService extends Service {
    public static final String ACTION_REPLY = "ACTION_REPLY";

    API api;

    public NotificationCallbackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Gson gson = new Gson();

        api = new API(gson.fromJson(intent.getStringExtra("accessToken"), AccessToken.class));

        String action = intent.getAction();
        if(action != null) {
            switch(action) {
                case ACTION_REPLY:
                    int message_id = intent.getIntExtra("message_id", 0);

                    Bundle results = RemoteInput.getResultsFromIntent(intent);
                    if (results == null)
                        break;
                    String content = results.getCharSequence("content").toString();

                    if (message_id != 0 && content.length() > 0) {
                        new Thread(() -> {
                            Message message = api.readMessage(new Message(message_id,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null));
                            if (message == null)
                                return;

                            Message newMessage = new Message(null, null, message.chat_id,
                                    content, null, null);

                            api.sendMessage(newMessage);

                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancel(message.id);
                        }).start();
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }
}
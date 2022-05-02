package com.ezlol.ezchat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.RemoteInput;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Event;
import com.ezlol.ezchat.models.Message;
import com.ezlol.ezchat.models.User;
import com.google.gson.Gson;

public class LongPollService extends Service {
    public static final String BROADCAST_ACTION = "com.ezl0l.ezchat.action.longpoll";

    ServiceThread serviceThread;
    API api;

    private int failedConnectionsCounter = 0;

    public LongPollService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String accessTokenJson = intent.getExtras().getString("accessToken");
        if(accessTokenJson == null)
            accessTokenJson = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("accessToken", null);
        api = new API(new Gson().fromJson(accessTokenJson, AccessToken.class));

        Notification notification =
                new Notification.Builder(this, StartActivity.TRAY_NOTIFICATION_CHANNEL)
                        .setContentTitle("EzChat")
                        .setContentText("Search for new messages")
                        .build();


        startForeground(101, notification);

        serviceThread = new ServiceThread();
        serviceThread.start();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "I was born right now!", Toast.LENGTH_LONG).show();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "I'm dying...", Toast.LENGTH_LONG).show();
        if(serviceThread != null)
            serviceThread.end();
        super.onDestroy();
    }

    class ServiceThread extends Thread {
        private boolean isWork = true;

        @Override
        public void run() {
            Intent intent;
            while(isWork) {
                Event[] events = api.events();
                if (events == null) {
                    Log.e("ServiceThread", "Events is null");
                    Log.e("ServiceThread", "Last error: " + api.getLastErrorCode());

                    failedConnectionsCounter++;
                    try {
                        sleep(failedConnectionsCounter < 3 ? 1000 : 30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                failedConnectionsCounter = 0;

                Log.d("ServiceThread", "Events count: " + events.length);

                if(events.length == 0)
                    continue;

                intent = new Intent(BROADCAST_ACTION)
                        .putExtra("events", new Gson().toJson(events, Event[].class));
                sendBroadcast(intent);

                Log.d("Show", "Chats: " + ChatsActivity.isShow + " Dialog: " + DialogActivity.isShow);

                if(ChatsActivity.isShow || DialogActivity.isShow)
                    continue;

                for (Event event : events) {
                    if(!event.type.equals("message_send"))
                        continue;

                    Gson gson = new Gson();

                    Message message = gson.fromJson(gson.toJson(event.model), Message.class);
                    if(message == null)
                        continue;
                    if(message.user_id == api.accessToken.user_id)
                        continue;

                    Log.d("Message", gson.toJson(message, Message.class));

                    User user = api.getUser(message.user_id);
                    if(user == null)
                        continue;

                    Intent replyIntent = new Intent(LongPollService.this, NotificationCallbackService.class);
                    replyIntent.setAction(NotificationCallbackService.ACTION_REPLY);
                    replyIntent.putExtra("accessToken", new Gson().toJson(api.accessToken, AccessToken.class));
                    replyIntent.putExtra("message_id", message.id);

                    @SuppressLint("UnspecifiedImmutableFlag") PendingIntent replyPendingIntent =
                            PendingIntent.getService(getApplicationContext(),
                                    message.id, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    RemoteInput remoteInput = new RemoteInput.Builder("content")
                            .setLabel("Type message")
                            .build();

                    NotificationCompat.Action action =
                            new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                                    "Reply", replyPendingIntent)
                                    .addRemoteInput(remoteInput)
                                    .build();

                    Intent readIntent = new Intent(LongPollService.this, NotificationCallbackService.class);
                    readIntent.setAction(NotificationCallbackService.ACTION_READ);
                    readIntent.putExtra("accessToken", new Gson().toJson(api.accessToken, AccessToken.class));
                    readIntent.putExtra("message_id", message.id);

                    @SuppressLint("UnspecifiedImmutableFlag") PendingIntent readPendingIntent =
                            PendingIntent.getService(getApplicationContext(),
                                    message.id, readIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent contentIntent = new Intent(getApplicationContext(), DialogActivity.class)
                            .putExtra("accessToken", gson.toJson(api.accessToken, AccessToken.class))
                            .putExtra("peer_id", message.chat_id);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addParentStack(DialogActivity.class);
                    stackBuilder.addNextIntent(contentIntent);

                    PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification =
                            new NotificationCompat.Builder(LongPollService.this, "CHANNEL")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setAutoCancel(true)
                                    .setContentTitle(user.username)
                                    .setContentInfo(user.username)
                                    .setContentText(message.content)
                                    .setContentIntent(contentPendingIntent)
                                    .addAction(0, "Read", readPendingIntent)
                                    .addAction(action)
                            .build();

                    NotificationCompat.MessagingStyle.Message message1 =
                            new NotificationCompat.MessagingStyle.Message(
                                    message.content,
                                    message.time,
                                    user.username);


                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(message.chat_id, notification);
                }
            }
        }

        public void end() {
            Toast.makeText(LongPollService.this, "Thread stopped", Toast.LENGTH_LONG).show();
            isWork = false;
        }
    }
}
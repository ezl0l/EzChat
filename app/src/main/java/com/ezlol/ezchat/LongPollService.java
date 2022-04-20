package com.ezlol.ezchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.RemoteInput;
import android.app.Service;
import android.content.Intent;
import android.location.GnssAntennaInfo;
import android.os.IBinder;
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
    public static final String REPLY_ACTION = "com.ezl0l.ezchat.action.reply";

    ServiceThread serviceThread;
    API api;

    public LongPollService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        api = new API(new Gson().fromJson(intent.getExtras().getString("accessToken"), AccessToken.class));

        serviceThread = new ServiceThread();
        serviceThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
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
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Log.d("ServiceThread", "Events count: " + events.length);
                intent = new Intent(BROADCAST_ACTION)
                        .putExtra("events", new Gson().toJson(events, Event[].class));
                sendBroadcast(intent);

                if(ChatsActivity.isShow)
                    continue;

                for (Event event : events) {
                    if(!event.type.equals("message_send"))
                        continue;

                    Message message = (Message) event.model;
                    if(message == null)
                        continue;
                    if(message.user_id == api.accessToken.user_id)
                        continue;

                    User user = api.getUser(message.user_id);
                    if(user == null)
                        continue;

                    intent = new Intent(LongPollService.this, NotificationCallbackService.class);
                    intent.putExtra("accessToken", new Gson().toJson(api.accessToken, AccessToken.class));
                    intent.setAction(NotificationCallbackService.ACTION_REPLY);

                    PendingIntent replyPendingIntent =
                            PendingIntent.getService(getApplicationContext(),
                                    message.id, intent, PendingIntent.FLAG_IMMUTABLE);

                    RemoteInput remoteInput = new RemoteInput.Builder("content")
                            .setLabel("Type message")
                            .build();

                    NotificationCompat.Action action =
                            new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                                    "Reply", replyPendingIntent)
                                    .addRemoteInput(remoteInput)
                                    .build();

                    Notification notification =
                            new NotificationCompat.Builder(LongPollService.this, "CHANNEL")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(user.username)
                                    .setContentInfo(user.username)
                                    .setContentText(message.content)
                                    .addAction(action)
                            .build();

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(message.id, notification);
                }
            }
        }

        public void end() {
            isWork = false;
        }
    }
}
package com.ezlol.ezchat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ezlol.ezchat.models.AccessToken;
import com.ezlol.ezchat.models.Event;
import com.google.gson.Gson;

public class LongPollService extends Service {
    public static final String BROADCAST_ACTION = "com.ezl0l.ezchat.action.longpoll";

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

        serviceThread = new ServiceThread(startId);
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
        int startId;
        private boolean isWork = true;

        public ServiceThread(int startId) {
            this.startId = startId;
        }

        @Override
        public void run() {
            Intent intent;
            while(isWork) {
                Event[] events = api.events();
                if(events == null) {
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
            }
        }

        public void end() {
            isWork = false;
        }
    }
}
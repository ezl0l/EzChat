package com.ezlol.ezchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.ezlol.ezchat.models.AccessToken;
import com.google.gson.Gson;

public class AutoloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            return;

        String accessTokenJson = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("accessToken", null);
        if(accessTokenJson != null) {
            AccessToken accessToken = new Gson().fromJson(accessTokenJson, AccessToken.class);
            if (accessToken != null)
                context.startService(new Intent(context, LongPollService.class)
                        .putExtra("accessToken", accessTokenJson));
        }
    }
}
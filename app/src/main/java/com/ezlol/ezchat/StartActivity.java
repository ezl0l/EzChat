package com.ezlol.ezchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.ezlol.ezchat.models.AccessToken;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String DEFAULT_NOTIFICATION_CHANNEL = "DEFAULT_NOTIFICATION_CHANNEL";
    public static final String TRAY_NOTIFICATION_CHANNEL = "TRAY_NOTIFICATION_CHANNEL";

    EditText usernameEditText, passwordEditText;
    ProgressBar loginProgressBar;
    Button loginButton;

    API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        NotificationChannel channel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL,
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationManager.createNotificationChannel(new NotificationChannel(TRAY_NOTIFICATION_CHANNEL,
                "Tray",
                NotificationManager.IMPORTANCE_NONE));

        String accessTokenJson = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("accessToken", null);
        if(accessTokenJson != null) {
            AccessToken accessToken = new Gson().fromJson(accessTokenJson, AccessToken.class);
            if(accessToken != null) {
                startService(new Intent(this, LongPollService.class)
                        .putExtra("accessToken", accessTokenJson));

                Intent intent = new Intent(this, ChatsActivity.class);
                intent.putExtra("accessToken", accessTokenJson);
                startActivity(intent);

                finish();
            }
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        loginButton = findViewById(R.id.loginButton);

        loginProgressBar = findViewById(R.id.loginProgressBar);

        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.loginButton) {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if(username.length() > 0 && password.length() > 0) {
                new LoginTask().execute();
            } else {
                Snackbar.make(loginButton, "Please fill all fields ;/", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginButton.setEnabled(false);
            loginProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                api = new API(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                return true;
            } catch (API.AuthException ignored) {}
            return false;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            loginButton.setEnabled(true);
            loginProgressBar.setVisibility(View.GONE);
            passwordEditText.setText(null);

            if(!bool) {
                Snackbar.make(loginButton, "Log in failed. Check credentials.", Snackbar.LENGTH_LONG).show();
                return;
            }

            String accessTokenString = new Gson().toJson(api.accessToken);

            PreferenceManager.getDefaultSharedPreferences(StartActivity.this)
                    .edit()
                    .putString("accessToken", accessTokenString)
                    .apply();

            Intent intent = new Intent(StartActivity.this, ChatsActivity.class);
            intent.putExtra("accessToken", accessTokenString);
            startActivity(intent);

            finish();
        }
    }
}
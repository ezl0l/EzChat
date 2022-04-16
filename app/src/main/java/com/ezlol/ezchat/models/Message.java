package com.ezlol.ezchat.models;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ezlol.ezchat.R;

public class Message {
    public Integer id;
    public Integer user_id;
    public Integer chat_id;
    public String content;
    public String status;
    public Integer time;

    public Message(Integer id, Integer user_id, Integer chat_id, String content, String status, Integer time) {
        this.id = id;
        this.user_id = user_id;
        this.chat_id = chat_id;
        this.content = content;
        this.status = status;
        this.time = time;
    }

    public View getView(Context context, boolean isFromThisUser) {
        if(status.equals("DELETED"))
            return null;

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) context.getResources()
                .getDimension(R.dimen.messageLayoutMarginTop));
        layout.setLayoutParams(params);
        layout.setGravity(isFromThisUser ? Gravity.END : Gravity.START);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView contentTextView = new TextView(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(isFromThisUser ? 0 : 10, (int) context.getResources()
                .getDimension(R.dimen.messageContentMarginTop), isFromThisUser ? 10 : 0, 0);
        contentTextView.setLayoutParams(params1);

        contentTextView.setText(content);
        contentTextView.setTextColor(Color.BLACK);

        contentTextView.setBackgroundResource(R.drawable.rounded_corner);

        int padding = (int) context.getResources()
                .getDimension(R.dimen.messageContentPadding);
        contentTextView.setPadding(padding, padding, padding, padding);

        ImageView avatarImageView = new ImageView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.width = (int) context.getResources()
                .getDimension(R.dimen.messageAvatarSize);
        params2.height = (int) context.getResources()
                .getDimension(R.dimen.messageAvatarSize);
        params2.gravity = Gravity.BOTTOM;
        avatarImageView.setLayoutParams(params2);
        avatarImageView.setImageResource(R.mipmap.ic_launcher); // todo

        if(isFromThisUser) {
            layout.addView(contentTextView);
            layout.addView(avatarImageView);
        } else {
            layout.addView(avatarImageView);
            layout.addView(contentTextView);
        }

        return layout;
    }
}

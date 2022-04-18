package com.ezlol.ezchat.models;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezlol.ezchat.R;
import com.ezlol.ezchat.Utils;

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
        if(status == null || status.equals("DELETED"))
            return null;

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) context.getResources()
                .getDimension(R.dimen.messageLayoutMarginTop));
        params.gravity = isFromThisUser ? Gravity.END : Gravity.START;
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout secondLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.weight = 1;
        params1.setMargins(isFromThisUser ? 0 : 10, (int) context.getResources()
                .getDimension(R.dimen.messageContentMarginTop), isFromThisUser ? 10 : 0, 0);
        secondLayout.setLayoutParams(params1);
        secondLayout.setBackgroundResource(R.drawable.rounded_corner);

        TextView contentTextView = new TextView(context);
        LinearLayout.LayoutParams contentTextViewParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        contentTextViewParams.weight = 1;
        contentTextView.setLayoutParams(contentTextViewParams);

        contentTextView.setText(content);
        contentTextView.setTextColor(Color.BLACK);

        int padding = (int) context.getResources()
                .getDimension(R.dimen.messageContentPadding);
        contentTextView.setPadding(padding, padding, padding, padding);

        TextView timeTextView = new TextView(context);
        LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        linParams.gravity = Gravity.BOTTOM | Gravity.END;
        linParams.setMargins(0, 0, (int) context.getResources()
                .getDimension(R.dimen.messageTimeTextMarginRight),
                (int) context.getResources()
                .getDimension(R.dimen.messageTimeTextMarginBottom));
        timeTextView.setLayoutParams(linParams);

        timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) context.getResources()
                .getDimension(R.dimen.messageTimeTextSize));
        timeTextView.setText(Utils.timestampToDatetime(time));

        secondLayout.addView(contentTextView);
        secondLayout.addView(timeTextView);

        ImageView avatarImageView = new ImageView(context);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.width = (int) context.getResources()
                .getDimension(R.dimen.messageAvatarSize);
        params3.height = (int) context.getResources()
                .getDimension(R.dimen.messageAvatarSize);
        params3.gravity = Gravity.BOTTOM;
        avatarImageView.setLayoutParams(params3);
        avatarImageView.setImageResource(R.mipmap.ic_launcher); // todo

        if(isFromThisUser) {
            layout.addView(secondLayout);
            layout.addView(avatarImageView);
        } else {
            layout.addView(avatarImageView);
            layout.addView(secondLayout);
        }

        return layout;
    }
}

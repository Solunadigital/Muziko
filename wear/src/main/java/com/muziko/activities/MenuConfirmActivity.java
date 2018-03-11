package com.muziko.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.MuzikoWearApp;
import com.muziko.R;
import com.muziko.common.events.buswear.WearActionEvent;

import pl.tajchert.buswear.EventBus;

public class MenuConfirmActivity extends Activity implements View.OnClickListener {

    private TextView messageText;
    private ImageView cancelButton;
    private ImageView confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_confirm);
        findViewsById();

        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);

        String message = getIntent().getStringExtra("message");
        messageText.setText(message);
    }

    private void findViewsById() {
        messageText = (TextView) findViewById(R.id.messageText);
        cancelButton = (ImageView) findViewById(R.id.cancelButton);
        confirmButton = (ImageView) findViewById(R.id.confirmButton);
    }

    @Override
    public void onClick(View v) {
        if (v == cancelButton) {
            finish();
        } else if (v == confirmButton) {
            EventBus.getDefault(MenuConfirmActivity.this).postRemote(new WearActionEvent(MuzikoWearApp.wearAction, MuzikoWearApp.wearPosition));
            finish();
        }
    }
}

package com.eyalin.snakes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    static final String tag = "WelcomeActivity";
    static final String BUNDLE_KEY = "welcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onSinglePlayerClicked(View view) {
        Intent intent = new Intent(WelcomeActivity.this, GameActivity.class);
        startActivity(intent);
    }

    public void onMultiPlayerClicked(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}

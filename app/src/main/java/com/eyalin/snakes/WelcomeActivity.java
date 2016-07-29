package com.eyalin.snakes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    private final String PLAYER = "Player";

    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    private void startGameSingleMode() {

    }

    protected void onSinglePlayerClicked(View view) {
        final EditText NAME = new EditText(this);
        NAME.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        AlertDialog.Builder nameDlgBld = new AlertDialog.Builder(this);
        nameDlgBld.setTitle(R.string.new_game).setMessage(R.string.name);
        nameDlgBld.setView(NAME);
        nameDlgBld.setPositiveButton(R.string.ok_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerName = NAME.getText().toString();
                        if (playerName.trim().isEmpty());
                            playerName = PLAYER;
                        startGameSingleMode();
                    }
                })
                .setNegativeButton(R.string.cancel_btn,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast toast = Toast.makeText(WelcomeActivity.this,
                                        R.string.cancel_msg, Toast.LENGTH_SHORT);
                            }
                        });
    }

}

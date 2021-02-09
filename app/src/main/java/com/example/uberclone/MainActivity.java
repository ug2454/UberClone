package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    Switch userSwitchType;
    Button getStartedButton;

    public void redirectActivity() {
        try {
            if (ParseUser.getCurrentUser().get("riderOrDriver").equals("Rider")) {
                Intent in = new Intent(this, RiderActivity.class);
                finish();
                startActivity(in);
            } else {
                System.out.println("Driver activity");
                Intent in = new Intent(this, RequestListActivity.class);
                finish();
                startActivity(in);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void getStarted(View view) {
        String userType = "Rider";
        if (userSwitchType.isChecked()) {
            System.out.println("Driver");
            userType = "Driver";
        }
        ParseUser.getCurrentUser().put("riderOrDriver", userType);
        ParseUser.getCurrentUser().saveInBackground(e -> {
            if (e == null) {
                System.out.println("Saved");
                redirectActivity();
            } else {
                System.out.println(e.getMessage());
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userSwitchType = findViewById(R.id.switchToggle);
        getStartedButton = findViewById(R.id.getStartedButton);
        Log.i("switch value", String.valueOf(userSwitchType.isChecked()));

//        ParseUser.logOut();
        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn((user, e) -> {
                if (e == null) {
                    System.out.println("Anonymous login successful");
                    System.out.println(user.getSessionToken());
                } else {
                    System.out.println("Anonymous login failed");
                }
            });
        } else {
            System.out.println("user is not null");
            System.out.println(ParseUser.getCurrentUser().get("riderOrDriver"));
            if (ParseUser.getCurrentUser().get("riderOrDriver") != null) {
                System.out.println(ParseUser.getCurrentUser().get("riderOrDriver"));
                redirectActivity();
            }
        }


    }


}
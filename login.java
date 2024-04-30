package com.example.welcome_screen;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
public class login extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_screen);
        Intent intent = getIntent();
    }

    public void homeScreen(View view) {
        Intent sendToHomeScreen = new Intent(this, FragmentMain.class);
        startActivity(sendToHomeScreen);
    }
}
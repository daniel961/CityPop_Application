package com.citypop.hit.citypop;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class SplashScreen extends AppCompatActivity {

    Animation downtoup;
    LinearLayout CityPopSymbolLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //REFS
        CityPopSymbolLayout = (LinearLayout) findViewById(R.id.CityPopSymbolLayout);
        downtoup = AnimationUtils.loadAnimation(this,R.anim.downtoup);
        CityPopSymbolLayout.setAnimation(downtoup);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                //Create an Intent that will start the Menu-Activity.

                Intent mainIntent = new Intent(SplashScreen.this, LoginScreen.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();

            }
        }, 4000);




    }




}



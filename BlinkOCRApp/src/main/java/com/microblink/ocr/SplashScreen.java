package com.microblink.ocr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by currys on 7/17/2015.
 */
public class SplashScreen extends Activity implements LoadingTask.LoadingTaskFinishedListener {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 9999;
    public static String MyPREFERENCES;

    // Variable to store app opening time so that messages after the time are read later
    SharedPreferences openTime;


    public CellLocation getMyLocation()
    {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getCellLocation();
    }
    public String getMyOperator()
    {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getNetworkOperatorName();
    }

    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Find the progress bar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //Find the text view to display notice
        TextView notice = (TextView) findViewById(R.id.textView);

        progressBar.setProgress(0);

        try {
            /*PendingIntent piSent=PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
            PendingIntent piDelivered=PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);*/

            /* Getting SIM information*/
            TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);

            boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
            boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

            if(isSIM1Ready && isSIM1Ready)
                Toast.makeText(this,"Both SIM Detected"+getMyOperator(), Toast.LENGTH_LONG).show();
            else if(isSIM1Ready && !isSIM2Ready)
                Toast.makeText(this,"SIM 1 Detected : "+getMyOperator(),Toast.LENGTH_LONG).show();
            else if(!isSIM1Ready && isSIM2Ready)
                Toast.makeText(this,"SIM 2 Detected",Toast.LENGTH_LONG).show();

            boolean isDualSIM = telephonyInfo.isDualSIM();

            //notice.setText("Sending SMS to NCELL");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("90011", null, " ", null, null);
            //Toast.makeText(getApplicationContext(), "Requesting Balance Information", Toast.LENGTH_LONG).show();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                TextView notice = (TextView) findViewById(R.id.textView);
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    //notice.setText("SMS Sent!!!! Waiting for Reply");
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setProgress(25);

                    openTime = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_APPEND);
                    SharedPreferences.Editor editor = openTime.edit();
                    editor.putLong("Time",1010101010);
                    editor.commit();
                }
            }, 999);
            notice.setText("");
            progressBar.setProgress(50);
            progressBar.setProgress(75);
        }

        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setProgress(100);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
    // This is the callback for when your async task has finished
    @Override
    public void onTaskFinished() {
        completeSplash();
    }

    private void completeSplash(){
        startApp();
        finish(); // Don't forget to finish this Splash Activity so the user can't return to it!
    }

    private void startApp() {

        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
    }
}

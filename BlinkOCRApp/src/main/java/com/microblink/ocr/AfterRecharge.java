package com.microblink.ocr;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class AfterRecharge extends ActionBarActivity {
    private ProgressBar spinner;
    private TextView tv_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_recharge);
        spinner = (ProgressBar)findViewById(R.id.progressBar2);
        tv_message =(TextView)findViewById(R.id.tv_message);

        spinner.setVisibility(View.VISIBLE);

        Uri inboxURI = Uri.parse("content://sms/inbox");
        // List required columns
        String[] reqCols = new String[] { "_id", "address", "body" };
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null,"read = 0", null, "date desc limit 3");

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            int index_Address = cursor.getColumnIndex("address");
            String strAddress = cursor.getString(index_Address);
            int index_Body = cursor.getColumnIndex("body");
            String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
            if (strAddress.equals("Ncell")) {
                spinner.setProgress(100);
                spinner.setVisibility(View.GONE);
                tv_message.setText(tv_message.getText() + "\n" + cursor.getString(index_Body));
                try {
                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Cannot mark the message as read", Toast.LENGTH_LONG).show();
                }


            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_after_recharge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.microblink.ocr;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.activity.BlinkOCRActivity;
import com.microblink.help.HelpActivity;
import com.microblink.recognizers.ocr.blinkocr.parser.generic.AmountParserSettings;
import com.microblink.recognizers.ocr.blinkocr.parser.generic.IbanParserSettings;

import java.util.Date;


public class MainActivity extends Activity {

    private static final int BLINK_OCR_REQUEST_CODE = 100;
    private ProgressBar spinner;
    public static String MyPREFERENCES;
    // Cursor Adapter
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv_message;
        tv_message = (TextView)findViewById(R.id.tv_message);
        //Find the spinner
        spinner = (ProgressBar)findViewById(R.id.progressBarSpinner);
        //set spinner visible till the balance information is not retrieved
        spinner.setVisibility(View.VISIBLE);
        // Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/inbox");
        // List required columns
        String[] reqCols = new String[] { "_id", "address", "body" };
        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null,"read = 0", null, "date desc limit 3");

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                int index_Date = cursor.getColumnIndex("date");
                int index_Body = cursor.getColumnIndex("body");
                int index_Address = cursor.getColumnIndex("address");
                int messageId = cursor.getColumnIndex("_id");
                long longDate = cursor.getLong(index_Date);
                Date sysDate = new Date();
                Date msgDate = new Date(longDate);
                String msgStatus = cursor.getString(cursor.getColumnIndex("status"));
                String strAddress = cursor.getString(index_Address);
                String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                if(strAddress.equals("Ncell")){
                    spinner.setProgress(100);
                    spinner.setVisibility(View.GONE);
                    tv_message.setText(tv_message.getText()+"\n"+ cursor.getString(index_Body));
                    try {
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                    }catch(Exception e)
                    {
                        Toast.makeText(this, "Cannot mark the message as read",Toast.LENGTH_LONG).show();
                    }
                }

                /*for(int idx=0;idx<cursor.getColumnCount();idx++)
                {   //if(strAddress.equals("Ncell") && msgStatus!="-1")
                        //msgData += msgStatus+" " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    tv_message.setText(strAddress +msgStatus+ "$$$" + cursor.getString(index_Body)+longDate +"###\n");
                    Log.e("msgStatus",msgStatus);
                }*/
                /*String strAddress = cur.getString(index_Address);
                // use msgData
                String strbody = cursor.getString(index_Body);*/



            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
        if(true) // when balance information is extracted from the retrieved SMS
        {
            //Set spinner invisible and show balance information

            //

           /* SmsManager smsReceive = new SmsManager();*/




        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void advancedIntegration(View v) {
        // advanced integration example is given in ScanActivity source code
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    /**
     * Called as handler for "simple integration" button.
     */
    public void simpleIntegration(View v) {
        /**
         * In this simple example we will use BlinkOCR SDK to create a simple app
         * that scans an amount from invoice, tax amount from invoice and IBAN
         * to which amount has to be paid.
         */

        Intent intent = new Intent(this, BlinkOCRActivity.class);
        intent.putExtra(BlinkOCRActivity.EXTRAS_LICENSE_KEY, "TGOQR47C-KGCCVLIT-KMRNMYVR-GJRTHIMD-NX4ETSQZ-DXMSFVTC-WEZGGM5B-IPP6CEDT");

        // we need to scan 3 items, so we will add 3 scan configurations to scan configuration array
        ScanConfiguration conf[] = new ScanConfiguration[] {
                // each scan configuration contains two string resource IDs: string shown in title bar and string shown
                // in text field above scan box. Besides that, it contains name of the result and settings object
                // which defines what will be scanned.
                new ScanConfiguration(R.string.title_pin, R.string.title_msg, "PIN Code", new AmountParserSettings()),
                //new ScanConfiguration(R.string.tax_title, R.string.tax_msg, "Tax", new AmountParserSettings()),
                new ScanConfiguration(R.string.iban_title, R.string.iban_msg, "IBAN", new IbanParserSettings())
        };

        intent.putExtra(BlinkOCRActivity.EXTRAS_SCAN_CONFIGURATION, conf);

        // optionally, if we want the help screen to be available to user on camera screen,
        // we can simply prepare an intent for help activity and pass it to BlinkOCRActivity
        Intent helpIntent = new Intent(this, HelpActivity.class);
        intent.putExtra(BlinkOCRActivity.EXTRAS_HELP_INTENT, helpIntent);

        // once intent is prepared, we start the BlinkOCRActivity which will preform scan and return results
        // by calling onActivityResult
        startActivityForResult(intent, BLINK_OCR_REQUEST_CODE);
    }

    /**
     * This method is called whenever control is returned from activity started with
     * startActivityForResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // first we need to check that we have indeed returned from BlinkOCRActivity with
        // success
        if(requestCode == BLINK_OCR_REQUEST_CODE && resultCode == BlinkOCRActivity.RESULT_OK) {
            // now we can obtain bundle with scan results
            Bundle result = data.getBundleExtra(BlinkOCRActivity.EXTRAS_SCAN_RESULTS);

            // each result is stored under key equal to the name of the scan configuration that generated it
            String totalAmount = result.getString("PIN");
            /*String taxAmount = result.getString("Tax");
            String iban = result.getString("IBAN");*/



            //Toast.makeText(this, "To IBAN: " + iban + " we will pay total " + totalAmount + ", tax: " + taxAmount, Toast.LENGTH_LONG).show();
        }
    }
}

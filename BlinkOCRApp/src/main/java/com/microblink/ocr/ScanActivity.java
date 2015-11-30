package com.microblink.ocr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.geometry.Rectangle;
import com.microblink.hardware.SuccessCallback;
import com.microblink.help.HelpActivity;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.ocr.blinkocr.BlinkOCRRecognitionResult;
import com.microblink.recognizers.ocr.blinkocr.BlinkOCRRecognizerSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.results.ocr.OcrResult;
import com.microblink.util.Log;
import com.microblink.view.CameraAspectMode;
import com.microblink.view.CameraEventsListener;
import com.microblink.view.NotSupportedReason;
import com.microblink.view.recognition.RecognitionType;
import com.microblink.view.recognition.RecognizerView;
import com.microblink.view.recognition.ScanResultListener;




public class ScanActivity extends Activity implements CameraEventsListener, ScanResultListener {

    private RecognizerView mRecognizerView;
    private ImageButton mFlashButton;
    private View mResultView;
    private EditText mResult;
    private boolean mTorchOn = false;
    private TextView mMessage;
    private SlidingTabLayout mTitleIndicator;
    private ScanConfiguration[] mConfiguration = Configurator.createScanConfigurations();
    private int mSelectedConfiguration = 0;
    BlinkOCRRecognitionResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        for(ScanConfiguration conf : mConfiguration) {
            conf.loadTitle(this);
        }

        mRecognizerView = (RecognizerView) findViewById(R.id.rec_view);
        mFlashButton = (ImageButton) findViewById(R.id.btnFlash);
        mResultView = findViewById(R.id.layResult);
        mMessage = (TextView) findViewById(R.id.txtMessage);
        mResult = (EditText) findViewById(R.id.txtResult);
        mTitleIndicator = (SlidingTabLayout) findViewById(R.id.indicator);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SamplePagerAdapter());

        mTitleIndicator = (SlidingTabLayout) findViewById(R.id.indicator);
        mTitleIndicator.setViewPager(viewPager);


        mTitleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSelectedConfiguration = position;
                setupMessage(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // camera events listener is required as it will receive camera-related events
        // such as startup errors, autofocus callbacks etc.
        mRecognizerView.setCameraEventsListener(this);
        // scan result listener is requires as it will receive recognition results
        mRecognizerView.setScanResultListener(this);
        // we want camera to use whole available view space by cropping the camera preview
        // instead of letterboxing it
        mRecognizerView.setAspectMode(CameraAspectMode.ASPECT_FILL);
        // license key is required for recognizer to work.
        try {
            mRecognizerView.setLicenseKey("YAZKPB4R-O37ANKYG-VMBOLXSK-EFRLCMTD-GOQYG3PY-JHFBSHOZ-ELLGFMJS-TWL7MPLM");
        } catch (InvalidLicenceKeyException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid license key!", Toast.LENGTH_SHORT).show();
            finish();
        }
        mRecognizerView.setOptimizeCameraForNearScan(true);

        // create BlinkOCR recognizer settings object and add parser to it
        BlinkOCRRecognizerSettings settings = new BlinkOCRRecognizerSettings();
        settings.addParser(mConfiguration[mSelectedConfiguration].getParserName(), mConfiguration[mSelectedConfiguration].getParserSettings());
        // add BlinkOCR recognizer settings object to array of all recognizer settings and initialize
        // recognizer with that array
        mRecognizerView.setRecognitionSettings(new RecognizerSettings[]{settings});
        // define the scanning region of the image that will be scanned.
        // You must ensure that scanning region define here is the same as in the layout
        // The coordinates for scanning region are relative to recognizer view:
        // the following means: rectangle starts at 10% of recognizer view's width and
        // 34% of its height. Rectangle width is 80% of recognizer view's width and
        // 13% of its height.
        // If you do not set this, OCR will be performed on full camera frame and this
        // will result in very poor performance.
        //mRecognizerView.setScanningRegion(new Rectangle(0.1f, 0.34f, 0.8f, 0.13f), false);
        mRecognizerView.setScanningRegion(new Rectangle(0.1f, 0.14f, 0.8f, 0.33f), false);

        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.create();

        setupMessage(false);
    }



    private void setupMessage(boolean updateRecognizerView) {
        mMessage.setText(mConfiguration[mSelectedConfiguration].getTextResource());

        mTitleIndicator.getViewPager().setCurrentItem(mSelectedConfiguration);

        if(updateRecognizerView) {
            BlinkOCRRecognizerSettings settings = new BlinkOCRRecognizerSettings();
            settings.addParser(mConfiguration[mSelectedConfiguration].getParserName(), mConfiguration[mSelectedConfiguration].getParserSettings());
            // unlike setRecognitionSettings that needs to be set before calling create, reconfigureRecognizers is designed
            // to be called while recognizer is active.
            mRecognizerView.reconfigureRecognizers(new RecognizerSettings[] {settings});
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // all activity's lifecycle methods must be passed to recognizer view
        mRecognizerView.changeConfiguration(newConfig);
    }

    @Override
    public void onCameraPreviewStarted() {
        // this method is called when camera preview has started
        // camera is being initialized in background thread and when
        // it is ready, this method is called.
        // You can use it to check camera properties, such as whether
        // torch is supported and then show/hide torch button.
        if (mRecognizerView != null && mRecognizerView.isCameraTorchSupported()) {
            mFlashButton.setVisibility(View.VISIBLE);
        }

        // after camera is started, we can set the metering area for autofocus, white balance
        // and auto exposure measurements
        // we set the same rectangle as for scanning region
        //mRecognizerView.setMeteringAreas(new Rectangle[] {new Rectangle(0.1f, 0.34f, 0.8f, 0.13f)});
        mRecognizerView.setScanningRegion(new Rectangle(0.1f, 0.14f, 0.8f, 0.33f), false);
    }

    @Override
    public void onCameraPreviewStopped() {
        // this method is called when camera preview has stopped
    }

    @Override
    public void onStartupError(Throwable exc) {
        // this method is called when error happens whilst loading RecognizerView
        // this can be either because camera is busy and cannot be opened
        // or native library could not be loaded because of unsupported processor architecture
        Log.e(this, exc, "On startup error!");
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false)
          .setTitle("Error")
          .setMessage("Error while loading camera or library: " + exc.getMessage())
          .setNeutralButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  if(dialog != null) dialog.dismiss();
                  finish();
              }
          }).create().show();
    }

    @Override
    public void onNotSupported(NotSupportedReason reason) {
        // this method is called when RecognizerView detects that device is not
        // supported and describes the not supported reason via enum
        Log.e(this, "Not supported reason: {}", reason);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false)
                .setTitle("Feature not supported")
                .setMessage("Feature not supported! Reason: " + reason.name())
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog != null) dialog.dismiss();
                        finish();
                    }
                }).create().show();
    }

    @Override
    public void onAutofocusFailed() {
        // This method is called when camera focusing has failed.
        // You should inform user to try scanning under different light.
    }

    @Override
    public void onAutofocusStarted(Rect[] focusAreas) {
        // This method is called when camera starts focusing.
        // Focus areas is array of rectangles that camera uses
        // as focus measure regions.
    }

    @Override
    public void onAutofocusStopped(Rect[] focusAreas) {
        // This method is called when camera finishes focusing.
    }

    @Override
    public void onScanningDone(BaseRecognitionResult[] dataArray, RecognitionType recognitionType) {
        // we've enabled only one recognizer, so we expect only one element in dataArray
        if (dataArray != null && dataArray.length == 1) {
            if (dataArray[0] instanceof BlinkOCRRecognitionResult) {
                result = (BlinkOCRRecognitionResult) dataArray[0];
                String scanned = result.getParsedResult(mConfiguration[mSelectedConfiguration].getParserName());
                if(scanned != null && !scanned.isEmpty()) {
                    mResult.setText(scanned);
                    mResultView.setVisibility(View.VISIBLE);
                }
                // additionally if you want to process raw OCR result of default parser group
                // instead of parsed strings you can obtain it like this
                 //OcrResult ocrResult = result.getOcrResult();

                /*******************************************************************************************************
                 *
                 * Place to check the result in background
                 *                  To be written
                 * *****************************************************************************************************
                 */

                // to obtain raw OCR result for certain parser group, give a name of the parser
                // group to getOcrResult method
            }
        }
        mRecognizerView.resumeScanning(false);
    }

    public void onBtnExitClicked(View v) {
        finish();
    }

    public void onBtnHelpClicked(View v) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void onBtnFlashClicked(View v) {
        mRecognizerView.setTorchState(!mTorchOn, new SuccessCallback() {
            @Override
            public void onOperationDone(boolean success) {
                if (success) {
                    mTorchOn = !mTorchOn;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mTorchOn) {
                                mFlashButton.setImageResource(R.drawable.flashlight_inverse);
                            } else {
                                mFlashButton.setImageResource(R.drawable.flashlight);
                            }
                        }
                    });
                }
            }
        });
    }

    public String squeezePIN(String pinNumber){
        String correctedPIN = "";
        for(int i=0;i<pinNumber.length();i++){
            switch(pinNumber.charAt(i)){
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    correctedPIN +=pinNumber.charAt(i);
                    break;
            }
        }
        return correctedPIN;
    }
    public void onBtnAcceptClicked(View v) {
        // do something with data from mResult
        mSelectedConfiguration = (mSelectedConfiguration + 1) % mConfiguration.length;

        mResultView.setVisibility(View.INVISIBLE);
        setupMessage(true);

        OcrResult ocrResult = result.getOcrResult();
        //Toast.makeText(this,"Sending SMS\nMSG:"+ocrResult.toString(),Toast.LENGTH_LONG).show();
        String pinNumber = ocrResult.toString();
        pinNumber = pinNumber.replace(" ","");
        //pinNumber = squeezePIN(pinNumber);
        if(pinNumber.length()<16){
            int len = pinNumber.length();
            Toast.makeText(this,"PIN No. Less than 16 digit. Please retry!"+pinNumber,Toast.LENGTH_LONG).show();
            Toast.makeText(this,"Len: "+len+"/nSqueezed PIN : "+squeezePIN(pinNumber).toString(),Toast.LENGTH_LONG).show();
            onCameraPreviewStarted(); // restart preview
        }
        else if(pinNumber.length()>16){
            String correctedString = squeezePIN(pinNumber);
            Toast.makeText(this,"Corrected PIN No. from input = " + correctedString,Toast.LENGTH_LONG).show();
            if(correctedString.length()<16){
                Toast.makeText(this,correctedString+" The corrected PIN is still invalid!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            String correctedString = squeezePIN(pinNumber);
            if(correctedString.length()==16) {
                android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
                smsManager.sendTextMessage("90012", null, correctedString, null, null);

                ProgressBar c = (ProgressBar) findViewById(R.id.progressBarCheck);
                c.setVisibility(View.VISIBLE);
                Intent i = new Intent();
                ImageButton b = (ImageButton) findViewById(R.id.btnAccept);
                b.setVisibility(View.GONE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        Intent intent = new Intent(ScanActivity.this, AfterRecharge.class);
                        startActivity(intent);


                    }
                }, 9999);
            }
            else
            {
                Toast.makeText(this,"PIN No. Invalid... Please Retry!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return mConfiguration.length;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getString(mConfiguration[position].getTitleResource());
        }
        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return new View(ScanActivity.this);
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}

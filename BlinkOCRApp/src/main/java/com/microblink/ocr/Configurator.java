package com.microblink.ocr;

import com.microblink.recognizers.ocr.blinkocr.parser.generic.RawParserSettings;

/**
 * Created by dodo on 03/03/15.
 */
public class Configurator {
    public static ScanConfiguration[] createScanConfigurations() {
        // here we will create scan configuration for E-Mail and Raw text
        // in Raw text parser we will enable Sieve algorithm which will
        // reuse OCR results from multiple video frames to improve quality

        RawParserSettings rawSett = new RawParserSettings();
        rawSett.setUseSieve(false);

        return new ScanConfiguration[] {
                //new ScanConfiguration(R.string.date_title, R.string.date_msg, "Date", new DateParserSettings()),
                //new ScanConfiguration(R.string.email_title, R.string.email_msg, "EMail", new EMailParserSettings()),
                new ScanConfiguration(R.string.raw_title, R.string.raw_msg, "PIN", rawSett)
        };
    }
}

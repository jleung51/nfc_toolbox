package com.app.jleung.nfctagger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NfcReader {

    private static final String TAG = NfcReader.class.getSimpleName();

    private NfcAdapter nfcAdapter;

    private PendingIntent pendingIntent;

    private IntentFilter[] intentFilters;

    private String[][] nfcTechList;

    public NfcReader(Activity currentActivity) throws NfcException {

        nfcAdapter = NfcAdapter.getDefaultAdapter(currentActivity);
        if(nfcAdapter == null) {
            throw new NfcException(NfcException.Reason.NOT_SUPPORTED);
        }
        else if(!nfcAdapter.isEnabled()) {
            throw new NfcException(NfcException.Reason.NOT_ENABLED);
        }

        pendingIntent = PendingIntent.getActivity(
                currentActivity, 0,
                new Intent(
                        currentActivity,
                        currentActivity.getClass()
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );

        intentFilters = new IntentFilter[] {
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        };
        try {
            intentFilters[0].addDataType(MimeType.ANY);
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Internal Error: The Mime-Type was set incorrectly.", e);
        }

        nfcTechList = new String[][] {
                new String[]{ Ndef.class.getName() },
                new String[]{ NfcA.class.getName() },
                new String[]{ NfcB.class.getName() }
        };

    }

    public void pause(Activity currentActivity) {
        nfcAdapter.disableForegroundDispatch(currentActivity);
    }

    public void resume(Activity currentActivity) {
        nfcAdapter.enableForegroundDispatch(
                currentActivity, pendingIntent, intentFilters, nfcTechList
        );
    }

    public String readTagFromIntent(Intent intent) throws NfcException {
        String action = intent.getAction();
        Log.d(TAG, "Received intent with action [" + action + "]");

        // No tag when you startup the application
        if(action.equals("android.intent.action.MAIN")) {
            Log.d(TAG, "Startup Intent detected - no action taken");
            return null;
        }

        switch(action) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                if(intent.getType().equals(MimeType.TEXT_PLAIN)) {
                    return readParcelableTag((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
                }
                else {
                    throw new NfcException(
                            NfcException.Reason.INVALID_CONTENT_TYPE
                    );
                }
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                Tag parcelableTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                for(String techType : parcelableTag.getTechList()) {
                    if (techType.equals(Ndef.class.getName())) {
                        return readParcelableTag(parcelableTag);
                    }
                }
            default:
                throw new NfcException(NfcException.Reason.INVALID_TAG_TYPE);
        }
    }

    private static String readParcelableTag(Tag parcelableTag) throws NfcException {
        Ndef ndefTag = Ndef.get(parcelableTag);
        if(ndefTag == null) {
            throw new NfcException(NfcException.Reason.INVALID_TAG_TYPE);
        }

        NdefRecord extractedRecord = null;
        for(NdefRecord ndefRecord : ndefTag.getCachedNdefMessage().getRecords()) {
            if(ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                    Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                extractedRecord = ndefRecord;
                break;
            }
        }
        if(extractedRecord == null) {
            return "";
        }

        return readTagText(extractedRecord);
    }

    /**
     * See NFC forum specification for Text Record Type Definition at 3.2.1
     * http://www.nfc-forum.org/specs
     *
     * @param ndefRecord
     * @return
     * @throws NfcException
     */
    private static String readTagText(NdefRecord ndefRecord) throws NfcException {
        Log.d(TAG, "Reading NDEF record from tag payload");

        byte[] payload = ndefRecord.getPayload();
        int languageCodeLength = payload[0] & 0063;
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        String data;
        try {
            data = new String(
                    payload,
                    languageCodeLength + 1,
                    payload.length - languageCodeLength - 1,
                    textEncoding
            );
        }
        catch(UnsupportedEncodingException e) {
            throw new NfcException(NfcException.Reason.INVALID_ENCODING);
        }

        Log.d(TAG, "Parsed tag data: [" + data + "]");
        return data;
    }

    public static class NfcException extends Exception {

        private Reason reason;

        public NfcException(Reason reason) {
            super();
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }

        public static enum Reason {
            NOT_SUPPORTED("NFC is not supported on this device"),
            NOT_ENABLED("Please enable NFC on your device"),
            INVALID_TAG_TYPE("Invalid tag type; NDEF is required"),
            INVALID_CONTENT_TYPE("Invalid content type; text/plain is required"),
            INVALID_ENCODING("Invalid text encoding");

            private String text;

            private Reason(String text) {
                this.text = text;
            }

            public String getText() {
                return text;
            }
        }
    }

    private abstract static class MimeType {

        public static final String ANY = "*/*";

        public static final String TEXT_PLAIN = "text/plain";

    }

}

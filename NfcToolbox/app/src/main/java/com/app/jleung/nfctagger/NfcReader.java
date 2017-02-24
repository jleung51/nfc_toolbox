package com.app.jleung.nfctagger;

import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NfcReader {

    private static final String TAG = NfcReader.class.getSimpleName();

    private static final String MIME_TEXT_PLAIN = "text/plain";

    public String readTagFromIntent(Intent intent) throws NfcReadFailureException {
        // No tag when you startup the application
        if(intent.getAction().equals("android.intent.action.MAIN")) {
            Log.d(TAG, "Startup Intent detected - no action taken");
            return null;
        }
        else if(!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            throw new NfcReadFailureException(NfcReadFailureException.Reason.INVALID_TAG_TYPE);
        }
        else if(!MIME_TEXT_PLAIN.equals(intent.getType())) {
            throw new NfcReadFailureException(NfcReadFailureException.Reason.INVALID_CONTENT_TYPE);
        }

        return readTag((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
    }

    private String readTag(Tag tag) throws NfcReadFailureException {
        Ndef ndefTag = Ndef.get(tag);
        if(ndefTag == null) {
            throw new NfcReadFailureException(NfcReadFailureException.Reason.INVALID_TAG_TYPE);
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
     * @throws UnsupportedEncodingException
     */
    private String readTagText(NdefRecord ndefRecord) throws NfcReadFailureException {
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
            throw new NfcReadFailureException(NfcReadFailureException.Reason.INVALID_ENCODING);
        }

        Log.d(TAG, "Parsed tag data: [" + data + "]");
        return data;
    }

    public static class NfcReadFailureException extends Exception {

        private Reason reason;

        public NfcReadFailureException(Reason reason) {
            super();
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }

        public static enum Reason {
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

}

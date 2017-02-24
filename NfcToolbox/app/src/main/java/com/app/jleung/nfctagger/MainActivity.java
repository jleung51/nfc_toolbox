package com.app.jleung.nfctagger;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import static com.app.jleung.nfctagger.NfcReader.NfcReadFailureException;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private NfcAdapter nfcAdapter;

    private NfcReader nfcReader;

    public MainActivity() {
        super();
        nfcReader = new NfcReader();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.home_text);

        if(!instantiateNfcAdapter()) {
            return;
        }
        readTagDataTo(textView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        readTagDataTo(textView);
    }

    private boolean instantiateNfcAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        else if(!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC on your device.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void readTagDataTo(TextView textView) {
        String data;
        try {
            data = nfcReader.readTagFromIntent(getIntent());
        }
        catch(NfcReadFailureException e) {
            Toast.makeText(this, e.getReason().getText(), Toast.LENGTH_LONG).show();
            return;
        }
        if(data != null) {
            textView.setText("Tag data: " + data);
        }
    }

}

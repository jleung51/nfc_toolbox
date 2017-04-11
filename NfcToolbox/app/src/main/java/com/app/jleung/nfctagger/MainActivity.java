package com.app.jleung.nfctagger;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import static com.app.jleung.nfctagger.NfcReader.NfcException;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.home_text);

        try {
            NfcReader.instantiateNfcAdapter(this);
        }
        catch(NfcException e) {
            Toast.makeText(this, e.getReason().getText(), Toast.LENGTH_LONG).show();
            if(e.getReason().equals(NfcException.Reason.NOT_SUPPORTED)) {
                finish();
            }
            return;
        }

        readTagDataTo(textView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        readTagDataTo(textView);
    }

    private void readTagDataTo(TextView textView) {
        String data;
        try {
            data = NfcReader.readTagFromIntent(getIntent());
        }
        catch(NfcException e) {
            Toast.makeText(this, e.getReason().getText(), Toast.LENGTH_LONG).show();
            return;
        }

        if(data != null) {
            textView.setText("Tag data: " + data);
        }
    }

}

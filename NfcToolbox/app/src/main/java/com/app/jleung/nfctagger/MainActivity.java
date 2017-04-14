package com.app.jleung.nfctagger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import static com.app.jleung.nfctagger.NfcReader.NfcException;

public class MainActivity extends AppCompatActivity {

    private NfcReader nfcReader;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.home_text);

        try {
            nfcReader = new NfcReader(this);
        }
        catch(NfcException e) {
            Toast.makeText(this, e.getReason().getText(), Toast.LENGTH_LONG).show();
            if(e.getReason().equals(NfcException.Reason.NOT_SUPPORTED)) {
                finish();
            }
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcReader.pause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(nfcReader != null) {
            nfcReader.resume(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        readTagDataTo(intent);
    }

    private void readTagDataTo(Intent intent) {
        String data;
        try {
            data = nfcReader.readTagFromIntent(intent);
        }
        catch(NfcException e) {
            Toast.makeText(this, e.getReason().getText(), Toast.LENGTH_LONG).show();
            return;
        }

        if(data == null) {
            return;
        }

        textView.setText("Tag data: " + data);
        Toast.makeText(this, "Read tag data", Toast.LENGTH_SHORT).show();
    }

}

package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.view.Menu;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Home extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.button_scan_qr_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    AlertDialog alertDialog = new AlertDialog.Builder(Home.this).create();
                    alertDialog.setTitle(R.string.error);
                    alertDialog.setMessage(getString(R.string.error_barcode_scanner_not_installed));
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(
                                    "market://details?id=com.google.zxing.client.android"));
                            startActivity(intent);
                        }
                    });
                    alertDialog.show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String content = intent.getStringExtra("SCAN_RESULT");
            if (content == null) return;
            String decoded_content = null;
            try {
                byte[] decoded = Base64.decode(content, Base64.DEFAULT);
                decoded_content = new String(decoded);
            } catch (IllegalArgumentException e) {
                showSimpleErrorDialog(getString(R.string.error_invalid_content));
            }
            if (decoded_content != null) {
                try {
                    JSONObject object = new JSONObject(decoded_content);
                    int VERSION = object.getInt("v");
                    String URL = object.getString("u");
                    String CONTENT = object.getString("c");

                    // validate version number

                    if (VERSION <= 0) throw new JSONException(null);
                    if (VERSION > getResources().getInteger(R.integer.max_api_version))
                        throw new Exception(getString(R.string.error_please_update_app));

                    // validate help URL
                    URL url = new URL(URL);
                    url.toURI(); // stop never used warning

                    if (CONTENT.length() == 0)
                        throw new Exception(getString(R.string.error_invalid_content));
                } catch (MalformedURLException e) {
                    showSimpleErrorDialog(getString(R.string.error_invalid_url));
                } catch (JSONException e) {
                    showSimpleErrorDialog(getString(R.string.error_invalid_content));
                } catch (Exception e) {
                    showSimpleErrorDialog(e.getMessage());
                }
            }
        }
    }

    private void showSimpleErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setTitle(R.string.error);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
        alertDialog.show();
    }
}

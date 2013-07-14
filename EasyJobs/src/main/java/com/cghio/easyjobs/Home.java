package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Home extends Activity {

    private static String PREF_FILE = "auth_info";
    private static String PREF_VERSION = "VERSION";
    private static String PREF_URL = "URL";
    private static String PREF_CONTENT = "CONTENT";

    public static int API_VERSION = 0;
    public static String API_HELP_URL = null;
    public static String API_TOKEN = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (readPrefs()) {
            showJobs();
        }

        Button scan_or_revoke = (Button) findViewById(R.id.button_scan_or_revoke);

        scan_or_revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (readPrefs()) {
                    SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.remove(PREF_VERSION);
                    editor.remove(PREF_URL);
                    editor.remove(PREF_CONTENT);
                    editor.commit();
                    ((Button) view).setText(R.string.scan_qr_code);
                    return;
                }
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

                    SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(PREF_VERSION, VERSION);
                    editor.putString(PREF_URL, URL);
                    editor.putString(PREF_CONTENT, CONTENT);
                    editor.commit();

                } catch (MalformedURLException e) {
                    showSimpleErrorDialog(getString(R.string.error_invalid_url));
                } catch (JSONException e) {
                    showSimpleErrorDialog(getString(R.string.error_invalid_content));
                } catch (Exception e) {
                    showSimpleErrorDialog(e.getMessage());
                }
                if (readPrefs()) {
                    Toast.makeText(Home.this, R.string.successfully_scanned, Toast.LENGTH_SHORT)
                            .show();
                    showJobs();
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

    private void showJobs() {
        try {
            RequestParams params = new RequestParams();
            params.put("token", API_TOKEN);
            AsyncHttpClient client = new AsyncHttpClient();
            final ProgressDialog dialog = ProgressDialog.show(Home.this, null,
                    getString(R.string.loading), true);
            dialog.show();
            client.get(API_HELP_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Intent intent = new Intent(Home.this, Jobs.class);
                            Home.this.startActivity(intent);
                        }
                    }, 800);
                }
            });
        } catch (Exception e) {
            showSimpleErrorDialog(getString(R.string.error_unspecified));
        }
    }

    private boolean readPrefs() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
        int VERSION = sharedPrefs.getInt(PREF_VERSION, 0);
        String URL = sharedPrefs.getString(PREF_URL, "");
        String CONTENT = sharedPrefs.getString(PREF_CONTENT, "");

        // validate version number
        if (VERSION <= 0) return false;
        if (VERSION > getResources().getInteger(R.integer.max_api_version)) return false;

        // validate help URL
        try {
            URL url = new URL(URL);
            url.toURI();
        } catch (Exception e) {
            return false;
        }

        if (CONTENT.length() == 0) return false;

        API_VERSION = VERSION;
        API_HELP_URL = URL;
        API_TOKEN = CONTENT;

        Button scan_or_revoke = (Button) findViewById(R.id.button_scan_or_revoke);
        scan_or_revoke.setText(R.string.revoke_access);

        return true;
    }
}

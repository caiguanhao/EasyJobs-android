package com.cghio.easyjobs;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;

public class RunJob extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runjob);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("URL")) {
            String url = extras.getString("URL");
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(false);
            WebView webview = (WebView) findViewById(R.id.webView);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.loadUrl(url);
        }
    }

}

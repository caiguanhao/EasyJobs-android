package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import org.apache.http.Header;

public class EasyJobsBase extends Activity {

    protected int TIMEOUT = 5000;
    protected int MAX_API_VERSION = 1;
    protected static String IF_NONE_MATCH = "If-None-Match";
    protected static String ETAG = "ETag";

    private ProgressDialog dialog;
    private Handler dialogHandler;

    private static String ETAG_FILE = "etags";
    private static String NOT_MODIFIED = "Not Modified";

    protected String getHeader(Header[] headers, String key) {
        for (Header header : headers) {
            if (header.getName().equals(key)) {
                return header.getValue();
            }
        }
        return "";
    }

    protected void saveETagAndContent(String url, String etag, String content) {
        SharedPreferences eTags = getSharedPreferences(ETAG_FILE, 0);
        SharedPreferences.Editor editor = eTags.edit();
        editor.putString(Base64.encodeToString(url.getBytes(), Base64.NO_WRAP).toLowerCase(), etag);
        editor.putString(Base64.encodeToString(url.getBytes(), Base64.NO_WRAP).toUpperCase(),
                Base64.encodeToString(content.getBytes(), Base64.NO_WRAP));
        editor.commit();
    }

    protected String getEtag(String url) {
        String key = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP).toLowerCase();
        SharedPreferences eTags = getSharedPreferences(ETAG_FILE, 0);
        return eTags.getString(key, "");
    }

    protected String getEtagContent(String url) {
        String key = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP).toUpperCase();
        SharedPreferences eTags = getSharedPreferences(ETAG_FILE, 0);
        String content = eTags.getString(key, "");
        if (content.length() > 0) {
            content = new String(Base64.decode(content, Base64.NO_WRAP));
        }
        return content;
    }

    protected void removeEtagContent(String url) {
        String key = Base64.encodeToString(url.getBytes(), Base64.NO_WRAP);
        SharedPreferences eTags = getSharedPreferences(ETAG_FILE, 0);
        SharedPreferences.Editor editor = eTags.edit();
        editor.remove(key.toLowerCase());
        editor.remove(key.toUpperCase());
        editor.commit();
    }

    protected void clearEtags() {
        SharedPreferences eTags = getSharedPreferences(ETAG_FILE, 0);
        SharedPreferences.Editor editor = eTags.edit();
        editor.clear();
        editor.commit();
    }

    protected boolean isNotModified(Throwable e) {
        return e != null && e.getMessage() != null && e.getMessage().equals(NOT_MODIFIED);
    }

    protected void showSimpleErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setTitle(R.string.error);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
        alertDialog.show();
    }

    protected void showLoading() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setCancelable(false);
        if (dialogHandler == null) {
            dialogHandler = new Handler();
        }
        dialogHandler.postDelayed(new Runnable() {
            public void run() {
                if (dialog != null) dialog.show();
            }
        }, 600);
        dialogHandler.postDelayed(new Runnable() {
            public void run() {
                hideLoading();
            }
        }, TIMEOUT);
    }

    protected void hideLoading() {
        if (dialogHandler != null) {
            dialogHandler.removeCallbacksAndMessages(null);
            dialogHandler = null;
        }
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}

package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;

public class EasyJobsBase extends Activity {

    private ProgressDialog dialog;
    private Handler dialogHandler;

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

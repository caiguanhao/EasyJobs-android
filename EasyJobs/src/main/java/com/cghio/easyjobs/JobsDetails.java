package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsDetails extends Activity {

    private static String API_TOKEN = "";
    private static int JOBS_DETAILS_ID = 0;
    private static String JOBS_SHOW_URL = "";
    private static String JOBS_RUN_URL = "";

    private static ProgressDialog dialog;
    private static Handler dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_details);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("API_TOKEN")) {
                API_TOKEN = extras.getString("API_TOKEN");
            }
            if (extras.containsKey("JOBS_SHOW_URL")) {
                JOBS_SHOW_URL = extras.getString("JOBS_SHOW_URL");
            }
            if (extras.containsKey("JOBS_RUN_URL")) {
                JOBS_RUN_URL = extras.getString("JOBS_RUN_URL");
            }
            if (extras.containsKey("JOB_ID")) {
                JOBS_DETAILS_ID = extras.getInt("JOB_ID");
                if (JOBS_DETAILS_ID > 0) {
                    getJobDetails();
                }
            }
        }
    }

    private void getJobDetails() {
        if (JOBS_DETAILS_ID == 0 || API_TOKEN.length() == 0 || JOBS_SHOW_URL.length() == 0)
            return;

        try {
            RequestParams params = new RequestParams();
            params.put("token", API_TOKEN);
            AsyncHttpClient client = new AsyncHttpClient();
            String url = JOBS_SHOW_URL;
            url = url.replace(":id", JOBS_DETAILS_ID+"");
            client.setTimeout(5000);
            showLoading();
            client.get(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onFinish() {
                    hideLoading();
                }
                @Override
                public void onFailure(Throwable e, String response) {
                    if (e != null && e.getCause() != null) {
                        showSimpleErrorDialog(e.getCause().getMessage());
                    } else if (e != null && e.getCause() == null) {
                        showSimpleErrorDialog(e.getMessage());
                    } else {
                        showSimpleErrorDialog(getString(R.string.error_connection_problem));
                    }
                    showReloadButton();
                }
                @Override
                public void onSuccess(final String response) {
                    try {
                        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

                        JSONObject obj = new JSONObject(response);
                        JSONObject job = obj.getJSONObject("job");

                        String[] Keys = new String[]{"name", "script", "id", "created_at",
                                "updated_at", "mean_time"};
                        String[] Names = new String[]{"Name", "Script", "ID", "Created at",
                                "Updated at", "Mean time"};

                        for (int i = 0; i < Keys.length; i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", Names[i]);
                            map.put("VALUE", job.getString(Keys[i]));
                            data.add(map);
                        }

                        JSONObject interpreter = obj.optJSONObject("interpreter");

                        if (interpreter == null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", "Interpreter");
                            map.put("VALUE", "(default)");
                            data.add(map);
                        } else {
                            Keys = new String[]{"path", "upload_script_first"};
                            Names = new String[]{"Interpreter", "Upload script first?"};

                            for (int i = 0; i < Keys.length; i++) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("KEY", Names[i]);
                                map.put("VALUE", interpreter.getString(Keys[i]));
                                data.add(map);
                            }
                        }

                        JSONObject server = obj.optJSONObject("server");

                        if (server == null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", "Server");
                            map.put("VALUE", getString(R.string.no_server));
                            data.add(map);
                        } else {
                            Keys = new String[]{"name", "host", "username", "created_at",
                                    "updated_at"};
                            Names = new String[]{"Server Name", "Host", "Username", "Server created at",
                                    "Server updated at"};

                            for (int i = 0; i < Keys.length; i++) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("KEY", Names[i]);
                                map.put("VALUE", server.getString(Keys[i]));
                                data.add(map);
                            }
                        }

                        String hash = obj.optString("hash", "");

                        if (hash.length() == 0) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", "Run");
                            map.put("VALUE", "No script to run.");
                            data.add(map);
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("HASH", hash);
                            map.put("KEY", "Run");
                            map.put("VALUE", "Run this job now...");
                            data.add(map);
                        }

                        JobsDetailsAdapter adapter = new JobsDetailsAdapter(JobsDetails.this,
                                R.layout.listview_jobs_details_items, data);
                        ListView listview_jobs_details =
                                (ListView) findViewById(R.id.listview_jobs_details);
                        listview_jobs_details.setAdapter(adapter);
                        listview_jobs_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if (i == adapterView.getCount() - 1) {
                                    Object item = adapterView.getAdapter().getItem(i);
                                    if (item instanceof Map) {
                                        String hash = ((Map) item).get("HASH").toString();
                                        toRunJob(hash);
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        showSimpleErrorDialog(getString(R.string.error_unspecified));
                    }
                }
            });
        } catch (Exception e) {
            showSimpleErrorDialog(getString(R.string.error_unspecified));
        }
    }

    private void showReloadButton() {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("KEY", "Error connecting to server.");
        map.put("VALUE", "Retry");
        data.add(map);
        JobsDetailsAdapter adapter = new JobsDetailsAdapter(JobsDetails.this,
                R.layout.listview_jobs_details_items, data);
        ListView listview_jobs_details =
                (ListView) findViewById(R.id.listview_jobs_details);
        listview_jobs_details.setAdapter(adapter);
        listview_jobs_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getJobDetails();
            }
        });
    }

    private void toRunJob(final String hash) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_run_job).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        runJob(hash);
                    }
                }).setNegativeButton("No", null).show();
    }

    private void runJob(String hash) {
        String url = JOBS_RUN_URL;
        url = url.replace(":id", JOBS_DETAILS_ID+"");
        if (hash.length() > 0) {
            url = url + "?hash=" + hash;
            url = url + "&token=" + API_TOKEN;

            Intent intent = new Intent(JobsDetails.this, RunJob.class);
            intent.putExtra("URL", url);
            JobsDetails.this.startActivity(intent);
        }
    }

    private void showSimpleErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setTitle(R.string.error);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
        alertDialog.show();
    }

    private void showLoading() {
        dialog = new ProgressDialog(JobsDetails.this);
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

    private void hideLoading() {
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

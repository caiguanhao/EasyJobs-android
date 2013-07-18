package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobsDetails extends Activity {

    private static String API_TOKEN = "";
    private static int JOBS_DETAILS_ID = 0;
    private static String JOBS_SHOW_URL = "";
    private static String JOBS_RUN_URL = "";

    private static String JOBS_PARAMETERS_INDEX_URL = "";

    private static String jobScript = "";
    private static boolean jobHasNoInterpreter = false;

    private static ProgressDialog dialog;
    private static Handler dialogHandler;

    private static String NOT_DEFINED = "(not defined)\n";

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
            if (extras.containsKey("JOBS_PARAMETERS_INDEX_URL")) {
                JOBS_PARAMETERS_INDEX_URL = extras.getString("JOBS_PARAMETERS_INDEX_URL");
            }
            if (extras.containsKey("JOB_ID")) {
                JOBS_DETAILS_ID = extras.getInt("JOB_ID");
                if (JOBS_DETAILS_ID > 0) {
                    getJobDetails();
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reload_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:
                getJobDetails();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                            if (Keys[i].matches(".*at")) {
                                map.put("VALUE", toRelativeDateTime(job.getString(Keys[i])));
                            } else {
                                map.put("VALUE", job.getString(Keys[i]));
                            }
                            data.add(map);
                        }

                        jobScript = job.getString("script");

                        JSONObject interpreter = obj.optJSONObject("interpreter");

                        if (interpreter == null) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("KEY", "Interpreter");
                            map.put("VALUE", "(default)");
                            data.add(map);

                            jobHasNoInterpreter = true;
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
                                if (Keys[i].matches(".*at")) {
                                    map.put("VALUE", toRelativeDateTime(server.getString(Keys[i])));
                                } else {
                                    map.put("VALUE", server.getString(Keys[i]));
                                }
                                data.add(map);
                            }
                        }

                        if (jobHasNoInterpreter) {
                            Pattern replace = Pattern.compile("[^\\%]?\\%\\{(.*?)\\}");
                            Matcher matcher = replace.matcher(jobScript);
                            while (matcher.find()) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("PARAM", matcher.group(1));
                                map.put("KEY", "Variable: " + matcher.group(1));
                                map.put("VALUE", NOT_DEFINED);
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
                                Object item = adapterView.getAdapter().getItem(i);
                                if (item instanceof Map) {
                                    if (((Map) item).containsKey("HASH")) {
                                        String hash = ((Map) item).get("HASH").toString();
                                        toRunJob(hash);
                                    }
                                    if (((Map) item).containsKey("PARAM")) {
                                        showParams(((Map) item).get("PARAM").toString());
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
            String params = collectParams();
            if (params.length() > 0) url += params;
            Intent intent = new Intent(JobsDetails.this, RunJob.class);
            intent.putExtra("URL", url);
            JobsDetails.this.startActivity(intent);
        }
    }

    private String collectParams() {
        String params = "";
        ListView listview_jobs_details =
                (ListView) findViewById(R.id.listview_jobs_details);
        JobsDetailsAdapter adapter = (JobsDetailsAdapter) listview_jobs_details.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Object item = adapter.getItem(i);
                if (item != null) {
                    Map map_item = (Map) item;
                    if (map_item.containsKey("PARAM") && map_item.containsKey("VALUE")) {
                        if (!map_item.get("VALUE").toString().equals(NOT_DEFINED)) {
                            try {
                                params += "&parameters[" + map_item.get("PARAM").toString() + "]=" +
                                        URLEncoder.encode(map_item.get("VALUE").toString(), "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return params;
    }

    private void showParams(String param) {
        Intent intent = new Intent(JobsDetails.this, Parameters.class);
        intent.putExtra("JOBS_PARAMETERS_INDEX_URL", JOBS_PARAMETERS_INDEX_URL);
        intent.putExtra("API_TOKEN", API_TOKEN);
        intent.putExtra("PARAM", param);
        JobsDetails.this.startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String key = "";
                String value = "";
                if (data != null) {
                    Bundle extra = data.getExtras();
                    if (extra != null) {
                        key = extra.getString("key");
                        value = extra.getString("value");
                    }
                }
                if (key == null || key.length() == 0) return;
                if (value == null || value.length() == 0) value = NOT_DEFINED;
                ListView listview_jobs_details =
                        (ListView) findViewById(R.id.listview_jobs_details);
                JobsDetailsAdapter adapter = (JobsDetailsAdapter) listview_jobs_details.getAdapter();
                if (adapter != null) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i) != null) {
                            Map<String, Object> map_item = adapter.getItem(i);
                            if (map_item.containsKey("PARAM") &&
                                    map_item.get("PARAM").toString().equals(key)) {
                                map_item.put("VALUE", value);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String toRelativeDateTime(String text) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            text = String.valueOf(DateUtils.getRelativeTimeSpanString(
                    format.parse(text).getTime(), (new Date()).getTime(), 0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return text;
    }

    private void showSimpleErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(message);
        alertDialog.setTitle(R.string.error);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (Message) null);
        alertDialog.show();
    }

    private void showLoading() {
        hideLoading();
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

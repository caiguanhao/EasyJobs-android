package com.cghio.easyjobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jobs extends Activity {

    private static String JOBS_INDEX_VERB = null;
    private static String JOBS_INDEX_URL = null;

    public static String JOBS_SHOW_VERB = null;
    public static String JOBS_SHOW_URL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        getHelp();
    }

    private void getHelp() {
        if (Home.API_TOKEN.length() == 0) return;
        try {
            RequestParams params = new RequestParams();
            params.put("token", Home.API_TOKEN);
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(Home.API_HELP_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(final String response) {
                    try {
                        JSONObject helpObj = new JSONObject(response);
                        JSONObject jobsObj = helpObj.getJSONObject("jobs");
                        JSONObject jobsIndexObj = jobsObj.getJSONObject("index");
                        JOBS_INDEX_VERB = jobsIndexObj.getString("verb");
                        JOBS_INDEX_URL = jobsIndexObj.getString("url");

                        JSONObject jobsShowObj = jobsObj.getJSONObject("show");
                        JOBS_SHOW_VERB = jobsShowObj.getString("verb");
                        JOBS_SHOW_URL = jobsShowObj.getString("url");

                        getJobs();
                    } catch (JSONException e) {
                        showSimpleErrorDialog(getString(R.string.error_unspecified));
                    }
                }
            });
        } catch (Exception e) {
            showSimpleErrorDialog(getString(R.string.error_unspecified));
        }
    }

    private void getJobs() {
        if (JOBS_INDEX_VERB.length() == 0 || JOBS_INDEX_URL.length() == 0) return;
        if (JOBS_INDEX_VERB.equals("get")) {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("token", Home.API_TOKEN);
            client.get(JOBS_INDEX_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    try {
                        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

                        JSONArray jobs = new JSONArray(response);
                        for (int i = 0; i < jobs.length(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("ID", jobs.getJSONObject(i).getInt("id"));
                            map.put("NAME", jobs.getJSONObject(i).getString("name"));
                            String server = jobs.getJSONObject(i).getString("server_name");
                            if (server.equals("null")) server = getString(R.string.no_server);
                            map.put("SERVER_NAME", server);
                            data.add(map);
                        }
                        SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                                R.layout.listview_jobs_items, new String[]{"NAME", "SERVER_NAME"},
                                new int[]{R.id.text_job_name, R.id.text_server_name});
                        ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
                        listview_jobs.setAdapter(adapter);
                        listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Object item = adapterView.getAdapter().getItem(i);
                                if (item instanceof Map) {
                                    int ID = Integer.parseInt(((Map) item).get("ID").toString());
                                    Intent intent = new Intent(Jobs.this, JobsDetails.class);
                                    intent.putExtra("JOB_ID", ID);
                                    Jobs.this.startActivity(intent);
                                }
                            }
                        });
                    } catch (JSONException e) {
                        showSimpleErrorDialog(getString(R.string.error_unspecified));
                    }
                }
            });
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

package com.cghio.easyjobs;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class JobsDetailsAdapter extends ArrayAdapter<Map<String, Object>> {

    private List<Map<String, Object>> items;

    public JobsDetailsAdapter(Context context, int resource, List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.items = objects;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            view = li.inflate(R.layout.listview_jobs_details_items, null);
        }
        Map<String, Object> item = this.items.get(position);
        if (item != null && view != null) {
            TextView key = (TextView) view.findViewById(R.id.text_key);
            TextView value = (TextView) view.findViewById(R.id.text_value);

            if (key != null) {
                key.setText(item.get("KEY").toString());
            }
            if (value != null) {
                String text = item.get("VALUE").toString().trim();
                if (text.equals("null")) {
                    value.setText(R.string.na);
                } else {
                    value.setText(text);
                    if (text.length() > 30) {
                        value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    } else {
                        value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    }
                }
            }
        }
        return view;
    }
}
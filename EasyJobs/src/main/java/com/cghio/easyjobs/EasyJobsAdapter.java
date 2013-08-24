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

public class EasyJobsAdapter extends ArrayAdapter<Map<String, Object>> {

    private List<Map<String, Object>> items;
    private int resource;
    private int initialTextColor = -1;
    private int initialBackgroundColor = -1;
    private float initialTextSize = -1;

    public EasyJobsAdapter(Context context, int resource, List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.items = objects;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater li = LayoutInflater.from(getContext());
            view = li.inflate(resource, null);
        }
        Map<String, Object> item = this.items.get(position);
        if (item != null && view != null) {
            TextView key = (TextView) view.findViewById(R.id.text_key);
            TextView value = (TextView) view.findViewById(R.id.text_value);

            if (value != null) {
                value.setVisibility(View.VISIBLE);

                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                        getContext().getResources().getDisplayMetrics());
                if (key != null) {
                    key.setText(item.get("KEY").toString());
                    key.setPadding(padding, padding, padding, 0);
                    if (initialTextColor == -1 || initialBackgroundColor == -1 || initialTextSize == -1) {
                        initialTextColor = key.getCurrentTextColor();
                        initialBackgroundColor = key.getDrawingCacheBackgroundColor();
                        initialTextSize = key.getTextSize();
                    } else {
                        key.setTextColor(initialTextColor);
                        key.setBackgroundColor(initialBackgroundColor);
                        key.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialTextSize);
                    }
                }

                String text = "";
                if (item.containsKey("VALUE") && item.get("VALUE") != null)
                    text = item.get("VALUE").toString().trim();
                if (text.equals("null")) {
                    value.setText(R.string.na);
                } else if (text.length() == 0) {
                    value.setVisibility(View.GONE);

                    if (key != null) {
                        key.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        key.setPadding(padding, padding, padding, padding);
                        key.setTextColor(getContext().getResources().getColor(android.R.color.background_light));
                        key.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
                    }
                } else {
                    value.setText(text);
                    if (resource == R.layout.listview_jobs_details_items) {
                        if (text.length() > 30) {
                            value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        } else {
                            value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        }
                    }
                }
            }
        }
        return view;
    }
}
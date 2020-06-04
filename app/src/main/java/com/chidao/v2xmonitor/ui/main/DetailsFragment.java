package com.chidao.v2xmonitor.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.ListFragment;

import com.chidao.v2xmonitor.R;

public class DetailsFragment extends ListFragment {
    private DetailsFragment.DataArrayAdapter<String> mAdapter;
    private String[] dataContent = {
        "3D", "1970.1.1 0:0:0", "121.469170", "31.224361", "20 m", "80 km/h",
        "3000 rpm", "80 km/h", "300 ℃", "210 ℃", "800 kpa", "100 %", "28 V",
        "200 %", "260 kpa", "20000 km", "240 km"
    };

    public static DetailsFragment newInstance() {
        return new DetailsFragment();
    }

    public DetailsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new DetailsFragment.DataArrayAdapter<String>(getActivity());
        setListAdapter(mAdapter);

        for (String c : dataContent) {
            mAdapter.add(c);
        }
    }

    @Override
    public void onStart () {
        super.onStart();
        getListView().setDivider(null);
    }

    public class DataArrayAdapter<T> extends ArrayAdapter<T> {
        private Context mCtx;
        private String[] dataNameArray;

        public DataArrayAdapter(Context context) {
            super(context, R.layout.data_list_item, R.id.data_content);
            mCtx = context;
            dataNameArray = getResources().getStringArray(R.array.data_name_array);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View listItem = super.getView(position, convertView, parent);

            TextView dot = listItem.findViewById(R.id.dot);
            Drawable background = dot.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int color = Utils.getRandomColor();
                gradientDrawable.setColor(color);
            }

            TextView dataName = listItem.findViewById(R.id.data_name);
            dataName.setText(dataNameArray[position]);

            TextView content = listItem.findViewById(R.id.data_content);
            content.setText((String)getItem(position));

            return listItem;
        }
    }
}

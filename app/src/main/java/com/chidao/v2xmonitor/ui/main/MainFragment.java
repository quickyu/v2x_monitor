package com.chidao.v2xmonitor.ui.main;

import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.chidao.v2xmonitor.R;

public class MainFragment extends ListFragment {

    private MainViewModel mViewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceArrayAdapter<Integer> adapter = new DeviceArrayAdapter<Integer>(getActivity());
        setListAdapter(adapter);

        adapter.add(4);
        adapter.add(2);
        adapter.add(3);
        adapter.add(5);
    }

    @Override
    public void onStart () {
        super.onStart();
        getListView().setDivider(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    public class DeviceArrayAdapter<T> extends ArrayAdapter<T> {
        private Context mCtx;

        public DeviceArrayAdapter(Context context) {
            super(context, R.layout.device_list_item, R.id.device_name);
            mCtx = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View listItem = super.getView(position, convertView, parent);

            TextView head = listItem.findViewById(R.id.head);
            Drawable background = head.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int color = Utils.getBackgroundColor();
                gradientDrawable.setColor(color);
            }
            head.setText(Integer.toString((Integer)getItem(position)));

            TextView deviceName = listItem.findViewById(R.id.device_name);
            deviceName.setText(mCtx.getString(R.string.collector_name));

            TextView deviceNum = listItem.findViewById(R.id.device_num);
            deviceNum.setText(mCtx.getString(R.string.device_num) + getItem(position));

            ImageView arrowImage = listItem.findViewById(R.id.image_arrow);
            arrowImage.setColorFilter(getContext().getResources().getColor(R.color.lightGrey));

            return listItem;
        }
    }
}

package com.chidao.v2xmonitor.ui.main;

import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chidao.v2xmonitor.DetailsActivity;
import com.chidao.v2xmonitor.R;

import java.util.ArrayList;

public class MainFragment extends ListFragment {
    private DataCommViewModel mViewModel;
    private DeviceArrayAdapter mAdapter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new DeviceArrayAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onStart () {
        super.onStart();

        getListView().setDivider(null);

        if (mAdapter.isEmpty())
            setListShown(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(DataCommViewModel.class);

        mViewModel.mDeviceData.observe(requireActivity(),  new Observer<ArrayList<DataCommViewModel.DataContent>>() {
            @Override
            public void onChanged(@Nullable  ArrayList<DataCommViewModel.DataContent> contentList) {
                if (contentList != null) {
                    if (mAdapter.isEmpty())
                        setListShown(true);

                    for (DataCommViewModel.DataContent data : contentList) {
                        boolean exist = false;
                        int id = data.getId();

                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            if (id == mAdapter.getItem(i)) {
                                exist = true;
                                break;
                            }
                        }

                        if (!exist)
                            mAdapter.add(id);
                    }
                }
            }
        });
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), DetailsActivity.class);
        intent.putExtra("device_id",  (int)mAdapter.getItem(position));
        startActivity(intent);
    }

    public class DeviceArrayAdapter extends ArrayAdapter<Integer> {
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
                int color = Utils.getDeviceColor(getItem(position));
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

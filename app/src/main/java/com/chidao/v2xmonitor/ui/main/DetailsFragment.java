package com.chidao.v2xmonitor.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.chidao.v2xmonitor.DetailsActivity;
import com.chidao.v2xmonitor.R;

import java.util.ArrayList;

public class DetailsFragment extends ListFragment {
    private DataCommViewModel mViewModel;
    private DetailsFragment.DataArrayAdapter<String> mAdapter;
    private String[] dataNameArray;

    public static DetailsFragment newInstance() {
        return new DetailsFragment();
    }

    public DetailsFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(DataCommViewModel.class);

        mViewModel.mDeviceData.observe(requireActivity(), new Observer<ArrayList<DataCommViewModel.DataContent>>() {
            @Override
            public void onChanged(@Nullable ArrayList<DataCommViewModel.DataContent> contentList) {
                if (contentList != null) {
                    if (mAdapter.isEmpty())
                        setListShown(true);

                    int devId = ((DetailsActivity)requireActivity()).deviceId;

                    DataCommViewModel.DataContent content = null;
                    for (DataCommViewModel.DataContent c : contentList) {
                        if (c.getId() == devId) {
                            content = c;
                            break;
                        }
                    }

                    if (content != null) {
                        mAdapter.clear();
                        for (int i = 0; i < DataCommViewModel.DataContent.CONTENT_NUM; i++) {
                            mAdapter.add(content.getContent(i));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataNameArray = getResources().getStringArray(R.array.data_name_array);

        mAdapter = new DetailsFragment.DataArrayAdapter<String>(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onStart () {
        super.onStart();
        getListView().setDivider(null);

        if (mAdapter.isEmpty())
            setListShown(false);
    }

    public class DataArrayAdapter<T> extends ArrayAdapter<T> {
        private Context mCtx;

        public DataArrayAdapter(Context context) {
            super(context, R.layout.data_list_item, R.id.data_content);
            mCtx = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View listItem = super.getView(position, convertView, parent);

            TextView dot = listItem.findViewById(R.id.dot);
            Drawable background = dot.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int color = Utils.getColorByIndex(position);
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

package org.openhds.mobile.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.repository.DataWrapper;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class DataSelectionFragment extends Fragment {

    private DataSelectionListener listener;
    private DataSelectionListAdapter dataWrapperAdapter;
    private ListView listView;

    public interface DataSelectionListener {
        void onDataSelected(DataWrapper data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DataSelectionListener) {
            this.listener = (DataSelectionListener)activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.data_selection_fragment, container, false);
        listView = (ListView) viewGroup.findViewById(R.id.data_fragment_listview);
        listView.setOnItemClickListener(new DataClickListener());
        return viewGroup;
    }

    public void populateData(List<DataWrapper> data) {
        dataWrapperAdapter = new DataSelectionListAdapter(getActivity(), R.layout.generic_list_item_white_text, data);
        listView.setAdapter(dataWrapperAdapter);
    }

    public void clearData() {
        if (dataWrapperAdapter != null) {
            dataWrapperAdapter.clear();
        }
    }

    private class DataClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listener != null) {
                DataWrapper selected = dataWrapperAdapter.getItem(position);
                listener.onDataSelected(selected);
            }
        }
    }

    private class DataSelectionListAdapter extends ArrayAdapter<DataWrapper> {

        public DataSelectionListAdapter(Context context, int resource, List<DataWrapper> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DataWrapper dataWrapper = dataWrapperAdapter.getItem(position);

            if (convertView == null) {
                convertView = makeTextWithPayload(getActivity(), dataWrapper.getName(), dataWrapper.getExtId(), dataWrapper.getName(),
                        null, null, R.drawable.data_selector, dataWrapper.getStringsPayload(), dataWrapper.getStringIdsPayload(), false);
            } else {
                configureTextWithPayload(getActivity(), (RelativeLayout) convertView, dataWrapper.getName(), dataWrapper.getExtId(),
                        dataWrapper.getStringsPayload(), dataWrapper.getStringIdsPayload(), false);
            }

            return convertView;
        }
    }
}

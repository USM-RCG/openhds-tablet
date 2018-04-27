package org.openhds.mobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.utilities.MessageUtils;

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
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof DataSelectionListener) {
            this.listener = (DataSelectionListener)ctx;
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
        registerForContextMenu(listView);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.data_fragment_listview) {
            getActivity().getMenuInflater().inflate(R.menu.data_selection_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_favorite:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                DataWrapper selected = getItem(info.position);
                if (selected != null) {
                    Context ctx = getActivity();
                    DatabaseAdapter.getInstance(ctx).addFavorite(selected);
                    MessageUtils.showShortToast(ctx, R.string.saved_favorite);
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private class DataClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listener != null) {
                DataWrapper selected = getItem(position);
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

            DataWrapper dataWrapper = getItem(position);

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

    private DataWrapper getItem(int position) {
        return dataWrapperAdapter.getItem(position);
    }
}

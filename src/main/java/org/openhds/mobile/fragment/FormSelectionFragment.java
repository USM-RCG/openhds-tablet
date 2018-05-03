package org.openhds.mobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.navconfig.forms.Launcher;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class FormSelectionFragment extends Fragment {

    private FormSelectionListener listener;
    private FormSelectionListAdapter formListAdapter;

    public interface FormSelectionListener {
        void onFormSelected(Binding binding);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof FormSelectionListener) {
            listener = (FormSelectionListener)ctx;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.form_selection_fragment, container, false);
    }

    public void createFormButtons(List<Launcher> values) {
        formListAdapter = new FormSelectionListAdapter(getActivity(), R.layout.generic_list_item_white_text, values);
        ListView listView = (ListView) getActivity().findViewById(R.id.form_fragment_listview);
        listView.setAdapter(formListAdapter);
        listView.setOnItemClickListener(new FormClickListener());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)listView.getLayoutParams();
        params.setMargins(0, 0, 0, values.isEmpty()? 0 : 10);  // Add bottom margin only when we have content
    }

    private class FormClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listener != null) {
                Launcher launcher = formListAdapter.getItem(position);
                listener.onFormSelected(launcher.getBinding());
            }
        }
    }

    private class FormSelectionListAdapter extends ArrayAdapter<Launcher> {

        public FormSelectionListAdapter(Context context, int resource, List<Launcher> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String label = formListAdapter.getItem(position).getLabel();

            if (convertView == null) {
                convertView = makeTextWithPayload(getActivity(), label, null,
                        label, null, null, R.drawable.form_selector, null, null, true);
                convertView.setPadding(0, 10, 0, 10); // Make the buttons thicker for easier selection
            }

            configureTextWithPayload(getActivity(),
                    (RelativeLayout) convertView, label, null, null, null, true);

            return convertView;
        }
    }
}

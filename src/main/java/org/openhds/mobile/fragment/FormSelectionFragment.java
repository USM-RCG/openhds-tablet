package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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
import org.openhds.mobile.model.form.FormBehavior;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class FormSelectionFragment extends Fragment {

    private SelectionHandler selectionHandler;
    private FormSelectionListAdapter formListAdapter;
    private int formSelectionDrawableId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.form_selection_fragment, container, false);
    }

    public void setSelectionHandler(SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public void createFormButtons(List<FormBehavior> values) {
        formListAdapter = new FormSelectionListAdapter(getActivity(), R.layout.generic_list_item_white_text, values);
        ListView listView = (ListView) getActivity().findViewById(R.id.form_fragment_listview);
        listView.setAdapter(formListAdapter);
        listView.setOnItemClickListener(new FormClickListener());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)listView.getLayoutParams();
        params.setMargins(0, 0, 0, values.isEmpty()? 0 : 10);  // Add bottom margin only when we have content
    }

    public void setFormSelectionDrawableId(int formSelectionDrawableId) {
        this.formSelectionDrawableId = formSelectionDrawableId;
    }

    public interface SelectionHandler {
        void handleSelectedForm(FormBehavior formBehavior);
    }

    private class FormClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FormBehavior form = formListAdapter.getItem(position);
            selectionHandler.handleSelectedForm(form);
        }
    }

    private class FormSelectionListAdapter extends ArrayAdapter<FormBehavior> {

        public FormSelectionListAdapter(Context context, int resource, List<FormBehavior> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            FormBehavior form = formListAdapter.getItem(position);

            if (convertView == null) {
                convertView = makeTextWithPayload(getActivity(), form.getLabel(), null,
                        form.getLabel(), null, null, formSelectionDrawableId, null, null, true);
                convertView.setPadding(0, 10, 0, 10); // Make the buttons thicker for easier selection
            }

            configureTextWithPayload(getActivity(),
                    (RelativeLayout) convertView, form.getLabel(), null, null, null, true);

            return convertView;
        }
    }
}

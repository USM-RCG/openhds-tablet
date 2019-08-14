package org.cimsbioko.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.cimsbioko.R;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.Launcher;

import java.util.List;

import static org.cimsbioko.utilities.LayoutUtils.configureTextWithPayload;
import static org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload;

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
        ListView listView = getActivity().findViewById(R.id.form_fragment_listview);
        listView.setAdapter(formListAdapter);
        listView.setOnItemClickListener(new FormClickListener());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)listView.getLayoutParams();
        // Add bottom margin only when we have content
        int buttonSpacing = getResources().getDimensionPixelSize(R.dimen.button_list_divider_height);
        params.setMargins(0, 0, 0, values.isEmpty()? 0 : buttonSpacing);
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
            }

            configureTextWithPayload(getActivity(),
                    (RelativeLayout) convertView, label, null, null, null, true);

            return convertView;
        }
    }
}

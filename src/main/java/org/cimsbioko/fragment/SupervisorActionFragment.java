package org.cimsbioko.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.cimsbioko.R;

import static java.util.Arrays.asList;
import static org.cimsbioko.search.Utils.isSearchEnabled;

public class SupervisorActionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SupervisorActionFragment.class.getSimpleName();

    private Button deleteFormsButton, approveFormsButton, rebuildIndicesButton;

    private ActionListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.supervisor_action_fragment, container, false);
        deleteFormsButton = view.findViewById(R.id.delete_forms);
        approveFormsButton = view.findViewById(R.id.approve_forms);
        rebuildIndicesButton = view.findViewById(R.id.rebuild_search_indices);
        rebuildIndicesButton.setVisibility(isSearchEnabled(getContext()) ? View.VISIBLE : View.GONE);
        for (Button button : asList(deleteFormsButton, approveFormsButton, rebuildIndicesButton)) {
            button.setOnClickListener(this);
        }
        return view;
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            if (v == deleteFormsButton) {
                listener.onDeleteForms();
            } else if (v == approveFormsButton) {
                listener.onApproveForms();
            } else if (v == rebuildIndicesButton) {
                listener.onRebuildIndices();
            } else {
                Log.w(TAG, "unable to handle click, unknown view");
            }
        }
    }

    public interface ActionListener {
        void onDeleteForms();
        void onApproveForms();
        void onRebuildIndices();
    }
}

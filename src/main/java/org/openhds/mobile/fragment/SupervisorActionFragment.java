package org.openhds.mobile.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.openhds.mobile.R;

import static java.util.Arrays.asList;
import static org.openhds.mobile.search.Utils.isSearchEnabled;

public class SupervisorActionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SupervisorActionFragment.class.getSimpleName();

    private Button sendFormsButton, deleteFormsButton, approveFormsButton, rebuildIndicesButton;

    private ActionListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.supervisor_action_fragment, container, false);
        sendFormsButton = view.findViewById(R.id.send_forms);
        deleteFormsButton = view.findViewById(R.id.delete_forms);
        approveFormsButton = view.findViewById(R.id.approve_forms);
        rebuildIndicesButton = view.findViewById(R.id.rebuild_search_indices);
        rebuildIndicesButton.setVisibility(isSearchEnabled(getContext()) ? View.VISIBLE : View.GONE);
        for (Button button : asList(sendFormsButton, deleteFormsButton, approveFormsButton, rebuildIndicesButton)) {
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
            if (v == sendFormsButton) {
                listener.onSendForms();
            } else if (v == deleteFormsButton) {
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
        void onSendForms();
        void onDeleteForms();
        void onApproveForms();
        void onRebuildIndices();
    }
}

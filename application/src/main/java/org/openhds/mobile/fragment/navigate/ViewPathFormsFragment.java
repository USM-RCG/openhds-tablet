package org.openhds.mobile.fragment.navigate;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ViewPathFormsFragment extends Fragment
{
    private List<FormInstance> formsForPath;
    private ListView formInstanceView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_form_fragment, container, false);
        return view;
    }

    public void populateRecentFormInstanceListView(List<FormInstance>  formsForPath) {

        formInstanceView =  (ListView) getActivity().findViewById(R.id.path_forms_form_right_column);
        FormInstanceAdapter adapter = new FormInstanceAdapter(
                getActivity().getApplicationContext(),
                R.id.form_instance_list_item, formsForPath.toArray());
        formInstanceView.setAdapter(adapter);
        formInstanceView.setOnItemClickListener(new RecentFormInstanceClickListener());
    }

// Launch an intent for ODK Collect when user clicks on a form instance.
    private class RecentFormInstanceClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position >= 0 && position < formsForPath.size()) {
                FormInstance selected = formsForPath.get(position);
                Uri uri = Uri.parse(selected.getUriString());

                File selectedFile = new File(selected.getFilePath());
                EncryptionHelper.decryptFile(selectedFile, getActivity().getApplicationContext());

                Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                showShortToast(getActivity().getApplicationContext(), R.string.launching_odk_collect);
                startActivityForResult(intent, 0);
            }
        }
    }
}

package org.openhds.mobile.fragment.navigate;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ViewRecentFormFragment extends Fragment implements View.OnClickListener
{
    Button recentForm;
    private List<FormInstance> recentformInstances;
    String currentModuleName;

     private ListView recentFormInstanceView;
    private  FrameLayout recentFormInstanceView1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_form_fragment, container, false);
        recentForm = (Button) view.findViewById(R.id.viewRecentFormButton);
        recentForm.setOnClickListener(this);
      //  recentForm.setOnClickListener();
        return view;
    }


    public void onClick(View view) {

        populateRecentFormInstanceListView();
    }
    //Smita: added this since the portal activity have private method. and also we can add additiona logic to  display specific form
    public void populateRecentFormInstanceListView() {

        recentformInstances = OdkCollectHelper.getAllUnsentFormInstances(getActivity().getContentResolver());
        recentFormInstanceView =  (ListView) getActivity().findViewById(R.id.view_right_column);
        //TODO: parse the recent from instances by the activity type and name
 /*
        checkRecentFormByName(recentformInstances);

  */

        if (null == recentformInstances ||recentformInstances.isEmpty()) {
            return;
        }

        FormInstanceAdapter adapter = new FormInstanceAdapter(
                getActivity().getApplicationContext(), R.id.form_instance_list_item, recentformInstances.toArray());

       recentFormInstanceView.setAdapter(adapter);
       recentFormInstanceView.setOnItemClickListener(new RecentFormInstanceClickListener());
    }

//Smita: it checks the form instances by name and populate it based on which activity get launched

    public static void  checkRecentFormByName(List<FormInstance> recentformInstances, String currentModuleName)
    {
        List recentFormInstanceCatagorized = null;

        for (FormInstance instance : recentformInstances) {

            if (currentModuleName.equals("CensusActivityModule")) {
                if (instance.getFileName().equals("Location_evaluation") || instance.getFileName().equals("Individual")) {

                    recentFormInstanceCatagorized.add(instance);
                }
            } else if (currentModuleName.equals("BiokoActivityModule")) {

                if (instance.getFileName().equals("Bed_net") ||
                        instance.getFileName().equals("spraying") ||
                        instance.getFileName().equals("super_ojo")) {

                    recentFormInstanceCatagorized.add(instance);
                }
            } else if (currentModuleName.equals("UpdateActivityModule")) {

                if (instance.getFileName().equals("Visit") ||
                        instance.getFileName().equals("Pregnancy_observation") ||
                        instance.getFileName().equals("Location")) {

                    recentFormInstanceCatagorized.add(instance);
                }

            }

        }
    }

// Launch an intent for ODK Collect when user clicks on a form instance.
    private class RecentFormInstanceClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position > 0 && position <= recentformInstances.size()) {
                FormInstance selected = recentformInstances.get(position - 1);
                Uri uri = Uri.parse(selected.getUriString());

                File selectedFile = new File(selected.getFilePath());
                EncryptionHelper.decryptFile(selectedFile, getActivity().getApplicationContext());

                Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                showShortToast(getActivity().getApplicationContext(), R.string.launching_odk_collect);
                startActivityForResult(intent, 0);
            }
        }
    }

    private class
            RecentFormButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();

        }
    }


}

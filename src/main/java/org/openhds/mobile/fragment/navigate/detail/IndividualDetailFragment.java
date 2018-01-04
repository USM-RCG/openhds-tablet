package org.openhds.mobile.fragment.navigate.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.navconfig.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;

public class IndividualDetailFragment extends DetailFragment {

    private static final int LABEL_COLOR = R.color.DarkGray;
    private static final int VALUE_COLOR = R.color.White;
    private static final int MISSING_COLOR = R.color.Gray;

    private ScrollView detailContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        detailContainer = (ScrollView) inflater.inflate(R.layout.individual_detail_fragment, container, false);
        return detailContainer;
    }

    @Override
    public void setUpDetails(DataWrapper data) {
        Individual individual = getIndividual(data.getUuid());
        setBannerText(individual.getExtId());
        rebuildPersonalDetails(individual);
        rebuildContactDetails(individual);
    }

    private void setBannerText(String text) {
        TextView banner = (TextView) detailContainer.findViewById(R.id.individual_detail_frag_extid);
        banner.setText(text);
    }

    private void rebuildPersonalDetails(Individual individual) {
        LinearLayout container = (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_personal_info);
        container.removeAllViews();
        addTextView(container, R.string.individual_full_name_label, individual.getFirstName() + " " + individual.getLastName());
        addTextView(container, R.string.individual_other_names_label, individual.getOtherNames());
        addTextView(container, R.string.gender_lbl, getString(ProjectResources.Individual.getIndividualStringId(individual.getGender())));
        addTextView(container, R.string.individual_language_preference_label, getString(ProjectResources.Individual.getIndividualStringId(individual.getLanguagePreference())));
        addTextView(container, R.string.individual_nationality_label, getString(ProjectResources.Individual.getIndividualStringId(individual.getNationality())));
        addTextView(container, R.string.individual_age_label, Individual.getAgeWithUnits(individual));
        addTextView(container, R.string.individual_date_of_birth_label, individual.getDob());
        addTextView(container, R.string.uuid, individual.getUuid());
    }

    private void rebuildContactDetails(Individual individual) {
        LinearLayout container = (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_contact_info);
        container.removeAllViews();
        addTextView(container, R.string.individual_personal_phone_number_label, individual.getPhoneNumber());
        addTextView(container, R.string.individual_other_phone_number_label, individual.getOtherPhoneNumber());
        addTextView(container, R.string.individual_point_of_contact_label, individual.getPointOfContactName());
        addTextView(container, R.string.individual_point_of_contact_phone_number_label, individual.getPointOfContactPhoneNumber());
    }

    private void addTextView(LinearLayout layout, int label, String value) {
        layout.addView(makeLargeTextWithValueAndLabel(getActivity(), label, value, LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
    }

    private Individual getIndividual(String uuid) {
        IndividualGateway gateway = GatewayRegistry.getIndividualGateway();
        return gateway.getFirst(getActivity().getContentResolver(), gateway.findById(uuid));
    }
}

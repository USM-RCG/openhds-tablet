package org.openhds.mobile.fragment.navigate.detail;

import java.util.List;

import android.widget.ScrollView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.navconfig.ProjectResources;

import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;

public class IndividualDetailFragment extends DetailFragment {

    private static final int LABEL_COLOR = R.color.DetailLabel;
    private static final int VALUE_COLOR = R.color.DetailValue;
    private static final int MISSING_COLOR = R.color.DetailMissing;

    ScrollView detailContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        detailContainer = (ScrollView) inflater.inflate(R.layout.individual_detail_fragment, container, false);
        return detailContainer;
    }

    @Override
    public void setUpDetails(DataWrapper data) {

        Individual individual = getIndividual(data.getUuid());

        List<Membership> memberships = getMemberships(individual.getExtId());

        LinearLayout personalInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_personal_info);

        LinearLayout contactInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_contact_info);

        LinearLayout membershipInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_membership_info);

        personalInfoContainer.removeAllViews();
        contactInfoContainer.removeAllViews();
        membershipInfoContainer.removeAllViews();

        // Draw extId
        TextView extIdTextView = (TextView) detailContainer.findViewById(R.id.individual_detail_frag_extid);
        extIdTextView.setText(individual.getExtId());

        // Name
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_full_name_label,
                individual.getFirstName() + " " + individual.getLastName(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
        // Other names
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_names_label,
                individual.getOtherNames(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.gender_lbl,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getGender())),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        // Language Preference
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_language_preference_label,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getLanguagePreference())),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        // Nationality
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_nationality_label,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getNationality())),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        // age and birthday
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_age_label,
                Individual.getAgeWithUnits(individual),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_date_of_birth_label,
                individual.getDob(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        //UUID
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.uuid,
                individual.getUuid(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        // Contact Info
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_personal_phone_number_label,
                individual.getPhoneNumber(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_phone_number_label,
                individual.getOtherPhoneNumber(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_label,
                individual.getPointOfContactName(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_phone_number_label,
                individual.getPointOfContactPhoneNumber(),
                LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));

        if (!memberships.isEmpty()) {
            for (Membership membership : memberships) {
                membershipInfoContainer.addView(makeLargeTextWithValueAndLabel(
                        getActivity(),
                        R.string.individual_relationship_to_head_label,
                        getString(ProjectResources.Relationship.getRelationshipStringId(membership.getRelationshipToHead())),
                        LABEL_COLOR, VALUE_COLOR, MISSING_COLOR));
            }
        } else {
            membershipInfoContainer.setVisibility(View.GONE);
        }
    }

    private Individual getIndividual(String uuid) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        return individualGateway.getFirst(getActivity().getContentResolver(), individualGateway.findById(uuid));
    }

    private List<Membership> getMemberships(String individualExtId) {
        MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
        return membershipGateway.getList(getActivity().getContentResolver(), membershipGateway.findByIndividual(individualExtId));
    }
}

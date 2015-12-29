package org.openhds.mobile.fragment.navigate.detail;

import java.util.List;

import android.content.ContentResolver;
import android.widget.ScrollView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.projectdata.ProjectResources;

import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;

public class IndividualDetailFragment extends DetailFragment {

    ScrollView detailContainer;
    int labelColor;
    int valueColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        detailContainer = (ScrollView) inflater.inflate(R.layout.individual_detail_fragment, container, false);

        labelColor = R.color.GrayButtonBorder;
        valueColor = R.color.GrayButtonFillPressed;

        return detailContainer;
    }

    @Override
    public void setUpDetails() {

        Individual individual = getIndividual(navigateActivity.getCurrentSelection().getUuid());

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
                labelColor, valueColor, R.color.NA_Gray));
        // Other names
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_names_label,
                individual.getOtherNames(),
                labelColor, valueColor, R.color.NA_Gray));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.gender_lbl,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getGender())),
                labelColor, valueColor, R.color.NA_Gray));

        // Language Preference
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_language_preference_label,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getLanguagePreference())),
                labelColor, valueColor, R.color.NA_Gray));

        // Nationality
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_nationality_label,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getNationality())),
                labelColor, valueColor, R.color.NA_Gray));

        // age and birthday
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_age_label,
                Individual.getAgeWithUnits(individual),
                labelColor, valueColor, R.color.NA_Gray));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_date_of_birth_label,
                individual.getDob(),
                labelColor, valueColor, R.color.NA_Gray));

        //UUID
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.uuid,
                individual.getUuid(),
                labelColor, valueColor, R.color.NA_Gray));

        // Contact Info
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_personal_phone_number_label,
                individual.getPhoneNumber(),
                labelColor, valueColor, R.color.NA_Gray));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_phone_number_label,
                individual.getOtherPhoneNumber(),
                labelColor, valueColor, R.color.NA_Gray));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_label,
                individual.getPointOfContactName(),
                labelColor, valueColor, R.color.NA_Gray));
        contactInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_phone_number_label,
                individual.getPointOfContactPhoneNumber(),
                labelColor, valueColor, R.color.NA_Gray));

        // Memberships
        for (Membership membership : memberships) {

            membershipInfoContainer.addView(makeLargeTextWithValueAndLabel(
                    getActivity(),
                    R.string.individual_relationship_to_head_label,
                    getString(ProjectResources.Relationship.getRelationshipStringId(membership.getRelationshipToHead())),
                    labelColor, valueColor, R.color.NA_Gray));

        }
    }

    private Individual getIndividual(String uuid) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        Individual individual = individualGateway.getFirst(contentResolver, individualGateway.findById(uuid));

        return individual;
    }

    private List<Membership> getMemberships(String individualExtId) {
        MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        List<Membership> memberships = membershipGateway.getList(contentResolver,
                membershipGateway.findByIndividual(individualExtId));

        return memberships;
    }
}

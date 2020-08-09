package org.cimsbioko.fragment.navigate.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.cimsbioko.R
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.data.GatewayRegistry.individualGateway
import org.cimsbioko.model.core.Individual
import org.cimsbioko.navconfig.forms.KnownValues
import org.cimsbioko.utilities.configureTextWithLabel
import org.cimsbioko.utilities.makeLargeTextWithLabel

class IndividualDetailFragment : DetailFragment() {

    private lateinit var detailContainer: ScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        detailContainer = inflater.inflate(R.layout.individual_detail_fragment, container, false) as ScrollView
        return detailContainer
    }

    override fun setUpDetails(data: DataWrapper?) {
        data?.let { d ->
            getIndividual(d.uuid)?.let { i ->
                setBannerText(i.extId)
                rebuildPersonalDetails(i)
                rebuildContactDetails(i)
            }
        }
    }

    private fun setBannerText(text: String?) {
        detailContainer.findViewById<TextView>(R.id.individual_detail_frag_extid).text = text
    }

    private fun rebuildPersonalDetails(individual: Individual) {
        detailContainer.findViewById<LinearLayout>(R.id.individual_detail_frag_personal_info)?.apply {
            removeAllViews()
            addTextView(R.string.individual_full_name_label, individual.firstName + " " + individual.lastName)
            addTextView(R.string.individual_other_names_label, individual.otherNames)
            addTextView(R.string.gender_lbl, decodedLabel(KnownValues.Individual.getLabel(individual.gender!!)))
            addTextView(R.string.individual_language_preference_label, decodedLabel(KnownValues.Individual.getLabel(individual.languagePreference!!)))
            addTextView(R.string.individual_nationality_label, decodedLabel(KnownValues.Individual.getLabel(individual.nationality!!)))
            addTextView(R.string.individual_date_of_birth_label, individual.dob)
            addTextView(R.string.uuid, individual.uuid)
            addTextView(R.string.individual_status_label, decodedLabel(KnownValues.Individual.getLabel(individual.status!!)))
            addTextView(R.string.individual_relationship_to_head_label, decodedLabel(KnownValues.Relationship.getLabel(individual.relationshipToHead!!)))
        }
    }

    private fun decodedLabel(resId: Int?): String? {
        return resId?.let { getString(it) }
    }

    private fun rebuildContactDetails(individual: Individual) {
        detailContainer.findViewById<LinearLayout>(R.id.individual_detail_frag_contact_info)?.apply {
            removeAllViews()
            addTextView(R.string.individual_personal_phone_number_label, individual.phoneNumber)
            addTextView(R.string.individual_other_phone_number_label, individual.otherPhoneNumber)
            addTextView(R.string.individual_point_of_contact_label, individual.pointOfContactName)
            addTextView(R.string.individual_point_of_contact_phone_number_label, individual.pointOfContactPhoneNumber)
        }
    }

    private fun LinearLayout.addTextView(label: Int, value: String?) {
        activity?.let {
            addView(makeLargeTextWithLabel(it).apply {
                configureTextWithLabel(label, value, LABEL_COLOR, VALUE_COLOR, MISSING_COLOR)
            })
        }
    }

    private fun getIndividual(uuid: String): Individual? {
        return individualGateway.findById(uuid).first
    }

    companion object {
        private const val LABEL_COLOR = R.color.DarkGray
        private const val VALUE_COLOR = R.color.White
        private const val MISSING_COLOR = R.color.Gray
    }
}
package org.cimsbioko.navconfig.forms

import org.cimsbioko.R

object KnownValues {

    object Individual {

        private val labels: Map<String, Int> = mapOf(
                "Male" to R.string.db_val_gender_male,
                "Female" to R.string.db_val_gender_female,
                "Spanish" to R.string.db_val_language_pref_spanish,
                "Fang" to R.string.db_val_language_pref_fang,
                "Bubi" to R.string.db_val_language_pref_bubi,
                "English" to R.string.db_val_language_pref_english,
                "French" to R.string.db_val_language_pref_french,
                "Permanent" to R.string.db_val_status_permanent,
                "Visitor" to R.string.db_val_status_visitor,
                "Equato Guinean" to R.string.db_val_nationality_equato_guinean,
                "Other African Country" to R.string.db_val_nationality_other_african_country,
                "Asian" to R.string.db_val_nationality_asian,
                "Other Non-African Country" to R.string.db_val_nationality_other_non_african_country,
                "Other" to R.string.db_val_nationality_other,
                "notApplicable" to R.string.not_available
        )

        @JvmStatic
        fun getLabel(value: String) = labels[value]
    }

    object Relationship {

        private val labels: Map<String, Int> = mapOf(
                "1" to R.string.db_val_relation_to_head_type_head,
                "2" to R.string.db_val_relation_to_head_type_spouse,
                "3" to R.string.db_val_relation_to_head_type_son_daughter,
                "4" to R.string.db_val_relation_to_head_type_brother_sister,
                "5" to R.string.db_val_relation_to_head_type_parent,
                "6" to R.string.db_val_relation_to_head_type_grandchild,
                "7" to R.string.db_val_relation_to_head_type_not_related,
                "8" to R.string.db_val_relation_to_head_type_other_relative,
                "9" to R.string.db_val_relation_to_head_type_dont_know,
                "10" to R.string.db_val_relation_to_head_type_cousin,
                "11" to R.string.db_val_relation_to_head_type_nephew_niece
        )

        @JvmStatic
        fun getLabel(value: String) = labels[value]
    }
}
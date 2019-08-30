package org.cimsbioko.navconfig;

import java.util.HashMap;
import java.util.Map;

import org.cimsbioko.R;

public class ProjectResources {

    public static class General {
        public static final String FORM_NEEDS_REVIEW = "1";
        public static final String FORM_NO_REVIEW_NEEDED = "0";
    }

    public static final class Individual {

        static final Map<String, Integer> valueLabels = new HashMap<>();

        static final String GENDER_MALE = "Male";
        static final String GENDER_FEMALE = "Female";

        static final String LANGUAGE_PREF_SPANISH = "Spanish";
        static final String LANGUAGE_PREF_FANG = "Fang";
        static final String LANGUAGE_PREF_BUBE = "Bubi";
        static final String LANGUAGE_PREF_ENGLISH = "English";
        static final String LANGUAGE_PREF_FRENCH = "French";

        static final String NATIONALITY_EQUATO_GUINEAN = "Equato Guinean";
        static final String NATIONALITY_OTHER_AFRICAN_COUNTRY = "Other African Country";
        static final String NATIONALITY_ASIAN = "Asian";
        static final String NATIONALITY_OTHER_NON_AFRICAN_COUNTRY = "Other Non-African Country";
        static final String NATIONALITY_OTHER = "Other";
        static final String NOT_APPLICABLE = "notApplicable";

        private static final String STATUS_PERMANENT = "Permanent";
        private static final String STATUS_VISITOR = "Visitor";

        static {
            valueLabels.put(GENDER_MALE, R.string.db_val_gender_male);
            valueLabels.put(GENDER_FEMALE, R.string.db_val_gender_female);
            valueLabels.put(LANGUAGE_PREF_SPANISH,R.string.db_val_language_pref_spanish);
            valueLabels.put(LANGUAGE_PREF_FANG, R.string.db_val_language_pref_fang);
            valueLabels.put(LANGUAGE_PREF_BUBE, R.string.db_val_language_pref_bubi);
            valueLabels.put(LANGUAGE_PREF_ENGLISH,R.string.db_val_language_pref_english);
            valueLabels.put(LANGUAGE_PREF_FRENCH, R.string.db_val_language_pref_french);
            valueLabels.put(STATUS_PERMANENT, R.string.db_val_status_permanent);
            valueLabels.put(STATUS_VISITOR, R.string.db_val_status_visitor);
            valueLabels.put(NATIONALITY_EQUATO_GUINEAN, R.string.db_val_nationality_equato_guinean);
            valueLabels.put(NATIONALITY_OTHER_AFRICAN_COUNTRY, R.string.db_val_nationality_other_african_country);
            valueLabels.put(NATIONALITY_ASIAN, R.string.db_val_nationality_asian);
            valueLabels.put(NATIONALITY_OTHER_NON_AFRICAN_COUNTRY, R.string.db_val_nationality_other_non_african_country);
            valueLabels.put(NATIONALITY_OTHER, R.string.db_val_nationality_other);
            valueLabels.put(NOT_APPLICABLE, R.string.not_available);
        }

        public static int getIndividualStringId(String key) {
            if (key != null && valueLabels.containsKey(key)) {
                return valueLabels.get(key);
            } else {
                return 0;
            }
        }
    }

    public static final class Relationship {

        private static final Map<String, Integer> valueLabels = new HashMap<>();

        static final String RELATION_TO_HOH_TYPE_HEAD = "1";
        static final String RELATION_TO_HOH_TYPE_SPOUSE = "2";
        static final String RELATION_TO_HOH_TYPE_SON_DAUGHTER = "3";
        static final String RELATION_TO_HOH_TYPE_BROTHER_SISTER = "4";
        static final String RELATION_TO_HOH_TYPE_PARENT = "5";
        static final String RELATION_TO_HOH_TYPE_GRANDCHILD = "6";
        static final String RELATION_TO_HOH_TYPE_NOT_RELATED = "7";
        static final String RELATION_TO_HOH_TYPE_OTHER_RELATIVE = "8";
        static final String RELATION_TO_HOH_TYPE_DONT_KNOW = "9";
        static final String RELATION_TO_HOH_TYPE_COUSIN = "10";
        static final String RELATION_TO_HOH_TYPE_NEPHEW_NIECE = "11";

        static {
            valueLabels.put(RELATION_TO_HOH_TYPE_HEAD, R.string.db_val_relation_to_head_type_head);
            valueLabels.put(RELATION_TO_HOH_TYPE_SPOUSE, R.string.db_val_relation_to_head_type_spouse);
            valueLabels.put(RELATION_TO_HOH_TYPE_SON_DAUGHTER, R.string.db_val_relation_to_head_type_son_daughter);
            valueLabels.put(RELATION_TO_HOH_TYPE_BROTHER_SISTER, R.string.db_val_relation_to_head_type_brother_sister);
            valueLabels.put(RELATION_TO_HOH_TYPE_COUSIN, R.string.db_val_relation_to_head_type_cousin);
            valueLabels.put(RELATION_TO_HOH_TYPE_NEPHEW_NIECE, R.string.db_val_relation_to_head_type_nephew_niece);
            valueLabels.put(RELATION_TO_HOH_TYPE_PARENT, R.string.db_val_relation_to_head_type_parent);
            valueLabels.put(RELATION_TO_HOH_TYPE_GRANDCHILD, R.string.db_val_relation_to_head_type_grandchild);
            valueLabels.put(RELATION_TO_HOH_TYPE_NOT_RELATED, R.string.db_val_relation_to_head_type_not_related);
            valueLabels.put(RELATION_TO_HOH_TYPE_OTHER_RELATIVE, R.string.db_val_relation_to_head_type_other_relative);
            valueLabels.put(RELATION_TO_HOH_TYPE_DONT_KNOW, R.string.db_val_relation_to_head_type_dont_know);
        }

        public static int getRelationshipStringId(String key) {
            if (key != null && valueLabels.containsKey(key)) {
                return valueLabels.get(key);
            } else {
                return 0;
            }
        }
    }
}
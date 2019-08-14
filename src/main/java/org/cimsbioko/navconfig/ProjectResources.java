package org.cimsbioko.navconfig;

import java.util.HashMap;
import java.util.Map;

import org.cimsbioko.R;

public class ProjectResources {

    public static class General {

        public static final Map<String, Integer> General = new HashMap<>();

        public static final String FORM_NEEDS_REVIEW = "1";
        public static final String FORM_NO_REVIEW_NEEDED = "0";

        public static final String TRUE = "true";
        public static final String FALSE = "false";

        static{
            General.put(TRUE, R.string.db_val_true);
            General.put(FALSE, R.string.db_val_false);
        }

        public static int getGeneralStringId(String key) {
            if (General.containsKey(key)) {
                return General.get(key);
            } else {
                return 0;
            }
        }
    }

    public static final class Individual {

        public static final Map<String, Integer> Individual = new HashMap<>();

        public static final String GENDER_MALE = "Male";
        public static final String GENDER_FEMALE = "Female";

        public static final String LANGUAGE_PREF_SPANISH = "Spanish";
        public static final String LANGUAGE_PREF_FANG = "Fang";
        public static final String LANGUAGE_PREF_BUBE = "Bubi";
        public static final String LANGUAGE_PREF_ENGLISH = "English";
        public static final String LANGUAGE_PREF_FRENCH = "French";
        public static final String LANGUAGE_PREF_OTHER = "Other";

        public static final String NATIONALITY_EQUATO_GUINEAN = "Equato Guinean";
        public static final String NATIONALITY_OTHER_AFRICAN_COUNTRY = "Other African Country";
        public static final String NATIONALITY_ASIAN = "Asian";
        public static final String NATIONALITY_OTHER_NON_AFRICAN_COUNTRY = "Other Non-African Country";
        public static final String NATIONALITY_OTHER = "Other";
        public static final String NOT_APPLICABLE = "notApplicable";

        private static final String STATUS_PERMANENT = "Permanent";
        private static final String STATUS_VISITOR = "Visitor";

        static {
            Individual.put(GENDER_MALE, R.string.db_val_gender_male);
            Individual.put(GENDER_FEMALE, R.string.db_val_gender_female);
            Individual.put(LANGUAGE_PREF_SPANISH,R.string.db_val_language_pref_spanish);
            Individual.put(LANGUAGE_PREF_FANG, R.string.db_val_language_pref_fang);
            Individual.put(LANGUAGE_PREF_BUBE, R.string.db_val_language_pref_bubi);
            Individual.put(LANGUAGE_PREF_ENGLISH,R.string.db_val_language_pref_english);
            Individual.put(LANGUAGE_PREF_FRENCH, R.string.db_val_language_pref_french);
            Individual.put(LANGUAGE_PREF_OTHER, R.string.db_val_language_pref_other);
            Individual.put(STATUS_PERMANENT, R.string.db_val_status_permanent);
            Individual.put(STATUS_VISITOR, R.string.db_val_status_visitor);
            Individual.put(NATIONALITY_EQUATO_GUINEAN, R.string.db_val_nationality_equato_guinean);
            Individual.put(NATIONALITY_OTHER_AFRICAN_COUNTRY, R.string.db_val_nationality_other_african_country);
            Individual.put(NATIONALITY_ASIAN, R.string.db_val_nationality_asian);
            Individual.put(NATIONALITY_OTHER_NON_AFRICAN_COUNTRY, R.string.db_val_nationality_other_non_african_country);
            Individual.put(NATIONALITY_OTHER, R.string.db_val_nationality_other);
            Individual.put(NOT_APPLICABLE, R.string.not_available);
        }

        public static int getIndividualStringId(String key) {
            if (key != null && Individual.containsKey(key)) {
                return Individual.get(key);
            } else {
                return 0;
            }
        }
    }

    public static final class Location {

        public static final Map<String, Integer> Location = new HashMap<>();

        private static final String STATUS_NEVER_AVAILABLE = "neverAvailable";
        private static final String STATUS_REJECTED_INTERVENTION = "rejectedIntervention";
        private static final String STATUS_UNINHABITED = "uninhabited";

        static {
            Location.put(STATUS_NEVER_AVAILABLE, R.string.db_val_location_status_never_available);
            Location.put(STATUS_REJECTED_INTERVENTION, R.string.db_val_location_status_rejected_intervention);
            Location.put(STATUS_UNINHABITED, R.string.db_val_location_status_uninhabited);
        }

        public static int getLocationStringId(String key) {
            if (key != null && Location.containsKey(key)) {
                return Location.get(key);
            } else {
                return 0;
            }
        }
    }

    public static final class Relationship {

        private static final Map<String, Integer> Relationship = new HashMap<>();

        public static final String RELATION_TO_HOH_TYPE_HEAD = "1";
        public static final String RELATION_TO_HOH_TYPE_SPOUSE = "2";
        public static final String RELATION_TO_HOH_TYPE_SON_DAUGHTER = "3";
        public static final String RELATION_TO_HOH_TYPE_BROTHER_SISTER = "4";
        public static final String RELATION_TO_HOH_TYPE_PARENT = "5";
        public static final String RELATION_TO_HOH_TYPE_GRANDCHILD = "6";
        public static final String RELATION_TO_HOH_TYPE_NOT_RELATED = "7";
        public static final String RELATION_TO_HOH_TYPE_OTHER_RELATIVE = "8";
        public static final String RELATION_TO_HOH_TYPE_DONT_KNOW = "9";
        public static final String RELATION_TO_HOH_TYPE_COUSIN = "10";
        public static final String RELATION_TO_HOH_TYPE_NEPHEW_NIECE = "11";

        static {
            Relationship.put(RELATION_TO_HOH_TYPE_HEAD, R.string.db_val_relation_to_head_type_head);
            Relationship.put(RELATION_TO_HOH_TYPE_SPOUSE, R.string.db_val_relation_to_head_type_spouse);
            Relationship.put(RELATION_TO_HOH_TYPE_SON_DAUGHTER, R.string.db_val_relation_to_head_type_son_daughter);
            Relationship.put(RELATION_TO_HOH_TYPE_BROTHER_SISTER, R.string.db_val_relation_to_head_type_brother_sister);
            Relationship.put(RELATION_TO_HOH_TYPE_COUSIN, R.string.db_val_relation_to_head_type_cousin);
            Relationship.put(RELATION_TO_HOH_TYPE_NEPHEW_NIECE, R.string.db_val_relation_to_head_type_nephew_niece);
            Relationship.put(RELATION_TO_HOH_TYPE_PARENT, R.string.db_val_relation_to_head_type_parent);
            Relationship.put(RELATION_TO_HOH_TYPE_GRANDCHILD, R.string.db_val_relation_to_head_type_grandchild);
            Relationship.put(RELATION_TO_HOH_TYPE_NOT_RELATED, R.string.db_val_relation_to_head_type_not_related);
            Relationship.put(RELATION_TO_HOH_TYPE_OTHER_RELATIVE, R.string.db_val_relation_to_head_type_other_relative);
            Relationship.put(RELATION_TO_HOH_TYPE_DONT_KNOW, R.string.db_val_relation_to_head_type_dont_know);
        }

        public static int getRelationshipStringId(String key) {
            if (key != null && Relationship.containsKey(key)) {
                return Relationship.get(key);
            } else {
                return 0;
            }
        }
    }
}
package org.openhds.mobile.projectdata;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.R;

public class ProjectResources {

	public static final class Individual {

		/*
		 * INDIVIDUAL mapping of Database/Form VALUES to String resources IDs
		 */

		private static final Map<String, Integer> Individual = new HashMap<String, Integer>();

		private static final String AGE_UNITS_YEARS = "Years";
		private static final String AGE_UNITS_MONTHS = "Months";

		private static final String GENDER_MALE = "Male";
		private static final String GENDER_FEMALE = "Female";

		private static final String LANGUAGE_PREF_SPANISH = "Spanish";
		private static final String LANGUAGE_PREF_FANG = "Fang";
		private static final String LANGUAGE_PREF_BUBE = "Bube";
		private static final String LANGUAGE_PREF_ENGLISH = "English";
		private static final String LANGUAGE_PREF_FRENCH = "French";

		private static final String STATUS_PERMANENT = "Permanent";
		private static final String STATUS_VISITOR = "Visitor";
		static {
			Individual.put(AGE_UNITS_YEARS, R.string.db_val_age_units_years);
			Individual.put(AGE_UNITS_MONTHS, R.string.db_val_age_units_months);
			Individual.put(GENDER_MALE, R.string.db_val_gender_male);
			Individual.put(GENDER_FEMALE, R.string.db_val_gender_female);
			Individual.put(LANGUAGE_PREF_SPANISH,
					R.string.db_val_language_pref_spanish);
			Individual.put(LANGUAGE_PREF_FANG,
					R.string.db_val_language_pref_fang);
			Individual.put(LANGUAGE_PREF_BUBE,
					R.string.db_val_language_pref_bube);
			Individual.put(LANGUAGE_PREF_ENGLISH,
					R.string.db_val_language_pref_english);
			Individual.put(LANGUAGE_PREF_FRENCH,
					R.string.db_val_language_pref_french);
			Individual.put(STATUS_PERMANENT, R.string.db_val_status_permanent);
			Individual.put(STATUS_VISITOR, R.string.db_val_status_visitor);
		}

		public static int getIndividualStringId(String value) {
			if (Individual.containsKey(value)) {
				return Individual.get(value);
			} else {
				return 0;
			}
		}
	}

	public static final class Relationship {
		/*
		 * RELATIONSHIPS mapping of Database/Form VALUES to String resources IDs
		 */

		private static final Map<String, Integer> Relationship = new HashMap<String, Integer>();

		private static final String RELATION_TO_HOH_TYPE_HEAD = "1";
		private static final String RELATION_TO_HOH_TYPE_SPOUSE = "2";
		private static final String RELATION_TO_HOH_TYPE_SON_DAUGHTER = "3";
		private static final String RELATION_TO_HOH_TYPE_BROTHER_SISTER = "4";
		private static final String RELATION_TO_HOH_TYPE_PARENT = "5";
		private static final String RELATION_TO_HOH_TYPE_GRANDCHILD = "6";
		private static final String RELATION_TO_HOH_TYPE_NOT_RELATED = "7";
		private static final String RELATION_TO_HOH_TYPE_OTHER_RELATIVE = "8";
		private static final String RELATION_TO_HOH_TYPE_DONT_KNOW = "9";

		static {
			Relationship.put(RELATION_TO_HOH_TYPE_HEAD,
					R.string.db_val_relation_to_head_type_head);
			Relationship.put(RELATION_TO_HOH_TYPE_SPOUSE,
					R.string.db_val_relation_to_head_type_spouse);
			Relationship.put(RELATION_TO_HOH_TYPE_SON_DAUGHTER,
					R.string.db_val_relation_to_head_type_son_daughter);
			Relationship.put(RELATION_TO_HOH_TYPE_BROTHER_SISTER,
					R.string.db_val_relation_to_head_type_brother_sister);
			Relationship.put(RELATION_TO_HOH_TYPE_PARENT,
					R.string.db_val_relation_to_head_type_parent);
			Relationship.put(RELATION_TO_HOH_TYPE_GRANDCHILD,
					R.string.db_val_relation_to_head_type_grandchild);
			Relationship.put(RELATION_TO_HOH_TYPE_NOT_RELATED,
					R.string.db_val_relation_to_head_type_not_related);
			Relationship.put(RELATION_TO_HOH_TYPE_OTHER_RELATIVE,
					R.string.db_val_relation_to_head_type_other_relative);
			Relationship.put(RELATION_TO_HOH_TYPE_DONT_KNOW,
					R.string.db_val_relation_to_head_type_dont_know);
		}

		public static int getRelationshipStringId(String value) {
			if (Relationship.containsKey(value)) {
				return Relationship.get(value);
			} else {
				return 0;
			}
		}

	}

}

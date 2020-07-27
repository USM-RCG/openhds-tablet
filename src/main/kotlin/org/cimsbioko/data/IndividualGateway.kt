package org.cimsbioko.data

import android.content.ContentValues
import android.database.Cursor
import org.cimsbioko.App.Individuals
import org.cimsbioko.R
import org.cimsbioko.data.CursorConvert.extractString
import org.cimsbioko.model.core.Individual
import org.cimsbioko.navconfig.Hierarchy

/**
 * Convert Individuals to and from database.  Individual-specific queries.
 */
class IndividualGateway internal constructor()
    : Gateway<Individual>(Individuals.CONTENT_ID_URI_BASE, Individuals.COLUMN_INDIVIDUAL_UUID) {

    fun findByResidency(residencyId: String): Query<Individual> =
            Query<Individual>(this, tableUri, Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, residencyId, Individuals.COLUMN_INDIVIDUAL_EXTID)

    override fun getId(entity: Individual): String = entity.uuid!!

    override val entityConverter: CursorConverter<Individual> by lazy { IndividualEntityConverter() }
    override val wrapperConverter: CursorConverter<DataWrapper> by lazy { IndividualWrapperConverter() }
    override val contentValuesConverter: ContentValuesConverter<Individual> by lazy { IndividualContentValuesConverter() }
}

private class IndividualEntityConverter : CursorConverter<Individual> {
    override fun convert(c: Cursor): Individual = Individual().apply {
        uuid = extractString(c, Individuals.COLUMN_INDIVIDUAL_UUID)
        extId = extractString(c, Individuals.COLUMN_INDIVIDUAL_EXTID)
        firstName = extractString(c, Individuals.COLUMN_INDIVIDUAL_FIRST_NAME)
        lastName = extractString(c, Individuals.COLUMN_INDIVIDUAL_LAST_NAME)
        dob = extractString(c, Individuals.COLUMN_INDIVIDUAL_DOB)
        gender = extractString(c, Individuals.COLUMN_INDIVIDUAL_GENDER)
        currentResidenceUuid = extractString(c, Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID)
        otherId = extractString(c, Individuals.COLUMN_INDIVIDUAL_OTHER_ID)
        otherNames = extractString(c, Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES)
        phoneNumber = extractString(c, Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER)
        otherPhoneNumber = extractString(c, Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)
        pointOfContactName = extractString(c, Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME)
        status = extractString(c, Individuals.COLUMN_INDIVIDUAL_STATUS)
        pointOfContactPhoneNumber = extractString(c, Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER)
        languagePreference = extractString(c, Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)
        nationality = extractString(c, Individuals.COLUMN_INDIVIDUAL_NATIONALITY)
        relationshipToHead = extractString(c, Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD)
        attrs = extractString(c, Individuals.COLUMN_INDIVIDUAL_ATTRS)
    }
}

private class IndividualWrapperConverter : CursorConverter<DataWrapper> {
    override fun convert(c: Cursor): DataWrapper = DataWrapper(
            extractString(c, Individuals.COLUMN_INDIVIDUAL_UUID)!!,
            Hierarchy.INDIVIDUAL,
            extractString(c, Individuals.COLUMN_INDIVIDUAL_EXTID)!!,
            extractString(c, Individuals.COLUMN_INDIVIDUAL_FIRST_NAME) + " " + extractString(c, Individuals.COLUMN_INDIVIDUAL_LAST_NAME)
    ).apply {
        stringsPayload.apply {
            put(R.string.individual_other_names_label, extractString(c, Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES))
            put(R.string.individual_language_preference_label, extractString(c, Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE))
        }
    }
}

private class IndividualContentValuesConverter : ContentValuesConverter<Individual> {
    override fun toContentValues(entity: Individual): ContentValues = ContentValues().apply {
        with(entity) {
            put(Individuals.COLUMN_INDIVIDUAL_UUID, uuid)
            put(Individuals.COLUMN_INDIVIDUAL_EXTID, extId)
            put(Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, firstName)
            put(Individuals.COLUMN_INDIVIDUAL_LAST_NAME, lastName)
            put(Individuals.COLUMN_INDIVIDUAL_DOB, dob)
            put(Individuals.COLUMN_INDIVIDUAL_GENDER, gender)
            put(Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, currentResidenceUuid)
            put(Individuals.COLUMN_INDIVIDUAL_OTHER_ID, otherId)
            put(Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, otherNames)
            put(Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, phoneNumber)
            put(Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, otherPhoneNumber)
            put(Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, pointOfContactName)
            put(Individuals.COLUMN_INDIVIDUAL_STATUS, status)
            put(Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, pointOfContactPhoneNumber)
            put(Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, languagePreference)
            put(Individuals.COLUMN_INDIVIDUAL_NATIONALITY, nationality)
            put(Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD, relationshipToHead)
            put(Individuals.COLUMN_INDIVIDUAL_ATTRS, attrs)
        }
    }
}
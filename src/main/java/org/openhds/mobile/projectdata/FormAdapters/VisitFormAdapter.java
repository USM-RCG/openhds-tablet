package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_UUID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_DATE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_UUID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_LOCATION_UUID;

public class VisitFormAdapter {

    public static Visit fromForm(Map<String, String> formInstanceData){
        Visit visit = new Visit();

        visit.setUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_UUID)));
        visit.setExtId(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_EXTID)));
        visit.setVisitDate(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_DATE)));
        visit.setFieldWorkerUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_FIELDWORKER_UUID)));
        visit.setLocationUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_LOCATION_UUID)));

        return visit;
    }
}

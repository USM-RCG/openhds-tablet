package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to FieldWorkers.
 */
public class FieldWorkerParser extends EntityParser<FieldWorker> {

    private static final String pageName = "fieldworker";

    @Override
    protected FieldWorker toEntity(DataPage dataPage) {
        FieldWorker fieldWorker = new FieldWorker();
        fieldWorker.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        fieldWorker.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        fieldWorker.setIdPrefix(dataPage.getFirstString(asList(pageName, "id")));
        fieldWorker.setPasswordHash(dataPage.getFirstString(asList(pageName, "pass")));
        fieldWorker.setFirstName(dataPage.getFirstString(asList(pageName, "firstName")));
        fieldWorker.setLastName(dataPage.getFirstString(asList(pageName, "lastName")));
        return fieldWorker;
    }
}

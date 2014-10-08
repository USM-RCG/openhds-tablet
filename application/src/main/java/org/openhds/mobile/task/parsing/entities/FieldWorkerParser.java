package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.FieldWorker;
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

        fieldWorker.setPasswordHash(dataPage.getFirstString(asList(pageName, "passwordHash")));
        fieldWorker.setIdPrefix(dataPage.getFirstString(asList(pageName, "idPrefix")));
        fieldWorker.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        fieldWorker.setFirstName(dataPage.getFirstString(asList(pageName, "firstName")));
        fieldWorker.setLastName(dataPage.getFirstString(asList(pageName, "lastName")));

        // TODO: this is a temporary hack
        if (null == fieldWorker.getCollectedIdPrefix()) {
            fieldWorker.setIdPrefix("99");
        }

        return fieldWorker;
    }
}

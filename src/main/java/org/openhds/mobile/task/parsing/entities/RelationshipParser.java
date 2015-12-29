package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Relationships.
 */
public class RelationshipParser extends EntityParser<Relationship> {

    private static final String pageName = "relationship";

    @Override
    protected Relationship toEntity(DataPage dataPage) {
        Relationship relationship = new Relationship();

        relationship.setIndividualAUuid(dataPage.getFirstString(asList(pageName, "individualA", "uuid")));
        relationship.setIndividualBUuid(dataPage.getFirstString(asList(pageName, "individualB", "uuid")));
        relationship.setStartDate(dataPage.getFirstString(asList(pageName, "startDate")));
        relationship.setType(dataPage.getFirstString(asList(pageName, "aIsToB")));
        relationship.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));

        return relationship;
    }
}

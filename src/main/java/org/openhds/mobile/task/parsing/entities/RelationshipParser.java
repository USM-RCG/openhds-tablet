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
        relationship.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        relationship.setIndividualAUuid(dataPage.getFirstString(asList(pageName, "indivA")));
        relationship.setIndividualBUuid(dataPage.getFirstString(asList(pageName, "indivB")));
        relationship.setStartDate(dataPage.getFirstString(asList(pageName, "startDate")));
        relationship.setType(dataPage.getFirstString(asList(pageName, "aIsToB")));
        return relationship;
    }
}

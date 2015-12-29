package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Memberships.
 */
public class MembershipParser extends EntityParser<Membership> {

    private static final String pageName = "membership";

    @Override
    protected Membership toEntity(DataPage dataPage) {
        Membership membership = new Membership();

        membership.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        membership.setIndividualUuid(dataPage.getFirstString(asList(pageName, "individual", "uuid")));
        membership.setRelationshipToHead(dataPage.getFirstString(asList(pageName, "bIsToA")));
        membership.setSocialGroupUuid(dataPage.getFirstString(asList(pageName, "socialGroup", "uuid")));

        return membership;
    }
}

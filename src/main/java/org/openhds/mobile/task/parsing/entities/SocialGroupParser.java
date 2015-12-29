package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to SocialGroups.
 */
public class SocialGroupParser extends EntityParser<SocialGroup> {

    private static final String pageName = "socialgroup";

    @Override
    protected SocialGroup toEntity(DataPage dataPage) {
        SocialGroup socialGroup = new SocialGroup();

        socialGroup.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        socialGroup.setGroupHeadUuid(dataPage.getFirstString(asList(pageName, "groupHead", "uuid")));
        socialGroup.setLocationUuid(dataPage.getFirstString(asList(pageName, "location", "uuid")));
        socialGroup.setGroupName(dataPage.getFirstString(asList(pageName, "groupName")));

        return socialGroup;
    }
}

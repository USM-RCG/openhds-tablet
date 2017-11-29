package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;


public class SocialGroupGatewayTest extends GatewayTest<SocialGroup> {

    public SocialGroupGatewayTest() {
        super(new SocialGroupGateway());
    }

    @Override
    protected SocialGroup makeTestEntity(String id, String name) {
        SocialGroup socialGroup = new SocialGroup();
        socialGroup.setUuid(id);
        socialGroup.setLocationUuid("LOCATION");
        socialGroup.setGroupName(name);
        socialGroup.setGroupHeadUuid("HEAD");
        return socialGroup;
    }
}

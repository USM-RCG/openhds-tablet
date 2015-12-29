package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.repository.gateway.MembershipGateway;


public class MembershipGatewayTest extends GatewayTest<Membership> {

    private MembershipGateway membershipGateway;

    public MembershipGatewayTest() {
        super(new MembershipGateway());
        this.membershipGateway = (MembershipGateway) this.gateway;
    }

    @Override
    protected Membership makeTestEntity(String id, String name) {
        Membership membership = new Membership();

        membership.setUuid(id);
        membership.setIndividualUuid(id);
        membership.setSocialGroupUuid("SOCIALGROUP");
        membership.setRelationshipToHead("RELATIONSHIPTOHEAD");

        return membership;
    }
}

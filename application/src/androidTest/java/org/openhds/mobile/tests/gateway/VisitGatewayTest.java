package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.repository.gateway.VisitGateway;


public class VisitGatewayTest extends GatewayTest<Visit> {

    private VisitGateway visitGateway;

    public VisitGatewayTest() {
        super(new VisitGateway());
        this.visitGateway = (VisitGateway) this.gateway;
    }

    @Override
    protected Visit makeTestEntity(String id, String name) {
        Visit visit = new Visit();

        visit.setUuid(id);
        visit.setExtId(id);
        visit.setVisitDate("2000-01-01 00:00:00");
        visit.setLocationUuid("LOCATION");
        visit.setFieldWorkerUuid("FIELD_WORKER");

        return visit;
    }
}

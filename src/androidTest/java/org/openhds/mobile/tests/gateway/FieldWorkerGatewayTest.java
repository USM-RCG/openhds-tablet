package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;


public class FieldWorkerGatewayTest extends GatewayTest<FieldWorker> {

    public FieldWorkerGatewayTest() {
        super(new FieldWorkerGateway());
    }

    @Override
    protected FieldWorker makeTestEntity(String id, String name) {
        FieldWorker fieldWorker = new FieldWorker();

        fieldWorker.setUuid(id);
        fieldWorker.setExtId(id);
        fieldWorker.setIdPrefix("00");
        fieldWorker.setFirstName(name);
        fieldWorker.setLastName("LASTNAME");
        fieldWorker.setPasswordHash("PASSWORD_HASH");

        return fieldWorker;
    }
}

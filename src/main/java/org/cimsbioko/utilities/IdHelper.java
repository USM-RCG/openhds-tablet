package org.cimsbioko.utilities;

import org.cimsbioko.model.core.Individual;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.GatewayRegistry;
import org.cimsbioko.repository.gateway.IndividualGateway;

import java.util.List;
import java.util.UUID;

public class IdHelper {

    public static String generateEntityUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateIndividualExtId(DataWrapper location) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        List<Individual> individuals = individualGateway.findByResidency(location.getUuid()).getList();
        int individualsInHousehold = individuals.size() + 1;
        return location.getExtId() + "-" + String.format("%03d", individualsInHousehold);
    }
}

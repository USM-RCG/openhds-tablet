package org.cimsbioko.utilities;

import org.cimsbioko.model.core.Individual;
import org.cimsbioko.data.DataWrapper;

import java.util.List;
import java.util.UUID;

import static org.cimsbioko.data.GatewayRegistry.getIndividualGateway;

public class IdHelper {

    public static String generateEntityUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateIndividualExtId(DataWrapper location) {
        List<Individual> individuals = getIndividualGateway().findByResidency(location.getUuid()).getList();
        int individualsInHousehold = individuals.size() + 1;
        return location.getExtId() + "-" + String.format("%03d", individualsInHousehold);
    }
}
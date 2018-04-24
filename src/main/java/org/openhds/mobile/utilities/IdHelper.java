package org.openhds.mobile.utilities;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.List;
import java.util.UUID;

public class IdHelper {

    public static String generateEntityUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateIndividualExtId(ContentResolver contentResolver, DataWrapper location) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(location.getUuid()));
        int individualsInHousehold = individuals.size() + 1;
        return location.getExtId() + "-" + String.format("%03d", individualsInHousehold);
    }
}

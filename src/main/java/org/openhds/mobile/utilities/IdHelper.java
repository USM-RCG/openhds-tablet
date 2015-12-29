package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.projectdata.QueryHelpers.CensusQueryHelper;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;

/**
 * Created by motech-admin on 12/9/14.
 */
public class IdHelper {

    public static String INDIVIDUAL_ID_FORMAT = LIKE_WILD_CARD + "05d";

    public static String generateEntityUuid(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String generateIndividualExtId(ContentResolver contentResolver, DataWrapper location) {

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(location.getUuid()));
        int individualsInHousehold = individuals.size() + 1;

        // -001
        String suffixSequence = "-"+String.format("%03d", individualsInHousehold);

        // M1000S057E02P1-001
        return location.getExtId()+suffixSequence;

    }


}

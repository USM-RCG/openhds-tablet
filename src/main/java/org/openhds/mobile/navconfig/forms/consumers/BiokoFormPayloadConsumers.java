package org.openhds.mobile.navconfig.forms.consumers;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.navconfig.BiokoHierarchy;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    @UsedByJSConfig
    public static class CreateMap extends DefaultConsumer {
        @Override
        public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationHierarchyGateway hierGateway = GatewayRegistry.getLocationHierarchyGateway();
            ContentResolver resolver = ctx.getContentResolver();

            String localityUuid = formPayload.get(ProjectFormFields.CreateMap.LOCALITY_UUID);
            String mapUuid = formPayload.get(ProjectFormFields.CreateMap.MAP_UUID);
            String mapName = formPayload.get(ProjectFormFields.CreateMap.MAP_NAME);

            LocationHierarchy locality = hierGateway.getFirst(resolver, hierGateway.findById(localityUuid));

            LocationHierarchy map = new LocationHierarchy();
            map.setUuid(mapUuid);
            map.setExtId(mapName + "/" + locality.getName());
            map.setName(mapName);
            map.setParentUuid(localityUuid);
            map.setLevel(BiokoHierarchy.SERVER_MAP_AREA);

            hierGateway.insertOrUpdate(resolver, map);

            return new ConsumerResult(false);
        }
    }

    @UsedByJSConfig
    public static class CreateSector extends DefaultConsumer {
        @Override
        public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationHierarchyGateway hierGateway = GatewayRegistry.getLocationHierarchyGateway();
            ContentResolver resolver = ctx.getContentResolver();

            String mapUuid = formPayload.get(ProjectFormFields.CreateSector.MAP_UUID);
            String sectorUuid = formPayload.get(ProjectFormFields.CreateSector.SECTOR_UUID);
            String sectorName = formPayload.get(ProjectFormFields.CreateSector.SECTOR_NAME);

            LocationHierarchy map = hierGateway.getFirst(resolver, hierGateway.findById(mapUuid));
            LocationHierarchy locality = hierGateway.getFirst(resolver, hierGateway.findById(map.getParentUuid()));

            LocationHierarchy sector = new LocationHierarchy();
            sector.setUuid(sectorUuid);
            sector.setExtId(map.getName() + sectorName + "/" + locality.getName());
            sector.setName(sectorName);
            sector.setParentUuid(mapUuid);
            sector.setLevel(BiokoHierarchy.SERVER_SECTOR);

            hierGateway.insertOrUpdate(resolver, sector);

            return new ConsumerResult(false);
        }
    }
}

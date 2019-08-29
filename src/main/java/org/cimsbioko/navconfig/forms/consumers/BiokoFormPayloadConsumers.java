package org.cimsbioko.navconfig.forms.consumers;

import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.navconfig.ProjectFormFields;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.repository.GatewayRegistry;
import org.cimsbioko.repository.gateway.LocationHierarchyGateway;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    @UsedByJSConfig
    public static class CreateMap extends DefaultConsumer {
        @Override
        public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationHierarchyGateway hierGateway = GatewayRegistry.getLocationHierarchyGateway();

            String localityUuid = formPayload.get(ProjectFormFields.CreateMap.LOCALITY_UUID);
            String mapUuid = formPayload.get(ProjectFormFields.CreateMap.MAP_UUID);
            String mapName = formPayload.get(ProjectFormFields.CreateMap.MAP_NAME);

            LocationHierarchy locality = hierGateway.findById(localityUuid).getFirst();

            LocationHierarchy map = new LocationHierarchy();
            map.setUuid(mapUuid);
            map.setExtId(mapName + "/" + locality.getName());
            map.setName(mapName);
            map.setParentUuid(localityUuid);
            map.setLevel("mapArea"); // FIXME: decoupled from hierarchy constants to break dependency

            hierGateway.insertOrUpdate(map);

            return new ConsumerResult(false);
        }
    }

    @UsedByJSConfig
    public static class CreateSector extends DefaultConsumer {
        @Override
        public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationHierarchyGateway hierGateway = GatewayRegistry.getLocationHierarchyGateway();

            String mapUuid = formPayload.get(ProjectFormFields.CreateSector.MAP_UUID);
            String sectorUuid = formPayload.get(ProjectFormFields.CreateSector.SECTOR_UUID);
            String sectorName = formPayload.get(ProjectFormFields.CreateSector.SECTOR_NAME);

            LocationHierarchy map = hierGateway.findById(mapUuid).getFirst();
            LocationHierarchy locality = hierGateway.findById(map.getParentUuid()).getFirst();

            LocationHierarchy sector = new LocationHierarchy();
            sector.setUuid(sectorUuid);
            sector.setExtId(map.getName() + sectorName + "/" + locality.getName());
            sector.setName(sectorName);
            sector.setParentUuid(mapUuid);
            sector.setLevel("sector"); // FIXME: decoupled from hierarchy constants to break dependency

            hierGateway.insertOrUpdate(sector);

            return new ConsumerResult(false);
        }
    }
}

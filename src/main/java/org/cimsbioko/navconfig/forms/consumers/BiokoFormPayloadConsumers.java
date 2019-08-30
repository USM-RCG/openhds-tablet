package org.cimsbioko.navconfig.forms.consumers;

import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.data.GatewayRegistry;
import org.cimsbioko.data.LocationHierarchyGateway;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    @UsedByJSConfig
    public static class CreateMap extends DefaultConsumer {
        @Override
        public ConsumerResult consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationHierarchyGateway hierGateway = GatewayRegistry.getLocationHierarchyGateway();

            String localityUuid = formPayload.get("localityUuid");
            String mapUuid = formPayload.get("mapUuid");
            String mapName = formPayload.get("mapName");

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

            String mapUuid = formPayload.get("mapUuid");
            String sectorUuid = formPayload.get("sectorUuid");
            String sectorName = formPayload.get("sectorName");

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

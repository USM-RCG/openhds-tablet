package org.openhds.mobile.navconfig.forms.consumers;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    public static class DistributeBednets implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(ctx.getContentResolver(),
                    locationGateway.findById(ctx.getCurrentSelection().getUuid()));

            location.setHasReceivedBedNets("true");

            locationGateway.insertOrUpdate(ctx.getContentResolver(), location);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) { }

    }

    public static class SprayHousehold implements FormPayloadConsumer {

        public static final String SPRAY_EVAL_KEY = "evaluation";

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(ctx.getContentResolver(),
                    locationGateway.findById(ctx.getCurrentSelection().getUuid()));

            if (formPayload.containsKey(SPRAY_EVAL_KEY)) {
                location.setSprayingEvaluation(formPayload.get(SPRAY_EVAL_KEY));
            }

            locationGateway.insertOrUpdate(ctx.getContentResolver(), location);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) { }
    }

    public static class SuperOjo implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) { }
    }

    public static class DuplicateLocation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) { }

    }
}

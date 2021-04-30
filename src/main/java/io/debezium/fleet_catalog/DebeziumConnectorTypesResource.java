package io.debezium.fleet_catalog;

import io.debezium.fleet_catalog.models.ConnectorMetadata;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("/")
public class DebeziumConnectorTypesResource {

    private static final List<ConnectorMetadata> AVAILABLE_CONNECTORS = AvailableConnectorsHelper.getAvailableConnectors();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<List<ConnectorMetadata>> listConnectors() {
        return Uni.createFrom().item(AVAILABLE_CONNECTORS);
    }

}

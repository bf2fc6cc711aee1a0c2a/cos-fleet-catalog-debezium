package io.debezium.mcs.envelope;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class FleetshardCatalogEnvelope {

    @JsonProperty("connector_type")
    public final ConnectorTypesEnvelope connectorTypesEnvelope;

    @JsonProperty("channels")
    public final JsonNode channels = JsonNodeFactory.instance.objectNode();

    public FleetshardCatalogEnvelope(Schema schema) {
        this.connectorTypesEnvelope = ConnectorTypesEnvelope.fromSchema(schema);
    }

}

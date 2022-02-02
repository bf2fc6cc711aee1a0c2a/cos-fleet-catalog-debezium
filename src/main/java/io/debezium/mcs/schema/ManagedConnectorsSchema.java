/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.mcs.schema;

import java.io.IOException;
import java.util.Map;

import io.debezium.schemagenerator.schema.Schema;
import io.debezium.schemagenerator.schema.SchemaDescriptor;
import io.debezium.schemagenerator.schema.SchemaName;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.mcs.envelope.FleetshardCatalogEnvelope;

@SchemaName("mcs")
public class ManagedConnectorsSchema implements Schema {

    private static final SchemaDescriptor DESCRIPTOR = new SchemaDescriptor() {
        @Override
        public String getId() {
            return "mcs";
        }

        @Override
        public String getName() {
            return "Managed Connectors ConnectorTypes API";
        }

        @Override
        public String getVersion() {
            return "0.0.1";
        }

        @Override
        public String getDescription() {
            return "TBD";
        }
    };

    @Override
    public SchemaDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public void configure(Map<String, Object> config) {
        if (null == config || config.isEmpty()) {
            return;
        }
        config.forEach((property, value) -> {
            switch (property) {
                default:
                    break;
            }
        });
    }

    @Override
    public String getSpec(org.eclipse.microprofile.openapi.models.media.Schema connectorSchema) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new FleetshardCatalogEnvelope(connectorSchema));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

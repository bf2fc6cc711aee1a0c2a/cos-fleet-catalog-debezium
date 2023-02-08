/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.mcs.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import io.debezium.schemagenerator.schema.Schema;
import io.debezium.schemagenerator.schema.SchemaDescriptor;
import io.debezium.schemagenerator.schema.SchemaName;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.debezium.mcs.envelope.FleetshardCatalogEnvelope;

@SchemaName("mcs")
public class ManagedConnectorsSchema implements Schema {

    public static final List<String> DEFAULT_INCLUDED_CONFIG_OPTIONS = Arrays.asList(
            "topic.prefix",
            "database.server.id",
            "database.hostname",
            "database.port",
            "database.user",
            "database.password",
            "database.include.list",
            "database.exclude.list",
            "table.include.list",
            "table.exclude.list",
            "column.include.list",
            "column.exclude.list",
            "snapshot.mode",
            "message.key.columns",
            "query.fetch.size",
            "max.batch.size",
            "max.queue.size",
            "decimal.handling.mode",
            "mongodb.name",
            "mongodb.hosts",
            "mongodb.user",
            "mongodb.password",
            "mongodb.authsource",
            "mongodb.ssl.enabled",
            "collection.include.list",
            "collection.exclude.list",
            "field.exclude.list",
            "database.dbname",
            "database.names",
            "slot.name",
            "publication.name",
            "publication.autocreate.mode",
            "schema.include.list",
            "schema.exclude.list");

    private static final Pattern VERSION_PATTERN = Pattern
            .compile("([1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)(?:-([a-zA-Z0-9]+))?(?:(\\+)(0|[1-9][0-9]*)?)?(?:-([-a-zA-Z0-9.]+))?");

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

    private static Runtime.Version parseVersion(String version) {
        Matcher m = VERSION_PATTERN.matcher(version);
        if (m.matches()) {
            return Runtime.Version.parse(version);
        }
        else if (m.lookingAt()) {
            return Runtime.Version.parse(m.group());
        }
        throw new IllegalArgumentException("Invalid version string: \"" + version + "\"");
    }

    @Override
    public String getSpec(org.eclipse.microprofile.openapi.models.media.Schema connectorSchema) {
        final ObjectMapper mapper = new ObjectMapper();

        JsonNode fleetshardCatalog = mapper.valueToTree(new FleetshardCatalogEnvelope(connectorSchema));
        final Map<String, Object> schemaExtensions = connectorSchema.getExtensions();
        final Runtime.Version connectorVersion = parseVersion((String) schemaExtensions.get("version"));
        final String overridesFilename = "overrides-" + schemaExtensions.get("connector-id") + "-" + connectorVersion.major() + "." + connectorVersion.minor() + ".json";
        final InputStream overridesFileInputStream = getClass().getClassLoader().getResourceAsStream(overridesFilename);
        if(null == overridesFileInputStream) {
            throw new RuntimeException("Overrides file not found: " + overridesFilename);
        }

        var merger = mapper.readerForUpdating(fleetshardCatalog);
        try {
            merger.readTree(overridesFileInputStream);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fleetshardCatalog);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FieldFilter getFieldFilter() {
        return (field) -> DEFAULT_INCLUDED_CONFIG_OPTIONS.contains(field.name());
    }

}

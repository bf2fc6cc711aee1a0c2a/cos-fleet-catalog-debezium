package io.debezium.mcs.envelope;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Runtime.Version;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FleetshardCatalogEnvelope {

    private static final Pattern VERSION_PATTERN = Pattern
            .compile("([1-9][0-9]*(?:(?:\\.0)*\\.[1-9][0-9]*)*)(?:-([a-zA-Z0-9]+))?(?:(\\+)(0|[1-9][0-9]*)?)?(?:-([-a-zA-Z0-9.]+))?");

    @JsonProperty("connector_type")
    public final ConnectorTypesEnvelope connectorTypesEnvelope;

    @JsonProperty("channels")
    public final JsonNode channels;

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

    public FleetshardCatalogEnvelope(Schema schema) {
        final Map<String, Object> schemaExtensions = schema.getExtensions();
        final Version connectorVersion = parseVersion((String) schemaExtensions.get("version"));
        final String channelsFilename = "channels-" + schemaExtensions.get("connector-id") + "-" + connectorVersion.major() + "." + connectorVersion.minor() + ".json";
        final InputStream channelsFileInputStream = getClass().getClassLoader().getResourceAsStream(channelsFilename);
        if(null == channelsFileInputStream) {
            throw new RuntimeException("Channels file not found: " + channelsFilename);
        }
        this.connectorTypesEnvelope = ConnectorTypesEnvelope.fromSchema(schema);
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            channels = objectMapper.readTree(channelsFileInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

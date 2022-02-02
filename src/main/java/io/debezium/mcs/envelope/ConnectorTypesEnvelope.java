package io.debezium.mcs.envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.openapi.models.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;

public class ConnectorTypesEnvelope {

    private static final String BASE_API_HREF = "/api/connector_mgmt/v1/kafka_connector_types/";
    private static final String BASE_IMAGES_HREF = "http://example.com/images/";

    private static final Schema PASSWORD_SCHEMA_PLAINTEXT = new SchemaImpl();
    private static final Schema PASSWORD_SCHEMA_REFRENCE = new SchemaImpl();

    public final String id;
    public final String kind = "ConnectorType";
    public final String href;
    public final String name;
    public final String version;
    public final List<String> channels = Collections.singletonList("stable");
    public final String description;

    @JsonProperty("icon_href")
    public final String iconHref;

    public final List<String> labels = new LinkedList<>(Arrays.asList("source", "debezium"));

    public final List<String> capabilities = new ArrayList<>(Collections.singletonList("data_shape"));

    @JsonProperty("schema")
    public final ObjectNode jsonSchema;

    private static final JsonNode ADDITIONAL_DEFINITIONS;

    private static final ObjectNode ADDITIONAL_PROPERTIES;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        PASSWORD_SCHEMA_PLAINTEXT.type(Schema.SchemaType.STRING);
        PASSWORD_SCHEMA_PLAINTEXT.format("password");
        PASSWORD_SCHEMA_PLAINTEXT.description("Password of the database user to be used when connecting to the database.");

        PASSWORD_SCHEMA_REFRENCE.type(Schema.SchemaType.OBJECT);
        PASSWORD_SCHEMA_REFRENCE.properties(new HashMap<>());
        PASSWORD_SCHEMA_REFRENCE.setAdditionalPropertiesBoolean(true);
        PASSWORD_SCHEMA_REFRENCE.description("An opaque reference to the password.");

        try {
            ADDITIONAL_PROPERTIES = (ObjectNode) OBJECT_MAPPER.readTree(ConnectorTypesEnvelope.class.getClassLoader().getResourceAsStream("additional_properties.json"));
            ADDITIONAL_DEFINITIONS = OBJECT_MAPPER.readTree(ConnectorTypesEnvelope.class.getClassLoader().getResourceAsStream("additional_definitions.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ConnectorTypesEnvelope(String name, String version, String title, String description, List<String> connectorLabels, ObjectNode jsonSchema) {
        this.id = name + "-" + version;
        this.href = BASE_API_HREF + id;
        this.name = title;
        this.version = version;
        this.description = description;
        this.iconHref = BASE_IMAGES_HREF + id + ".png";
        this.labels.addAll(connectorLabels);
        this.jsonSchema = jsonSchema;
    }

    private static void patchSchema(Schema originalSchema) {
        originalSchema.getProperties().forEach((property, schema) -> {
            if (Schema.SchemaType.STRING.equals(schema.getType())
                    && "password".equals(schema.getFormat())) {
                schema.type(null);
                schema.format(null);
                schema.oneOf(Arrays.asList(PASSWORD_SCHEMA_PLAINTEXT, PASSWORD_SCHEMA_REFRENCE));
            }
        });
    }

    public static ConnectorTypesEnvelope fromSchema(Schema schema) {
        Map<String, Object> schemaExtensions = schema.getExtensions();
        ObjectNode jsonSchema = OBJECT_MAPPER.createObjectNode();
        patchSchema(schema);
        SchemaWriter.writeSchema(jsonSchema, schema, schema.getTitle());
        ObjectNode generatedSchema = (ObjectNode) jsonSchema.elements().next();
        generatedSchema.setAll(ADDITIONAL_PROPERTIES);
        generatedSchema.set("$defs", ADDITIONAL_DEFINITIONS);

        return new ConnectorTypesEnvelope(
                "debezium-" + schemaExtensions.get("connector-id"),
                (String) schemaExtensions.get("version"),
                schema.getTitle(),
                schema.getDescription(),
                Arrays.asList((String) schemaExtensions.get("connector-id"), (String) schemaExtensions.get("version")),
                generatedSchema);
    }

}

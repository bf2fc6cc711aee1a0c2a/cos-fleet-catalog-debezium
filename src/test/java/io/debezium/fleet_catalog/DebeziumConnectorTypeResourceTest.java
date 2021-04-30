package io.debezium.fleet_catalog;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.restassured.RestAssuredResponse;
import io.debezium.fleet_catalog.models.ConnectorMetadata;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class DebeziumConnectorTypeResourceTest {

    private static final String CONNECTOR_API_BASE_URL = "/api/connector_mgmt/v1/kafka-connector-types/";
    private static final String METADATA_API_BASE_URL = "/v1/kafka-connector-catalog/";

    @Test
    public void testIndex() {
        var connectors = given()
          .when().get("/")
          .then().log().all()
             .statusCode(200)
             .extract().as(new TypeRef<List<ConnectorMetadata>>() {});
          assertEquals(AvailableConnectorsHelper.getAvailableConnectors(), connectors);
    }

    @Test
    public void validateOpenAPISchemaOfConnectors() {
        final var fleetmanagerConnectorTypeSpec = DebeziumConnectorTypeResourceTest.class.getResource("/connector_mgmt.yaml").getPath();
        final OpenApiInteractionValidator connectorSpecValidator = OpenApiInteractionValidator.createFor(fleetmanagerConnectorTypeSpec).build();
        AvailableConnectorsHelper.getAvailableConnectors().forEach(connectorMetadata -> {
            var connectorResponse = given()
                .when().get("/" + connectorMetadata.id)
                    .then()
                    .statusCode(200).extract();

            var validationReport = connectorSpecValidator.validateResponse(
                    CONNECTOR_API_BASE_URL + connectorMetadata.id.replace("/", "%2F"),
                    Request.Method.GET,
                    RestAssuredResponse.of(connectorResponse.response()));

            assertFalse(validationReport.hasErrors(), "Errors found: " + validationReport.getMessages());
        });
    }

    @Test
    public void validateOpenAPISchemaOfMetadata() {
        final var fleetmanagerConnectorTypeSpec = DebeziumConnectorTypeResourceTest.class.getResource("/connector_catalog.yaml").getPath();
        final OpenApiInteractionValidator connectorSpecValidator = OpenApiInteractionValidator.createFor(fleetmanagerConnectorTypeSpec).build();
        var connectorResponse = given()
            .when().get("/")
                .then()
                .statusCode(200).extract();

        var validationReport = connectorSpecValidator.validateResponse(
                METADATA_API_BASE_URL,
                Request.Method.GET,
                RestAssuredResponse.of(connectorResponse.response()));

        assertFalse(validationReport.hasErrors(), "Errors found: " + validationReport.getMessages());
    }

}
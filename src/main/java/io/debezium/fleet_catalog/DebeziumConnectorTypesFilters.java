package io.debezium.fleet_catalog;


import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

public class DebeziumConnectorTypesFilters {

    @RouteFilter(999)
    void rewriteConnectorTypesRequests(RoutingContext rc) {
        String finalUri = rc.request().uri();
        if ("/".equals(finalUri) || "/favicon.ico".equals(finalUri) || finalUri.endsWith(".json")) {
            rc.next();
            return;
        }

        rc.reroute(finalUri + ".json");
    }
}
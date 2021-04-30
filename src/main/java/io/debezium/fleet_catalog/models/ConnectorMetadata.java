package io.debezium.fleet_catalog.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ConnectorMetadata {

    private static final String META_IMAGE_0_0_1 = "quay.io/cos-debezium/cos-meta-debezium:0.0.1";
    private static final String DEFAULT_META_IMAGE = META_IMAGE_0_0_1;

    private static final String DEFAULT_REVISION = "0.0.1";

    private static final String STABLE_CHANNEL = "stable";
    private static final String DEFAULT_CHANNEL = STABLE_CHANNEL;

    public final String id;

    public final String channel;

    public final String revision;

    @JsonProperty("connector_image")
    public final String connectorImage;

    public ConnectorMetadata(String id) {
        this.id = id;
        this.channel = DEFAULT_CHANNEL;
        this.revision = DEFAULT_REVISION;
        this.connectorImage = DEFAULT_META_IMAGE;
    }

    public ConnectorMetadata(String id, String channel, String revision, String connectorImage) {
        this.id = id;
        this.channel = channel;
        this.revision = revision;
        this.connectorImage = connectorImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectorMetadata that = (ConnectorMetadata) o;
        return id.equals(that.id) && channel.equals(that.channel) && revision.equals(that.revision) && connectorImage.equals(that.connectorImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, channel, revision, connectorImage);
    }
}

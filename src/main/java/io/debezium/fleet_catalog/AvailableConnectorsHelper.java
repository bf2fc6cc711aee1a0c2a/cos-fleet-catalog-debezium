package io.debezium.fleet_catalog;

import io.debezium.fleet_catalog.models.ConnectorMetadata;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvailableConnectorsHelper {

    private static FileSystem initJarFileSystem(URI uri) throws IOException
    {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        return FileSystems.newFileSystem(uri, env);
    }

    public static List<ConnectorMetadata> getAvailableConnectors() {
        try {
            FileSystem fs = null;
            URI uri = DebeziumConnectorTypesResource.class.getResource("/META-INF/resources/").toURI();
            java.nio.file.Path baseDir;
            try {
                baseDir = Paths.get(uri);
            } catch (FileSystemNotFoundException e) {
                fs = initJarFileSystem(uri);
                baseDir = Paths.get(uri);
            }
            java.nio.file.Path finalBaseDir = baseDir;
            List<ConnectorMetadata> endpoints = Files.find(
                    baseDir,
                    3,
                    (path, attributes) -> attributes.isRegularFile())
                    .map(finalBaseDir::relativize)
                    .map(path -> new ConnectorMetadata(path.toString().substring(0, path.toString().length() - 5))) // remove `.json` file ending
                    .collect(Collectors.toList());
            if (null != fs) {
                fs.close();
            }
            return endpoints;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

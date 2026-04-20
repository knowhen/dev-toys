package io.devtoys.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluginLoaderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // --- PluginManifest parsing ---

    @Test
    void manifest_parsesFullJson() throws Exception {
        String json = """
            {
              "id": "io.example.my-plugin",
              "name": "My Plugin",
              "version": "1.2.3",
              "author": "Someone",
              "description": "Does stuff.",
              "homepage": "https://example.com",
              "minHostVersion": "0.1.0"
            }
            """;
        PluginManifest m = MAPPER.readValue(json, PluginManifest.class);
        assertEquals("io.example.my-plugin", m.id());
        assertEquals("My Plugin", m.name());
        assertEquals("1.2.3", m.version());
        assertEquals("Someone", m.author());
    }

    @Test
    void manifest_allowsMinimalJson() throws Exception {
        String json = """
            {
              "id": "x",
              "name": "X",
              "version": "1.0"
            }
            """;
        PluginManifest m = MAPPER.readValue(json, PluginManifest.class);
        assertEquals("", m.author());
        assertEquals("", m.description());
    }

    @Test
    void manifest_rejectsMissingId() {
        String json = "{ \"name\": \"X\", \"version\": \"1.0\" }";
        assertThrows(Exception.class, () -> MAPPER.readValue(json, PluginManifest.class));
    }

    @Test
    void manifest_rejectsBlankId() {
        String json = "{ \"id\": \"\", \"name\": \"X\", \"version\": \"1.0\" }";
        assertThrows(Exception.class, () -> MAPPER.readValue(json, PluginManifest.class));
    }

    // --- Loader on empty/absent directories ---

    @Test
    void loader_handlesNullDirectory() {
        List<Plugin> result = new SharedClassLoaderPluginLoader().loadAll(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void loader_handlesNonExistentDirectory(@TempDir Path base) {
        Path nope = base.resolve("does-not-exist");
        List<Plugin> result = new SharedClassLoaderPluginLoader().loadAll(nope);
        assertTrue(result.isEmpty());
    }

    @Test
    void loader_handlesEmptyDirectory(@TempDir Path dir) {
        List<Plugin> result = new SharedClassLoaderPluginLoader().loadAll(dir);
        assertTrue(result.isEmpty());
    }

    @Test
    void loader_ignoresNonJarFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("README.txt"), "not a jar");
        Files.writeString(dir.resolve("config.json"), "{}");
        List<Plugin> result = new SharedClassLoaderPluginLoader().loadAll(dir);
        assertTrue(result.isEmpty());
    }

    // End-to-end tests that actually build a plugin JAR would need the
    // plugin classloader to find our api module, which is non-trivial in a
    // pure unit-test setting. Those tests live as integration tests once we
    // have the example plugins packaged.
}

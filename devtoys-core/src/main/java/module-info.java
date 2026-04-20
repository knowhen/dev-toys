/**
 * Core runtime: plugin discovery, registry, host-service implementations,
 * and the background task runner.
 */
module io.devtoys.core {
    requires transitive io.devtoys.api;
    requires org.slf4j;

    // Consumes all IGuiTool services found on the module path.
    uses io.devtoys.api.IGuiTool;

    exports io.devtoys.core;
    exports io.devtoys.core.services;
    exports io.devtoys.core.tasks;
}

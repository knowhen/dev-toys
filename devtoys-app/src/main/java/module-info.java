/**
 * Desktop shell / main application module for DevToys Java.
 */
module io.devtoys.app {
    requires io.devtoys.api;
    requires io.devtoys.core;
    requires io.devtoys.tools;              // so ServiceLoader sees its provides directive
    requires io.devtoys.plugin.loader;      // third-party plugins support
    requires java.desktop;                  // for Desktop.open() in ExtensionsPanel
    requires javafx.controls;
    requires javafx.graphics;
    requires atlantafx.base;

    // Logging: SLF4J api is transitive via core; logback-classic is runtime-only
    // (the binding is auto-discovered, no module dependency needed).
    requires org.slf4j;

    // Icons: require the core, JavaFX integration and the three icon packs.
    // Ikonli loads packs via ServiceLoader, so all three are discovered at runtime.
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fluentui;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.material2;

    uses io.devtoys.api.IGuiTool;

    // JavaFX uses reflection to instantiate DevToysApp
    opens io.devtoys.app to javafx.graphics;

    exports io.devtoys.app;
}

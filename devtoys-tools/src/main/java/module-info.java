import io.devtoys.api.IGuiTool;

/**
 * Built-in tools module.
 *
 * <p>Each tool class is registered as an {@link IGuiTool} service via the
 * {@code provides ... with} directive. The host's {@code ToolRegistry}
 * discovers them through {@link java.util.ServiceLoader}.
 *
 * <p>A {@code META-INF/services/io.devtoys.api.IGuiTool} file listing the same
 * classes is also provided, so the tools are still discoverable when the app
 * is launched from the plain classpath.
 *
 * <p>The per-tool packages are exported so external modules (notably
 * {@code devtoys-cli}) can reuse the pure {@code *Core} classes. The GUI
 * {@code *Tool} classes are exported too for simplicity — they require
 * JavaFX at runtime, but callers that don't instantiate them pay no cost.
 */
module io.devtoys.tools {
    requires io.devtoys.api;
    requires io.devtoys.core;                         // for BackgroundTaskRunner
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.slf4j;

    exports io.devtoys.tools.base64;
    exports io.devtoys.tools.hash;
    exports io.devtoys.tools.json;
    exports io.devtoys.tools.regex;
    exports io.devtoys.tools.uuid;
    exports io.devtoys.tools.urlcodec;
    exports io.devtoys.tools.htmlentity;
    exports io.devtoys.tools.timestamp;
    exports io.devtoys.tools.numberbase;
    exports io.devtoys.tools.loremipsum;
    exports io.devtoys.tools.password;
    exports io.devtoys.tools.color;

    provides IGuiTool with
            io.devtoys.tools.base64.Base64Tool,
            io.devtoys.tools.json.JsonFormatterTool,
            io.devtoys.tools.hash.HashTool,
            io.devtoys.tools.uuid.UuidTool,
            io.devtoys.tools.regex.RegexTool,
            io.devtoys.tools.urlcodec.UrlCodecTool,
            io.devtoys.tools.htmlentity.HtmlEntityTool,
            io.devtoys.tools.timestamp.TimestampTool,
            io.devtoys.tools.numberbase.NumberBaseTool,
            io.devtoys.tools.loremipsum.LoremIpsumTool,
            io.devtoys.tools.password.PasswordTool,
            io.devtoys.tools.color.ColorTool;
}

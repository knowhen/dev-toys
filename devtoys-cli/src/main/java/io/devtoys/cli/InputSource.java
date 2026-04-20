package io.devtoys.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centralizes the "where does the input come from" rule used by most commands:
 *
 * <ol>
 *   <li>If a positional argument was given, use that.</li>
 *   <li>Else if {@code --file PATH} was given, read that file as UTF-8.</li>
 *   <li>Else, read until EOF from {@code System.in} as UTF-8.</li>
 * </ol>
 *
 * <p>Reading from stdin blocks until EOF (Ctrl-D on Unix / Ctrl-Z then Enter on
 * Windows, or simply a piped stream finishing). This is standard Unix behaviour
 * and lets the CLI compose with other tools, e.g.
 * {@code cat data.json | devtoys json format}.
 */
public final class InputSource {

    private InputSource() {}

    /**
     * @param positional the positional argument from Picocli (may be null)
     * @param file       the {@code --file} argument from Picocli (may be null)
     * @return the resolved text, never null (empty string if nothing was provided)
     */
    public static String read(String positional, Path file) {
        if (positional != null) return positional;
        if (file != null) {
            try {
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read " + file, e);
            }
        }
        return readStdin();
    }

    private static String readStdin() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read from stdin", e);
        }
        return sb.toString();
    }
}

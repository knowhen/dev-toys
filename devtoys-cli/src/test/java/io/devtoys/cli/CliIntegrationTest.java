package io.devtoys.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for the CLI. Executes {@code new CommandLine(new Main()).execute(args)}
 * with captured stdout/stderr and asserts on the captured output and exit code.
 * These tests don't call {@code System.exit}, so they're safe to run in-process.
 */
class CliIntegrationTest {

    private record Run(int exitCode, String stdout, String stderr) {}

    private Run run(String... args) {
        return runWithStdin("", args);
    }

    private Run runWithStdin(String stdin, String... args) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        var originalIn = System.in;
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outBuf, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(errBuf, true, StandardCharsets.UTF_8));
            System.setIn(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
            int exit = new CommandLine(new Main()).execute(args);
            return new Run(exit,
                    outBuf.toString(StandardCharsets.UTF_8),
                    errBuf.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(originalIn);
        }
    }

    // --- top-level ---

    @Test
    void noArgs_printsUsage() {
        Run r = run();
        assertEquals(0, r.exitCode);
        assertTrue(r.stderr.contains("Usage: devtoys"),
                "should show usage; got stderr=" + r.stderr);
    }

    @Test
    void unknownSubcommand_returnsUsageError() {
        Run r = run("bogus-subcommand");
        assertEquals(CommandLine.ExitCode.USAGE, r.exitCode);
    }

    // --- base64 ---

    @Test
    void base64_encode_positional() {
        Run r = run("base64", "hello");
        assertEquals(0, r.exitCode);
        assertEquals("aGVsbG8=\n", r.stdout.replace("\r\n", "\n"));
    }

    @Test
    void base64_decode_positional() {
        Run r = run("base64", "--decode", "aGVsbG8=");
        assertEquals(0, r.exitCode);
        assertEquals("hello\n", r.stdout.replace("\r\n", "\n"));
    }

    @Test
    void base64_encode_fromStdin() {
        Run r = runWithStdin("hello", "base64");
        assertEquals(0, r.exitCode);
        assertEquals("aGVsbG8=\n", r.stdout.replace("\r\n", "\n"));
    }

    // --- hash ---

    @Test
    void hash_withAlgo_printsSingleHex() {
        Run r = run("hash", "--algo", "MD5", "hello");
        assertEquals(0, r.exitCode);
        assertEquals("5d41402abc4b2a76b9719d911017c592\n", r.stdout.replace("\r\n", "\n"));
    }

    @Test
    void hash_noAlgo_printsAll() {
        Run r = run("hash", "hello");
        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("MD5"));
        assertTrue(r.stdout.contains("SHA-256"));
        assertTrue(r.stdout.contains("5d41402abc4b2a76b9719d911017c592"));  // MD5 of "hello"
    }

    @Test
    void hash_upper_flagWorks() {
        Run r = run("hash", "--algo", "MD5", "--upper", "hello");
        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("5D41402ABC4B2A76B9719D911017C592"));
    }

    // --- json ---

    @Test
    void json_format_prettyPrints() {
        Run r = run("json", "format", "{\"a\":1,\"b\":2}");
        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("\n"));
        assertTrue(r.stdout.contains("  \"a\""));
    }

    @Test
    void json_minify_removesWhitespace() {
        Run r = run("json", "minify", "{\n  \"a\" : 1\n}");
        assertEquals(0, r.exitCode);
        assertEquals("{\"a\":1}\n", r.stdout.replace("\r\n", "\n"));
    }

    @Test
    void json_validate_invalid_returns1() {
        Run r = run("json", "validate", "--quiet", "not json");
        assertEquals(1, r.exitCode);
    }

    @Test
    void json_validate_valid_returns0() {
        Run r = run("json", "validate", "--quiet", "{\"a\":1}");
        assertEquals(0, r.exitCode);
    }

    // --- uuid ---

    @Test
    void uuid_defaultCount_isOne() {
        Run r = run("uuid");
        assertEquals(0, r.exitCode);
        assertEquals(1, r.stdout.trim().lines().count());
    }

    @Test
    void uuid_count_generatesRequestedNumber() {
        Run r = run("uuid", "-n", "5");
        assertEquals(0, r.exitCode);
        assertEquals(5, r.stdout.trim().lines().count());
    }

    @Test
    void uuid_noHyphens_producesCompactForm() {
        Run r = run("uuid", "--no-hyphens");
        assertEquals(0, r.exitCode);
        String uuid = r.stdout.trim();
        assertFalse(uuid.contains("-"), "should not contain hyphens: " + uuid);
        assertEquals(32, uuid.length());
    }

    // --- regex ---

    @Test
    void regex_matches_returns0() {
        Run r = run("regex", "\\d+", "abc 123 def");
        assertEquals(0, r.exitCode);
        assertTrue(r.stdout.contains("123"));
    }

    @Test
    void regex_noMatch_returns1() {
        Run r = run("regex", "XXX", "abc 123");
        assertEquals(1, r.exitCode);
    }

    @Test
    void regex_quiet_producesNoStdout() {
        Run r = run("regex", "--quiet", "\\d+", "abc 123");
        assertEquals(0, r.exitCode);
        assertEquals("", r.stdout);
    }
}

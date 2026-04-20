package io.devtoys.tools.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure business logic for cryptographic digests. Zero UI dependencies.
 */
public final class HashCore {

    /** Algorithms we compute by default, in display order. */
    public static final List<String> DEFAULT_ALGORITHMS =
            List.of("MD5", "SHA-1", "SHA-256", "SHA-512");

    private HashCore() {}

    /**
     * Compute the digest of {@code data} (UTF-8 encoded) using the named
     * algorithm, returning lowercase hex.
     *
     * @throws IllegalArgumentException if the JVM does not support the algorithm
     */
    public static String digest(String algorithm, String data) {
        return digest(algorithm, data, false);
    }

    /**
     * Compute the digest of {@code data} using the named algorithm.
     *
     * @param upper when true, returns uppercase hex; otherwise lowercase
     * @throws IllegalArgumentException if the JVM does not support the algorithm
     */
    public static String digest(String algorithm, String data, boolean upper) {
        String safeData = data == null ? "" : data;
        byte[] bytes = safeData.getBytes(StandardCharsets.UTF_8);
        return digest(algorithm, bytes, upper);
    }

    public static String digest(String algorithm, byte[] data, boolean upper) {
        if (data == null) data = new byte[0];
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
        byte[] d = md.digest(data);
        StringBuilder sb = new StringBuilder(d.length * 2);
        for (byte b : d) {
            String h = Integer.toHexString(b & 0xff);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return upper ? sb.toString().toUpperCase() : sb.toString();
    }

    /**
     * Convenience: compute all {@link #DEFAULT_ALGORITHMS} in order.
     * Returns a LinkedHashMap preserving iteration order.
     */
    public static Map<String, String> digestAll(String data, boolean upper) {
        byte[] bytes = (data == null ? "" : data).getBytes(StandardCharsets.UTF_8);
        Map<String, String> out = new LinkedHashMap<>();
        for (String algo : DEFAULT_ALGORITHMS) {
            out.put(algo, digest(algo, bytes, upper));
        }
        return out;
    }
}

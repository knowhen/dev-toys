package io.devtoys.tools.hash;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HashCoreTest {

    /** Well-known digest of the empty string (MD5). */
    @Test
    void md5_empty() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", HashCore.digest("MD5", ""));
    }

    /** Well-known digest of "abc" (SHA-256), from NIST test vectors. */
    @Test
    void sha256_abc() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashCore.digest("SHA-256", "abc"));
    }

    @Test
    void sha1_helloWorld() {
        // Known value from many online calculators
        assertEquals(
                "0a4d55a8d778e5022fab701977c5d840bbc486d0",
                HashCore.digest("SHA-1", "Hello World"));
    }

    @Test
    void uppercase_flag() {
        String lower = HashCore.digest("MD5", "hello", false);
        String upper = HashCore.digest("MD5", "hello", true);
        assertEquals(lower.toUpperCase(), upper);
        assertNotEquals(lower, upper);
    }

    @Test
    void nullInput_treatedAsEmpty() {
        assertEquals(HashCore.digest("MD5", ""), HashCore.digest("MD5", (String) null));
    }

    @Test
    void unknownAlgorithm_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> HashCore.digest("NOPE-512", "hello"));
    }

    @Test
    void digestAll_returnsAllAlgorithms_inOrder() {
        Map<String, String> all = HashCore.digestAll("hello", false);
        assertEquals(HashCore.DEFAULT_ALGORITHMS, all.keySet().stream().toList());
        // Spot-check MD5 of "hello"
        assertEquals("5d41402abc4b2a76b9719d911017c592", all.get("MD5"));
    }
}

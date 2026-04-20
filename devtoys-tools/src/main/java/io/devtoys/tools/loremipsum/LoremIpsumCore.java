package io.devtoys.tools.loremipsum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate Lorem Ipsum placeholder text.
 *
 * <p>Output is deterministic-seedable so tests can assert on exact output, but
 * production callers will get a fresh random instance on each call.
 */
public final class LoremIpsumCore {

    /** Canonical opening phrase used by every Lorem Ipsum generator. */
    public static final String CANONICAL_START =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    private static final String[] WORDS = {
            "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing",
            "elit", "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore",
            "et", "dolore", "magna", "aliqua", "enim", "ad", "minim", "veniam",
            "quis", "nostrud", "exercitation", "ullamco", "laboris", "nisi",
            "aliquip", "ex", "ea", "commodo", "consequat", "duis", "aute", "irure",
            "in", "reprehenderit", "voluptate", "velit", "esse", "cillum", "fugiat",
            "nulla", "pariatur", "excepteur", "sint", "occaecat", "cupidatat",
            "non", "proident", "sunt", "culpa", "qui", "officia", "deserunt",
            "mollit", "anim", "id", "est", "laborum"
    };

    private LoremIpsumCore() {}

    /** Generate N words, separated by spaces. First word is always "Lorem". */
    public static String words(int count, long seed) {
        if (count <= 0) return "";
        Random rnd = new Random(seed);
        StringBuilder sb = new StringBuilder();
        sb.append("Lorem");
        for (int i = 1; i < count; i++) {
            sb.append(' ').append(WORDS[rnd.nextInt(WORDS.length)]);
        }
        return sb.toString();
    }

    /** Generate N sentences. First sentence is always the canonical one. */
    public static String sentences(int count, long seed) {
        if (count <= 0) return "";
        Random rnd = new Random(seed);
        List<String> sentences = new ArrayList<>(count);
        sentences.add(CANONICAL_START);
        for (int i = 1; i < count; i++) {
            sentences.add(buildSentence(rnd));
        }
        return String.join(" ", sentences);
    }

    /** Generate N paragraphs (each 3..7 sentences). */
    public static String paragraphs(int count, long seed) {
        if (count <= 0) return "";
        Random rnd = new Random(seed);
        List<String> paragraphs = new ArrayList<>(count);
        for (int p = 0; p < count; p++) {
            int len = 3 + rnd.nextInt(5);
            List<String> s = new ArrayList<>(len);
            if (p == 0) s.add(CANONICAL_START);
            while (s.size() < len) s.add(buildSentence(rnd));
            paragraphs.add(String.join(" ", s));
        }
        return String.join("\n\n", paragraphs);
    }

    private static String buildSentence(Random rnd) {
        int len = 6 + rnd.nextInt(10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String w = WORDS[rnd.nextInt(WORDS.length)];
            if (i == 0) w = Character.toUpperCase(w.charAt(0)) + w.substring(1);
            if (i > 0) sb.append(' ');
            sb.append(w);
        }
        sb.append('.');
        return sb.toString();
    }
}

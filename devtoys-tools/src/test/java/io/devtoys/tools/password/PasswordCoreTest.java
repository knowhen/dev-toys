package io.devtoys.tools.password;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordCoreTest {

    @Test void generatesRequestedLength() {
        String p = PasswordCore.generate(20, PasswordCore.Options.DEFAULT);
        assertEquals(20, p.length());
    }

    @Test void onlyLowercase_whenOnlyLowerSelected() {
        PasswordCore.Options opts =
                new PasswordCore.Options(true, false, false, false, false);
        String p = PasswordCore.generate(50, opts);
        assertTrue(p.matches("[a-z]+"), "Got: " + p);
    }

    @Test void excludeAmbiguous_hasNoSuchChars() {
        PasswordCore.Options opts =
                new PasswordCore.Options(true, true, true, false, true);
        for (int i = 0; i < 20; i++) {
            String p = PasswordCore.generate(80, opts);
            assertFalse(p.matches(".*[lIO01].*"),
                    "Ambiguous char slipped through: " + p);
        }
    }

    @Test void rejects_whenNoCharsetSelected() {
        PasswordCore.Options opts = new PasswordCore.Options(false, false, false, false, false);
        assertThrows(IllegalArgumentException.class,
                () -> PasswordCore.generate(10, opts));
    }

    @Test void rejects_zeroLength() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordCore.generate(0, PasswordCore.Options.DEFAULT));
    }

    @Test void generateMany_producesRequestedCount() {
        String[] passwords = PasswordCore.generateMany(5, 12, PasswordCore.Options.DEFAULT);
        assertEquals(5, passwords.length);
        for (String p : passwords) assertEquals(12, p.length());
    }
}

package io.devtoys.app;

/**
 * Plain entry point that does not extend {@code javafx.application.Application}.
 *
 * <p>Why this exists: when JavaFX is on the classpath (as opposed to the module
 * path), launching a class that extends {@code Application} directly fails with
 * "JavaFX runtime components are missing". A tiny launcher that simply calls
 * {@link DevToysApp#main(String[])} sidesteps that. It is also the standard
 * recipe for {@code jpackage}, which resolves the JDK + app modules, and for
 * fat-jar shading.
 */
public final class Launcher {

    public static void main(String[] args) {
        DevToysApp.main(args);
    }

    private Launcher() {}
}

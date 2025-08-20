package org.betamc.tsunami;

public class Tsunami {

    private Tsunami() {
    }

    public static String version() {
        return Tsunami.class.getPackage().getImplementationVersion();
    }

    public static TsunamiConfig config() {
        return TsunamiConfig.getInstance();
    }

}

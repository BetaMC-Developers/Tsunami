package org.betamc.tsunami;

import org.betamc.tsunami.profile.UserCache;

public class Tsunami {

    private Tsunami() {
    }

    public static String version() {
        return Tsunami.class.getPackage().getImplementationVersion();
    }

    public static TsunamiConfig config() {
        return TsunamiConfig.getInstance();
    }

    public static UserCache userCache() {
        return UserCache.getInstance();
    }

}

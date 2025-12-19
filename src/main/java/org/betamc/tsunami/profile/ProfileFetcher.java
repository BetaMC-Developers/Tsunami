package org.betamc.tsunami.profile;

import java.util.UUID;

public interface ProfileFetcher {

    void fetchOnlineProfile(String name, ProfileFetchCallback callback);

    static UUID parseDashlessUuid(String string) {
        return UUID.fromString(string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

}

package org.betamc.tsunami.profile;

import java.util.Optional;

public interface ProfileFetchCallback {

    void onSuccess(Optional<GameProfile> profile);

    void onFailure(Throwable cause);

}

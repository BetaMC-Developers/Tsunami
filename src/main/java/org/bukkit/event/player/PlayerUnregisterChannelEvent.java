package org.bukkit.event.player;

import org.bukkit.entity.Player;

/**
 * This is called immediately after a player unregisters for a plugin channel.
 */
public class PlayerUnregisterChannelEvent extends PlayerChannelEvent {

    public PlayerUnregisterChannelEvent(Player player, String channel) {
        super(Type.PLAYER_UNREGISTER_CHANNEL, player, channel);
    }
}
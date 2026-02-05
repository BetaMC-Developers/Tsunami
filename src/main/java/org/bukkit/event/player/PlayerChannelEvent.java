package org.bukkit.event.player;

import org.bukkit.entity.Player;

/**
 * This event is called after a player registers or unregisters a new plugin
 * channel.
 */
public abstract class PlayerChannelEvent extends PlayerEvent {
    private final String channel;

    public PlayerChannelEvent(Type type, Player player, String channel) {
        super(type, player);
        this.channel = channel;
    }

    public final String getChannel() {
        return channel;
    }
}
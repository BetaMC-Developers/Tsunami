package org.bukkit.plugin.messaging;

/**
 * Thrown if a plugin attempts to register for a reserved channel (such as "REGISTER")
 */
public class ReservedChannelException extends RuntimeException {

    public ReservedChannelException(String name) {
        super("Attempted to register for a reserved channel name ('" + name + "')");
    }
}

package org.betamc.tsunami.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;

import java.util.logging.Level;

public class NetworkUtil {

    public static void ensureOnMainThread(Packet packet, NetHandler netHandler, MinecraftServer server) {
        if (!server.isPrimaryThread()) {
            server.scheduleTask(() -> {
                try {
                    packet.a(netHandler);
                } catch (Exception e) {
                    MinecraftServer.log.log(Level.WARNING, "Failed to handle packet", e);
                    netHandler.disconnect("Internal server error");
                }
            });
            throw PacketScheduledException.INSTANCE;
        }
    }

}

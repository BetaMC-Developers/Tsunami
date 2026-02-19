package org.betamc.tsunami.network;

public final class PacketScheduledException extends RuntimeException {

    public static final PacketScheduledException INSTANCE = new PacketScheduledException();
}

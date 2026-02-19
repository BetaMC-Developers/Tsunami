package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkListenThread {

    public static Logger a = Logger.getLogger("Minecraft");
    private ServerSocket d;
    private Thread e;
    public volatile boolean b = false;
    private int f = 0;
    // Tsunami start - rewrite networking code
    //private List g = new ArrayList();
    //private ArrayList h = new ArrayList();
    private final List<NetHandler> connections = new CopyOnWriteArrayList<>();
    // Tsunami end
    public MinecraftServer c;

    public NetworkListenThread(MinecraftServer minecraftserver, InetAddress inetaddress, int i) throws IOException {
        this.c = minecraftserver;
        this.d = new ServerSocket(i, 0, inetaddress);
        this.d.setPerformancePreferences(0, 2, 1);
        this.b = true;
        this.e = new NetworkAcceptThread(this, "Listen thread", minecraftserver);
        this.e.start();
    }

    public void a(NetServerHandler netserverhandler) {
        addConnection(netserverhandler); // Tsunami
    }

    private void a(NetLoginHandler netloginhandler) {
        addConnection(netloginhandler); // Tsunami
    }

    // Tsunami start - rewrite networking code
    public void addConnection(NetHandler netHandler) {
        if (netHandler == null) {
            throw new IllegalArgumentException("Got null connection!");
        } else {
            this.connections.add(netHandler);
        }
    }
    // Tsunami end

    public void a() {
        // Tsunami start - rewrite networking code
        for (NetHandler netHandler : this.connections) {
            try {
                netHandler.a();
            } catch (Exception e) {
                a.log(Level.WARNING, "Failed to tick connection", e);
                netHandler.disconnect("Internal server error");
            }

            if (netHandler.disconnected()) {
                this.connections.remove(netHandler);
            }

            netHandler.getNetManager().a();
        }
        // Tsunami end
    }

    static ServerSocket a(NetworkListenThread networklistenthread) {
        return networklistenthread.d;
    }

    static int b(NetworkListenThread networklistenthread) {
        return networklistenthread.f++;
    }

    static void a(NetworkListenThread networklistenthread, NetLoginHandler netloginhandler) {
        networklistenthread.a(netloginhandler);
    }
}

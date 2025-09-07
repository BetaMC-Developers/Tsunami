package org.betamc.tsunami.rcon;

import net.minecraft.server.MinecraftServer;
import org.betamc.tsunami.Tsunami;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RconListenThread implements Runnable {

    private MinecraftServer mcServer;
    private ServerSocket serverSocket;

    public RconListenThread(MinecraftServer mcServer) throws IOException {
        String password = Tsunami.config().getString("rcon.password", "");
        if (password.isEmpty()) {
            MinecraftServer.log.warning("[Tsunami] Empty RCON password! Disabling RCON");
        } else {
            this.mcServer = mcServer;
            int port = Tsunami.config().getInt("rcon.port", 25575);
            serverSocket = new ServerSocket(port);
            new Thread(this).start();
            MinecraftServer.log.info("[Tsunami] RCON running on port " + port);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(500);
                new RconConnection(mcServer, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

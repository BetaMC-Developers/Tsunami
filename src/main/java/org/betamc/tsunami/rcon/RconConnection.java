package org.betamc.tsunami.rcon;

import net.minecraft.server.MinecraftServer;
import org.betamc.tsunami.Tsunami;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RconConnection implements Runnable {

    private final MinecraftServer mcServer;
    private final RconCommandSender commandSender;
    private final Socket socket;
    private final byte[] buffer = new byte[1460];
    private boolean authenticated = false;

    public RconConnection(MinecraftServer mcServer, Socket socket) throws IOException {
        this.mcServer = mcServer;
        this.socket = socket;
        this.socket.setSoTimeout(0);
        this.commandSender = new RconCommandSender(this);
        new Thread(this).start();
        MinecraftServer.log.info(this + " established a connection");
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                int i = inputStream.read(buffer, 0, 1460);
                if (10 > i) break;

                int cursor = 0;
                int length = RconUtil.intFromByteArray(buffer, 0, i);
                if (length != i - 4) break;

                cursor += 4;
                int requestId = RconUtil.intFromByteArray(buffer, cursor, i);
                cursor += 4;
                int type = RconUtil.intFromByteArray(buffer, cursor);
                cursor += 4;
                switch (type) {
                    case 2:
                        if (authenticated) {
                            String command = RconUtil.stringFromByteArray(buffer, cursor, i);
                            mcServer.issueRemoteCommand(this, requestId, command);
                            break;
                        }

                        sendAuthFailure();
                        break;
                    case 3:
                        String string = RconUtil.stringFromByteArray(buffer, cursor, i);
                        String password = Tsunami.config().getString("rcon.password", "");
                        if (!string.isEmpty() && string.equals(password)) {
                            authenticated = true;
                            send(requestId, 2, "");
                            MinecraftServer.log.info(this + " successfully authenticated");
                            break;
                        }

                        authenticated = false;
                        sendAuthFailure();
                        MinecraftServer.log.info(this + " failed to authenticate");
                        break;
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            MinecraftServer.log.info(this + " closed the connection");
        }
    }

    private void send(int id, int type, String message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1248);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(Integer.reverseBytes(bytes.length + 10));
        dataOutputStream.writeInt(Integer.reverseBytes(id));
        dataOutputStream.writeInt(Integer.reverseBytes(type));
        dataOutputStream.write(bytes);
        dataOutputStream.write(0);
        dataOutputStream.write(0);
        socket.getOutputStream().write(byteArrayOutputStream.toByteArray());
    }

    private void sendAuthFailure() throws IOException {
        send(-1, 2, "");
    }

    public void sendCmdResponse(int id, String message) throws IOException {
        int len = message.length();

        do {
            int i = Math.min(4096, len);
            send(id, 0, message.substring(0, i));
            message = message.substring(i);
            len = message.length();
        } while (0 != len);
    }

    public RconCommandSender getCommandSender() {
        return commandSender;
    }

    @Override
    public String toString() {
        return "RCON Client [" + socket.getRemoteSocketAddress() + "]";
    }

}

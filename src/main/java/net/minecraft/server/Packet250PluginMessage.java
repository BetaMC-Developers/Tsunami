package net.minecraft.server;

import org.bukkit.plugin.messaging.Messenger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet250PluginMessage extends Packet {

    public String channel;
    public byte[] message;

    public Packet250PluginMessage(String channel, byte[] message) {
        this.channel = channel;
        this.message = message;
    }

    public Packet250PluginMessage() {
    }

    private static byte[] readMessage(DataInputStream in) throws IOException {
        short length = in.readShort();
        if (length > Messenger.MAX_MESSAGE_SIZE) {
            throw new IOException("Received message length larger than maximum " + Messenger.MAX_MESSAGE_SIZE);
        }
        byte[] message = new byte[length];
        in.readFully(message);
        return message;
    }

    private static void writeMessage(byte[] message, DataOutputStream out) throws IOException {
        out.writeShort(message.length);
        out.write(message);
    }

    public void a(DataInputStream in) throws IOException {
        this.channel = a(in, Messenger.MAX_CHANNEL_SIZE);
        this.message = readMessage(in);
    }

    public void a(DataOutputStream out) throws IOException {
        a(this.channel, out);
        writeMessage(this.message, out);
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 2 + this.channel.length() * 2 + 2 + this.message.length;
    }

}

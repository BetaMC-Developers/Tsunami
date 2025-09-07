package org.betamc.tsunami.rcon;

import net.minecraft.server.ICommandListener;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class RconCommandSender extends ConsoleCommandSender implements ICommandListener {

    private final String name;
    private final StringBuffer buffer = new StringBuffer();

    public RconCommandSender(RconConnection connection) {
        super(Bukkit.getServer());
        this.name = connection.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendMessage(String message) {
        buffer.append(message + "\n");
    }

    public void prepareForCommand() {
        buffer.setLength(0);
    }

    public String getCommandResponse() {
        return buffer.toString();
    }

}

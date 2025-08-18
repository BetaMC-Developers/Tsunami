package org.bukkit.command.defaults;

import com.legacyminecraft.poseidon.PoseidonConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;

public class StopCommand extends VanillaCommand {
    private final String msgKickShutdown;
    
    public StopCommand() {
        super("stop");
        this.description = "Stops the server";
        this.usageMessage = "/stop";
        this.setPermission("bukkit.command.stop");
        this.msgKickShutdown = PoseidonConfig.getInstance().getConfigString("message.kick.shutdown");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        ((CraftServer) Bukkit.getServer()).setShuttingdown(true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.saveData();
            player.kickPlayer(this.msgKickShutdown);
        }
        for (World world : Bukkit.getWorlds()) {
            world.save();
        }

        Command.broadcastCommandMessage(sender, "Stopping the server..");
        Bukkit.shutdown();

        return true;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith("stop");
    }
}

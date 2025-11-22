package org.bukkit.craftbukkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;

public class ColouredConsoleSender extends ConsoleCommandSender {
    // Tsunami start - comment out
    //private final ConsoleReader reader;
    //private final Terminal terminal;
    //private final Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>(ChatColor.class);
    //private final ChatColor[] colors = ChatColor.values();

    public ColouredConsoleSender(CraftServer server) {
        super(server);
        //this.reader = server.getReader();
        //this.terminal = reader.getTerminal();
        // Tsunami end
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(ChatColor.convertToAnsi(message)); // Tsunami - improve chat color handling
    }

}

package net.minecraft.server;

import org.betamc.tsunami.Tsunami;
import org.bukkit.ChatColor;
import org.jline.reader.LineReader;

public class ThreadCommandReader extends Thread {

    final MinecraftServer server;
    private final String prompt; // Tsunami

    public ThreadCommandReader(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
        this.prompt = ChatColor.convertToAnsi(Tsunami.config().console().prompt()); // Tsunami - configurable prompt
    }

    public void run() {
        LineReader bufferedreader = this.server.reader; // Tsunami - ConsoleReader -> LineReader
        String s;

        // CraftBukkit start - JLine disabling compatibility
        while (!this.server.isStopped && MinecraftServer.isRunning(this.server)) {
            if (org.bukkit.craftbukkit.Main.useJline) {
                s = bufferedreader.readLine(this.prompt); // Tsunami
            } else {
                s = bufferedreader.readLine();
            }
            if (s != null && !s.isEmpty() && s.chars().anyMatch(i -> !Character.isWhitespace(i))) {
                this.server.issueCommand(s, this.server);
            }
            // CraftBukkit end
        }
        // Tsunami - delete catch
    }
}

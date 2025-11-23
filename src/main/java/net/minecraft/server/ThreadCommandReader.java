package net.minecraft.server;

import org.betamc.tsunami.Tsunami;
import org.bukkit.ChatColor;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

public class ThreadCommandReader extends Thread {

    final MinecraftServer server;
    private final String prompt; // Tsunami

    public ThreadCommandReader(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
        this.prompt = ChatColor.convertToAnsi(Tsunami.config().console().prompt()); // Tsunami - configurable prompt
        setName("Server console handler"); // Tsunami
    }

    public void run() {
        LineReader bufferedreader = this.server.reader; // Tsunami - ConsoleReader -> LineReader
        String s;

        try {
            // CraftBukkit start - JLine disabling compatibility
            while (!this.server.isStopped && MinecraftServer.isRunning(this.server)) {
                try {
                    if (org.bukkit.craftbukkit.Main.useJline) {
                        s = bufferedreader.readLine(this.prompt); // Tsunami
                    } else {
                        s = bufferedreader.readLine();
                    }
                    if (s != null && !s.isEmpty() && s.chars().anyMatch(i -> !Character.isWhitespace(i))) {
                        this.server.issueCommand(s, this.server);
                    }
                    // CraftBukkit end
                } catch (EndOfFileException e) { // Tsunami
                }
            }
        } catch (UserInterruptException e) {
            this.server.a(); // Tsunami - shut down gracefully
        }
    }
}

package net.minecraft.server;

import org.jline.reader.LineReader;

public class ThreadCommandReader extends Thread {

    final MinecraftServer server;

    public ThreadCommandReader(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
    }

    public void run() {
        LineReader bufferedreader = this.server.reader; // Tsunami - ConsoleReader -> LineReader
        String s;

        // CraftBukkit start - JLine disabling compatibility
        while (!this.server.isStopped && MinecraftServer.isRunning(this.server)) {
            if (org.bukkit.craftbukkit.Main.useJline) {
                s = bufferedreader.readLine("> ");
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

package org.bukkit.craftbukkit.util;

import org.bukkit.craftbukkit.Main;
import org.jline.reader.LineReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;

public class TerminalConsoleHandler extends ConsoleHandler {

    private final LineReader reader; // Tsunami - ConsoleReader -> LineReader

    public TerminalConsoleHandler(LineReader reader) { // Tsunami - ConsoleReader -> LineReader
        super();
        // Tsunami start
        if (Main.useJline) {
            setOutputStream(new JLineOutputStream());
        }
        // Tsunami end
        this.reader = reader;
    }

    // Tsunami start
    private class JLineOutputStream extends ByteArrayOutputStream {
        private JLineOutputStream() {
            super(1024);
        }

        @Override
        public synchronized void flush() throws IOException {
            reader.printAbove(JLineOutputStream.this.toString("UTF-8"));
            this.count = 0;
        }
    }
    // Tsunami end

}

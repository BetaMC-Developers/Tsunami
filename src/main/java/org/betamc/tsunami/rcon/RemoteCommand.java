package org.betamc.tsunami.rcon;

public class RemoteCommand {

    public final RconConnection connection;
    public final int requestId;
    public final String command;

    public RemoteCommand(RconConnection connection, int requestId, String command) {
        this.connection = connection;
        this.requestId = requestId;
        this.command = command;
    }

}

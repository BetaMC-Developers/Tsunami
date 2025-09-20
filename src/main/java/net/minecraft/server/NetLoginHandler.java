package net.minecraft.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.projectposeidon.ConnectionType;
import com.legacyminecraft.poseidon.PoseidonConfig;
import com.projectposeidon.johnymuffin.LoginProcessHandler;
import org.betamc.tsunami.Tsunami;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Logger;

import static com.legacyminecraft.poseidon.util.Release2Beta.deserializeAddress;

public class NetLoginHandler extends NetHandler {

    public static Logger a = Logger.getLogger("Minecraft");
    private static Random d = new Random();
    public NetworkManager networkManager;
    public boolean c = false;
    private MinecraftServer server;
    private int f = 0;
    private String g = null;
    private Packet1Login h = null;
    private String serverId = "";
    private ConnectionType connectionType;
    private boolean usingReleaseToBeta = false; //Poseidon -> Release2Beta support
    private boolean receivedLoginPacket = false;
    private int rawConnectionType;
    private boolean receivedKeepAlive = false;
    
    private final String msgKickShutdown;

    // Tsunami start
    public boolean requestedStatus = false;
    private int requestVersion = 14;
    // Tsunami end

    public NetLoginHandler(MinecraftServer minecraftserver, Socket socket, String s) {
        this.server = minecraftserver;
        this.networkManager = new NetworkManager(socket, s, this);
        this.networkManager.f = 0;
        
        this.msgKickShutdown = PoseidonConfig.getInstance().getConfigString("message.kick.shutdown");
    }

    // CraftBukkit start
    public Socket getSocket() {
        return this.networkManager.socket;
    }
    // CraftBukkit end

    public void a() {
        if (this.h != null) {
            this.b(this.h);
            this.h = null;
        }

        if (this.f++ == 600) {
            this.disconnect("Took too long to log in");
        } else {
            this.networkManager.b();
        }
    }

    public void disconnect(String s) {
        try {
            a.info("Disconnecting " + this.b() + ": " + s);
            this.networkManager.queue(new Packet255KickDisconnect(s));
            this.networkManager.d();
            this.c = true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void a(Packet2Handshake packet2handshake) {
        if (this.server.onlineMode) {
            this.serverId = Long.toHexString(d.nextLong());
            this.networkManager.queue(new Packet2Handshake(this.serverId));
        } else {
            this.networkManager.queue(new Packet2Handshake("-"));
        }
    }

    public void a(Packet0KeepAlive packet0KeepAlive) {
        receivedKeepAlive = true;
    }

    public void a(Packet1Login packet1login) {
        if (receivedLoginPacket) {
            this.disconnect("Multiple login packets received.");
            return;
        }
        receivedLoginPacket = true;
        this.g = packet1login.name;
        if (packet1login.a != 14) {
            if (packet1login.a > 14) {
                this.disconnect("Outdated server! I'm still on Beta 1.7.3");
            } else {
                this.disconnect("Outdated client! Please use Beta 1.7.3");
            }
        } else {
            //Project Poseidon - Start (Release2Beta)
            if (packet1login.d == (byte) -999 || packet1login.d == (byte) 25) {
                connectionType = ConnectionType.RELEASE2BETA_OFFLINE_MODE_IP_FORWARDING;
            } else if (packet1login.d == (byte) 26) {
                connectionType = ConnectionType.RELEASE2BETA_ONLINE_MODE_IP_FORWARDING;
            } else if (packet1login.d == (byte) 1) {
                connectionType = ConnectionType.RELEASE2BETA;
            } else if (packet1login.d == (byte) 2) {
                connectionType = ConnectionType.BUNGEECORD_OFFLINE_MODE_IP_FORWARDING;
            } else {
                connectionType = ConnectionType.NORMAL;
            }
            rawConnectionType = packet1login.d;
            //TODO: We need to find a better and cleaner way to support these different Beta proxies, Maybe a handler class???
            if ((Boolean) PoseidonConfig.getInstance().getConfigOption("settings.bungeecord.bungee-mode.enable") && !connectionType.equals(ConnectionType.BUNGEECORD_OFFLINE_MODE_IP_FORWARDING) && !connectionType.equals(ConnectionType.BUNGEECORD_ONLINE_MODE_IP_FORWARDING)) {
                a.info(packet1login.name + " is not using BungeeCord, kicking the player.");
                this.disconnect((String) PoseidonConfig.getInstance().getConfigOption("settings.bungeecord.bungee-mode.kick-message"));
                return;
            }
            
            if (connectionType.equals(ConnectionType.RELEASE2BETA_OFFLINE_MODE_IP_FORWARDING) || connectionType.equals(ConnectionType.RELEASE2BETA_ONLINE_MODE_IP_FORWARDING) || connectionType.equals(ConnectionType.BUNGEECORD_OFFLINE_MODE_IP_FORWARDING) || connectionType.equals(ConnectionType.BUNGEECORD_ONLINE_MODE_IP_FORWARDING)) {
                //Proxy has IP Forwarding enabled
                if ((Boolean) PoseidonConfig.getInstance().getConfigOption("settings.release2beta.enable-ip-pass-through")) {
                    //IP Forwarding is enabled server side
                    if (this.getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(String.valueOf(PoseidonConfig.getInstance().getConfigOption("settings.release2beta.proxy-ip", "127.0.0.1")))) {
                        //Release2Beta server is authorized - Override IP address
                        InetSocketAddress address = deserializeAddress(packet1login.c);
                        a.info(packet1login.name + " has been detected using Release2Beta, using the IP passed through: " + address.getAddress().getHostAddress());
                        this.networkManager.setSocketAddress(address);
                        this.usingReleaseToBeta = true;
                    } else {
                        //Release2Beta server isn't authorized
                        a.info(packet1login.name + " is attempting to use a unauthorized Release2Beta server, kicking the player.");
                        this.disconnect(ChatColor.RED + "The Release2Beta server you are connecting through is unauthorized.");
                        return;
                    }
                } else {
                    //Poseidon doesn't support IP Forwarding
                    a.info(packet1login.name + " is trying to connect through R2B with IP Forwarding enabled, however, it is disabled in Poseidon. Kicking player!");
                    this.disconnect(ChatColor.RED + "IP Forwarding is disabled in Poseidon. Please disable in Release2Beta.");
                    return;
                }
            }
            //Project Poseidon - End (Release2Beta

            if (((CraftServer) Bukkit.getServer()).isShuttingdown()) {
                this.disconnect(this.msgKickShutdown);
                return;
            }


            new LoginProcessHandler(this, packet1login, this.server.server, this.server.onlineMode);
            // (new ThreadLoginVerifier(this, packet1login, this.server.server)).start(); // CraftBukkit
//            }
        }
    }

    // Tsunami start - server list ping
    public void handleStatusRequest(byte firstByte, DataInputStream input) {
        try {
            int length = Packet.readVarInt(firstByte, input);
            int id = Packet.readVarInt(input);
            if (id == 0) {
                if (length != 1) {
                    requestVersion = Packet.readVarInt(input);
                    Packet.readUTF8(input);
                    input.readUnsignedShort();
                    Packet.readVarInt(input);
                    requestedStatus = true;
                } else {
                    doStatusResponse();
                }
            } else if (id == 1) {
                long time = input.readLong();
                doPongResponse(time);
            }
        } catch (IOException e) {
        }
    }

    private void doStatusResponse() throws IOException {
        JsonObject root = new JsonObject();

        JsonObject version = new JsonObject();
        version.addProperty("name", "b1.7.3");
        version.addProperty("protocol", requestVersion);
        root.add("version", version);

        JsonObject players = new JsonObject();
        players.addProperty("max", Bukkit.getMaxPlayers());
        players.addProperty("online", Bukkit.getOnlinePlayers().length);

        if (Tsunami.config().getBoolean("server-list-ping.show-player-names", true)) {
            JsonArray samples = new JsonArray();
            for (Player player : Bukkit.getOnlinePlayers()) {
                JsonObject sample = new JsonObject();
                sample.addProperty("name", player.getName());
                sample.addProperty("id", player.getUniqueId().toString());
                samples.add(sample);
            }
            players.add("sample", samples);
        }

        root.add("players", players);

        JsonObject description = new JsonObject();
        description.addProperty("text", Tsunami.config().getString("server-list-ping.motd", "A Minecraft Server"));
        root.add("description", description);

        if (server.serverIcon != null) {
            root.addProperty("favicon", "data:image/png;base64," + new String(server.serverIcon, StandardCharsets.ISO_8859_1));
        }

        ByteArrayOutputStream status = new ByteArrayOutputStream();
        String response = root.toString();
        Packet.writeVarInt(0, status);
        Packet.writeUTF8(response, status);
        Packet.writeVarInt(status.size(), this.networkManager.output);
        status.writeTo(this.networkManager.output);
        this.networkManager.output.flush();
    }

    private void doPongResponse(long time) throws IOException {
        ByteArrayOutputStream pong = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(pong);
        Packet.writeVarInt(1, data);
        data.writeLong(time);
        Packet.writeVarInt(pong.size(), this.networkManager.output);
        pong.writeTo(this.networkManager.output);
        this.networkManager.output.flush();

        this.networkManager.d();
        this.networkManager.socket.close();
        this.server.networkListenThread.b(this);
        this.c = true;
    }
    // Tsunami end

    public void b(Packet1Login packet1login) {
        EntityPlayer entityplayer = this.server.serverConfigurationManager.a(this, packet1login.name);

        if (entityplayer != null) {
            this.server.serverConfigurationManager.b(entityplayer);
            // entityplayer.a((World) this.server.a(entityplayer.dimension)); // CraftBukkit - set by Entity
            // CraftBukkit - add world and location to 'logged in' message.
            a.info(this.b() + " logged in with entity id " + entityplayer.id + " at ([" + entityplayer.world.worldData.name + "] " + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
            WorldServer worldserver = (WorldServer) entityplayer.world; // CraftBukkit
            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            NetServerHandler netserverhandler = new NetServerHandler(this.server, this.networkManager, entityplayer);
            //Poseidon Start
            netserverhandler.setUsingReleaseToBeta(usingReleaseToBeta);
            netserverhandler.setConnectionType(connectionType);
            netserverhandler.setRawConnectionType(rawConnectionType);
            netserverhandler.setReceivedKeepAlive(receivedKeepAlive);
            //Poseidon End
            netserverhandler.sendPacket(new Packet1Login("", entityplayer.id, worldserver.getSeed(), (byte) worldserver.worldProvider.dimension));
            netserverhandler.sendPacket(new Packet6SpawnPosition(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z));
            this.server.serverConfigurationManager.a(entityplayer, worldserver);
            // this.server.serverConfigurationManager.sendAll(new Packet3Chat("\u00A7e" + entityplayer.name + " joined the game."));  // CraftBukkit - message moved to join event
            this.server.serverConfigurationManager.c(entityplayer);
            netserverhandler.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
            this.server.networkListenThread.a(netserverhandler);
            netserverhandler.sendPacket(new Packet4UpdateTime(entityplayer.getPlayerTime())); // CraftBukkit - add support for player specific time
            entityplayer.syncInventory();
        }

        this.c = true;
    }

    public void a(String s, Object[] aobject) {
        a.info(this.b() + " lost connection");
        this.c = true;
    }

    public void a(Packet packet) {
        this.disconnect("Protocol error");
    }

    public String b() {
        return this.g != null ? this.g + " [" + this.networkManager.getSocketAddress().toString() + "]" : this.networkManager.getSocketAddress().toString();
    }

    //This can and will return null for multiple packets.
    public String getUsername() {
        return this.g;
    }

    public boolean c() {
        return true;
    }

    /**
     * @author moderator_man
     * @returns the session id for this player
     */
    public String getServerID() {
        return serverId;
    }

    static String a(NetLoginHandler netloginhandler) {
        return netloginhandler.serverId;
    }

    public static Packet1Login a(NetLoginHandler netloginhandler, Packet1Login packet1login) {
        return netloginhandler.h = packet1login;
    }
}
package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.event.PlayerReceivePacketEvent;
import org.betamc.tsunami.Tsunami;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkManager {

    public static final Object a = new Object();
    public static int b;
    public static int c;
    private Object g = new Object();
    public Socket socket; // CraftBukkit - private -> public
    private SocketAddress i; //Project Poseidon - remove final statement
    public DataInputStream input; // Tsunami - private -> public
    public DataOutputStream output; // Tsunami - private -> public
    private boolean l = true;
    private Queue<Packet> m = new ConcurrentLinkedQueue<>(); // Tsunami - ArrayList -> ConcurrentLinkedQueue
    private Queue<Packet> highPriorityQueue = new ConcurrentLinkedQueue<>(); // Tsunami - ArrayList -> ConcurrentLinkedQueue
    private Queue<Packet> lowPriorityQueue = new ConcurrentLinkedQueue<>(); // Tsunami - ArrayList -> ConcurrentLinkedQueue
    private NetHandler p;
    private boolean q = false;
    private Thread r;
    private Thread s;
    private boolean t = false;
    private String u = "";
    private Object[] v;
    private int w = 0;
    private AtomicInteger x = new AtomicInteger(0); // Tsunami - int -> AtomicInteger
    public static int[] d = new int[256];
    public static int[] e = new int[256];
    public int f = 0;
    private int lowPriorityQueueDelay = 50;
    private final boolean firePacketEvents;

    private final boolean spamDetection;

    private final int threshold;

    public NetworkManager(Socket socket, String s, NetHandler nethandler) {
        this.socket = socket;
        this.i = socket.getRemoteSocketAddress();
        this.p = nethandler;

        //Poseidon
        this.firePacketEvents = PoseidonConfig.getInstance().getBoolean("settings.packet-events.enabled", false);
        this.spamDetection = PoseidonConfig.getInstance().getBoolean("settings.packet-spam-detection.enabled", true);
        this.threshold = PoseidonConfig.getInstance().getInt("settings.packet-spam-detection.threshold", 1000);

        //Debug for packet spam detection
//        System.out.println("[Poseidon] Packet spam detection is " + (this.spamDetection ? "enabled" : "disabled") + " with a threshold of " + this.threshold + " packets");

        // CraftBukkit start - IPv6 stack in Java on BSD/OSX doesn't support setTrafficClass
        try {
            socket.setTrafficClass(24);
        } catch (SocketException e) {
        }
        // CraftBukkit end

        try {
            // CraftBukkit start - cant compile these outside the try
            socket.setSoTimeout(30000);
            socket.setTcpNoDelay(true); // Tsunami
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5120));
        } catch (java.io.IOException socketexception) {
            // CraftBukkit end
            System.err.println(socketexception.getMessage());
        }

        /* CraftBukkit start - moved up
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5120));
        // CraftBukkit end */
        this.s = new NetworkReaderThread(this, s + " read thread");
        this.r = new NetworkWriterThread(this, s + " write thread");
        this.s.start();
        this.r.start();
    }

    //Project Poseidon Start
    public void setSocketAddress(SocketAddress socketAddress) {
        this.i = socketAddress;
    }

    public SocketAddress generateSocketAddress(String hostname, int port) {
        return new InetSocketAddress(hostname, port);
    }

    //Project Poseidon End

    public void a(NetHandler nethandler) {
        this.p = nethandler;
    }

    public void queue(Packet packet) {
        if (!this.q) {
            this.x.addAndGet(packet.a() + 1); // Tsunami - non-blocking
            if (packet.k) {
                this.lowPriorityQueue.add(packet);
            } else {
                this.highPriorityQueue.add(packet);
            }
        }
    }

    private boolean f() {
        boolean flag = false;

        try {
            Packet highPriorityPacket = this.highPriorityQueue.peek(); // Tsunami
            int i;
            int[] aint;

            // Tsunami start - non-blocking logic
            if (highPriorityPacket != null && (this.f == 0 || System.currentTimeMillis() - highPriorityPacket.timestamp >= (long) this.f)) {
                this.highPriorityQueue.poll();
                this.x.addAndGet(-(highPriorityPacket.a() + 1));
                // Tsunami end

                Packet.a(highPriorityPacket, this.output);
                aint = e;
                i = highPriorityPacket.b();
                aint[i] += highPriorityPacket.a() + 1;
                flag = true;
            }

            Packet lowPriorityPacket = this.lowPriorityQueue.peek(); // Tsunami

            // CraftBukkit - don't allow low priority packet to be sent unless it was placed in the queue before the first packet on the high priority queue
            // Tsunami start - non-blocking logic
            if ((flag || this.lowPriorityQueueDelay-- <= 0) && lowPriorityPacket != null && (highPriorityPacket == null || highPriorityPacket.timestamp > lowPriorityPacket.timestamp)) {
                this.lowPriorityQueue.poll();
                this.x.addAndGet(-(lowPriorityPacket.a() + 1));
                // Tsunami end

                Packet.a(lowPriorityPacket, this.output);
                aint = e;
                i = lowPriorityPacket.b();
                aint[i] += lowPriorityPacket.a() + 1;
                this.lowPriorityQueueDelay = 0;
                flag = true;
            }

            return flag;
        } catch (Exception exception) {
            if (!this.t) {
                this.a(exception);
            }

            return false;
        }
    }

    public void a() {
        this.s.interrupt();
        this.r.interrupt();
    }

    private boolean g() {
        boolean flag = false;

        try {
            // Tsunami start - read packet id first, then check for a server list ping packet
            int id = -1;
            try {
                id = this.input.read();
            } catch (EOFException e) {
                System.out.println("Reached end of stream");
            } catch (SocketTimeoutException e) {
                System.out.println("Read timed out");
            } catch (SocketException e) {
                if (!(boolean) PoseidonConfig.getInstance().getConfigOption("settings.remove-join-leave-debug", true)) {
                    System.out.println("Connection reset");
                }
            }

            NetHandler nethandler = this.p;
            if (nethandler instanceof NetLoginHandler && (id > 2 || ((NetLoginHandler) nethandler).requestedStatus)) {
                if (Tsunami.config().getBoolean("server-list-ping.enabled", false)) {
                    ((NetLoginHandler) nethandler).handleStatusRequest((byte) id, this.input);
                    return false;
                }
            }
            // Tsunami end

            Packet packet = Packet.a(id, this.input, this.p.c());

            if (packet != null) {
                int[] aint = d;
                int i = packet.b();

                aint[i] += packet.a() + 1;
                this.m.add(packet);
                flag = true;
            } else {
                this.a("disconnect.endOfStream", new Object[0]);
            }

            return flag;
        } catch (Exception exception) {
            if (!this.t) {
                this.a(exception);
            }

            return false;
        }
    }

    private void a(Exception exception) {
        exception.printStackTrace();
        this.a("disconnect.genericReason", new Object[]{"Internal exception: " + exception.toString()});
    }

    public void a(String s, Object... aobject) {
        if (this.l) {
            this.t = true;
            this.u = s;
            this.v = aobject;
            (new NetworkMasterThread(this)).start();
            this.l = false;

            try {
                this.input.close();
                this.input = null;
            } catch (Throwable throwable) {
                ;
            }

            try {
                this.output.close();
                this.output = null;
            } catch (Throwable throwable1) {
                ;
            }

            try {
                this.socket.close();
                this.socket = null;
            } catch (Throwable throwable2) {
                ;
            }
        }
    }

    public void b() {
        boolean fast = PoseidonConfig.getInstance().getBoolean("settings.faster-packets.enabled", true);
        if (this.x.get() > (fast ? 2097152 : 1048576)) {
            this.a("disconnect.overflow", new Object[0]);
        }

        if (this.m.isEmpty()) {
            if (this.w++ == 1200) {
                this.a("disconnect.timeout", new Object[0]);
            }
        } else {
            this.w = 0;
        }

        int i = (fast ? 1000 : 100);

        //Poseidon - Packet spam detection
        if (spamDetection) {
            if (this.m.size() > threshold) {
                String playerUsername = "Unknown";
                if (this.p instanceof NetServerHandler) {
                    playerUsername = ((NetServerHandler) this.p).player.name;
                    ((NetServerHandler) this.p).disconnect(ChatColor.RED + "[Poseidon] You have been kicked for packet spamming.");
                } else {
                    this.a("disconnect.spam", new Object[0]);
                }
                System.out.println("[Poseidon] Player " + playerUsername + " has been kicked for packet spamming. The queue size was " + this.m.size() + " and the threshold was " + threshold + ".");
            }
        }

//        if(this.m.size() > 1000) {
//            String playerUsername = "Unknown";
//            if (this.p instanceof NetServerHandler) {
//                System.out.println("The packet queue size is " + this.m.size() + " for player " + ((NetServerHandler) this.p).player.name + ".");
//            }
//        }

        Packet packet;
        while ((packet = this.m.poll()) != null && i-- >= 0) { // Tsunami - poll
            //Poseidon Start - Packet Receive Event
            if (firePacketEvents && this.p instanceof NetServerHandler) {
                PlayerReceivePacketEvent event = new PlayerReceivePacketEvent(((NetServerHandler) this.p).player.name, packet);
                Bukkit.getPluginManager().callEvent(event);
                packet = event.getPacket();
                if (!event.isCancelled()) {
                    packet.a(this.p);
                }

            } else {
                packet.a(this.p);
            }

            //Poseidon End

//            packet.a(this.p);
        }

        this.a();
        if (this.t && this.m.isEmpty()) {
            this.p.a(this.u, this.v);
        }
    }

    public SocketAddress getSocketAddress() {
        return this.i;
    }

    public void d() {
        this.a();
        this.q = true;
        this.s.interrupt();
        (new ThreadMonitorConnection(this)).start();
    }

    public int e() {
        return this.lowPriorityQueue.size();
    }

    static boolean a(NetworkManager networkmanager) {
        return networkmanager.l;
    }

    static boolean b(NetworkManager networkmanager) {
        return networkmanager.q;
    }

    static boolean c(NetworkManager networkmanager) {
        return networkmanager.g();
    }

    static boolean d(NetworkManager networkmanager) {
        return networkmanager.f();
    }

    static DataOutputStream e(NetworkManager networkmanager) {
        return networkmanager.output;
    }

    static boolean f(NetworkManager networkmanager) {
        return networkmanager.t;
    }

    static void a(NetworkManager networkmanager, Exception exception) {
        networkmanager.a(exception);
    }

    static Thread g(NetworkManager networkmanager) {
        return networkmanager.s;
    }

    static Thread h(NetworkManager networkmanager) {
        return networkmanager.r;
    }
}

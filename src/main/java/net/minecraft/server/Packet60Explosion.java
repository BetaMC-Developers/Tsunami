package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.craftbukkit.util.LongHash;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class Packet60Explosion extends Packet {

    public double a;
    public double b;
    public double c;
    public float d;
    public LongOpenHashSet e; // Tsunami - Set -> LongOpenHashSet

    public Packet60Explosion() {}

    public Packet60Explosion(double d0, double d1, double d2, float f, Set set) {
        // Tsunami start - delegate
        this(d0, d1, d2, f, new LongOpenHashSet(
                ((Set<Object>) set).stream()
                        .map(c -> LongHash.toLong(((ChunkPosition) c).x, ((ChunkPosition) c).y, ((ChunkPosition) c).z))
                        .collect(Collectors.toSet())
                )
        );
        // Tsunami end
    }

    // Tsunami start
    public Packet60Explosion(double d0, double d1, double d2, float f, LongOpenHashSet set) {
        this.a = d0;
        this.b = d1;
        this.c = d2;
        this.d = f;
        this.e = set.clone();
    }
    // Tsunami end

    public void a(DataInputStream datainputstream) throws IOException {
        this.a = datainputstream.readDouble();
        this.b = datainputstream.readDouble();
        this.c = datainputstream.readDouble();
        this.d = datainputstream.readFloat();
        int i = datainputstream.readInt();

        this.e = new LongOpenHashSet(); // Tsunami
        int j = (int) this.a;
        int k = (int) this.b;
        int l = (int) this.c;

        for (int i1 = 0; i1 < i; ++i1) {
            int j1 = datainputstream.readByte() + j;
            int k1 = datainputstream.readByte() + k;
            int l1 = datainputstream.readByte() + l;

            this.e.add(LongHash.toLong(j1, k1, l1)); // Tsunami
        }
    }

    public void a(DataOutputStream dataoutputstream) throws IOException {
        dataoutputstream.writeDouble(this.a);
        dataoutputstream.writeDouble(this.b);
        dataoutputstream.writeDouble(this.c);
        dataoutputstream.writeFloat(this.d);
        dataoutputstream.writeInt(this.e.size());
        int i = (int) this.a;
        int j = (int) this.b;
        int k = (int) this.c;

        // Tsunami - ChunkPosition -> long
        LongIterator iterator = this.e.iterator();
        while (iterator.hasNext()) {
            long chunkPos = iterator.nextLong();
            int l = LongHash.high(chunkPos) - i;
            int i1 = LongHash.mid(chunkPos) - j;
            int j1 = LongHash.low(chunkPos) - k;

            dataoutputstream.writeByte(l);
            dataoutputstream.writeByte(i1);
            dataoutputstream.writeByte(j1);
        }
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 32 + this.e.size() * 3;
    }
}

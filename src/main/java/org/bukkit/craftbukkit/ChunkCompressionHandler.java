package org.bukkit.craftbukkit;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Deflater;

public class ChunkCompressionHandler implements Runnable {

    private static final int CHUNK_SIZE = 16 * 128 * 16 * 5 / 2;
    private static final int REDUCED_DEFLATE_THRESHOLD = CHUNK_SIZE / 4;
    private static final int DEFLATE_LEVEL_CHUNKS = 6;
    private static final int DEFLATE_LEVEL_PARTS = 1;

    private final EntityPlayer player;
    private final Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

    private final Deflater deflater = new Deflater();
    private byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];

    public ChunkCompressionHandler(EntityPlayer player) {
        this.player = player;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (!player.netServerHandler.disconnected) {
            try {
                Packet packet = packetQueue.poll();
                if (packet == null) continue;
                handlePacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void queuePacket(Packet packet) {
        packetQueue.offer(packet);
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof Packet51MapChunk) {
            Packet51MapChunk packet51mapchunk = (Packet51MapChunk) packet;
            if (packet51mapchunk.g == null) {
                int dataSize = packet51mapchunk.rawData.length;
                if (deflateBuffer.length < dataSize + 100) {
                    deflateBuffer = new byte[dataSize + 100];
                }

                deflater.reset();
                deflater.setLevel(dataSize < REDUCED_DEFLATE_THRESHOLD ? DEFLATE_LEVEL_PARTS : DEFLATE_LEVEL_CHUNKS);
                deflater.setInput(packet51mapchunk.rawData);
                deflater.finish();
                int size = deflater.deflate(deflateBuffer);
                if (size == 0) {
                    size = deflater.deflate(deflateBuffer);
                }

                packet51mapchunk.g = new byte[size];
                packet51mapchunk.h = size;
                System.arraycopy(deflateBuffer, 0, packet51mapchunk.g, 0, size);
            }
        }

        player.netServerHandler.networkManager.queue(packet);
    }

}

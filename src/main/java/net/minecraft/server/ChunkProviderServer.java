package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.betamc.tsunami.Tsunami;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.generator.BlockPopulator;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkProviderServer implements IChunkProvider {

    // Tsunami start
    private static final ExecutorService loadExecutor = Executors.newFixedThreadPool(
            Tsunami.config().world().asyncChunkLoading().threads(),
            new ChunkLoaderThreadFactory());
    private final Long2ObjectOpenHashMap<ChunkLoadTask> loadQueue = new Long2ObjectOpenHashMap<>();
    private final Queue<Runnable> postLoadQueue = new ConcurrentLinkedQueue<>();
    public final ObjectRBTreeSet<Chunk> autoSaveQueue = new ObjectRBTreeSet<>((c1, c2) -> {
        if (c1 == c2) return 0;
        int lastSaveCompare = Long.compare(c1.r, c2.r);
        if (lastSaveCompare != 0) return lastSaveCompare;
        return Long.compare(LongHash.toLong(c1.x, c1.z), LongHash.toLong(c2.x, c2.z));
    });
    private final ObjectArrayList<Chunk> autoSaveReschedule = new ObjectArrayList<>();
    long lastAutoSave;
    // Tsunami end

    // CraftBukkit start
    public LongLinkedOpenHashSet unloadQueue = new LongLinkedOpenHashSet(); // Tsunami - LongHashset -> LongLinkedOpenHashSet
    public Chunk emptyChunk;
    public IChunkProvider chunkProvider; // CraftBukkit
    private IChunkLoader e;
    public boolean forceChunkLoad = false;
    public Long2ObjectOpenHashMap<Chunk> chunks = new Long2ObjectOpenHashMap<>(); // Tsunami - LongHashtable -> Long2ObjectOpenHashMap
    //public List chunkList = new ArrayList(); // Tsunami
    public WorldServer world;
    // CraftBukkit end

    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, IChunkProvider ichunkprovider) {
        this.emptyChunk = new EmptyChunk(worldserver, new byte['\u8000'], 0, 0);
        this.world = worldserver;
        this.e = ichunkloader;
        this.chunkProvider = ichunkprovider;
        this.lastAutoSave = worldserver.worldData.f(); // Tsunami
    }

    public boolean isChunkLoaded(int i, int j) {
        return this.chunks.containsKey(LongHash.toLong(i, j)); // Tsunami - toLong
    }

    public void queueUnload(int i, int j) {
        ChunkCoordinates chunkcoordinates = this.world.getSpawn();
        int k = i * 16 + 8 - chunkcoordinates.x;
        int l = j * 16 + 8 - chunkcoordinates.z;
        short short1 = 128;

        if (k < -short1 || k > short1 || l < -short1 || l > short1 || !(this.world.keepSpawnInMemory)) { // CraftBukkit - added 'this.world.keepSpawnInMemory'
            this.unloadQueue.add(LongHash.toLong(i, j)); // Tsunami - toLong
        }
    }

    // Tsunami - moved code to postLoadChunk
    public Chunk getChunkAt(int i, int j) {
        this.unloadQueue.remove(LongHash.toLong(i, j));
        Chunk chunk = this.chunks.get(LongHash.toLong(i, j));

        if (chunk != null) {
            return chunk;
        } else {
            chunk = loadChunk(i, j);
            return postLoadChunk(chunk, i, j);
        }
    }

    // Tsunami start
    public CompletableFuture<Chunk> getChunkAtAsync(int i, int j) {
        this.unloadQueue.remove(LongHash.toLong(i, j));
        Chunk chunk = this.chunks.get(LongHash.toLong(i, j));

        if (chunk != null) {
            return CompletableFuture.completedFuture(chunk);
        } else {
            return loadChunkAsync(i, j);
        }
    }

    public void postLoadChunks() {
        int tasks = postLoadQueue.size();
        Runnable task;

        for (int i = 0; i < tasks && (task = postLoadQueue.poll()) != null; i++) {
            try {
                task.run();
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
    }
    // Tsunami end

    private Chunk postLoadChunk(Chunk chunk, int i, int j) {
        if (this.chunks.containsKey(LongHash.toLong(i, j))) { // Tsunami - toLong
            return this.chunks.get(LongHash.toLong(i, j)); // Tsunami - toLong
        }

        boolean newChunk = false;
        if (chunk == null) {
            if (this.chunkProvider == null) {
                chunk = this.emptyChunk;
            } else {
                chunk = this.chunkProvider.getOrCreateChunk(i, j);
            }
            newChunk = true; // CraftBukkit
        }

        this.chunks.put(LongHash.toLong(i, j), chunk); // Tsunami - toLong
        //this.chunkList.add(chunk); // Tsunami
        if (chunk != null) {
            chunk.loadNOP();
            chunk.addEntities();
        }

        // CraftBukkit start
        org.bukkit.Server server = this.world.getServer();
        if (server != null) {
            /*
             * If it's a new world, the first few chunks are generated inside
             * the World constructor. We can't reliably alter that, so we have
             * no way of creating a CraftWorld/CraftServer at that point.
             */
            server.getPluginManager().callEvent(new ChunkLoadEvent(chunk.bukkitChunk, newChunk));
        }
        // CraftBukkit end

        if (!chunk.done && this.isChunkLoaded(i + 1, j + 1) && this.isChunkLoaded(i, j + 1) && this.isChunkLoaded(i + 1, j)) {
            this.getChunkAt(this, i, j);
        }

        // Tsunami - getOrCreateChunk -> getChunkIfLoaded
        if (this.isChunkLoaded(i - 1, j) && !this.getChunkIfLoaded(i - 1, j).done && this.isChunkLoaded(i - 1, j + 1) && this.isChunkLoaded(i, j + 1) && this.isChunkLoaded(i - 1, j)) {
            this.getChunkAt(this, i - 1, j);
        }

        if (this.isChunkLoaded(i, j - 1) && !this.getChunkIfLoaded(i, j - 1).done && this.isChunkLoaded(i + 1, j - 1) && this.isChunkLoaded(i, j - 1) && this.isChunkLoaded(i + 1, j)) {
            this.getChunkAt(this, i, j - 1);
        }

        if (this.isChunkLoaded(i - 1, j - 1) && !this.getChunkIfLoaded(i - 1, j - 1).done && this.isChunkLoaded(i - 1, j - 1) && this.isChunkLoaded(i, j - 1) && this.isChunkLoaded(i - 1, j)) {
            this.getChunkAt(this, i - 1, j - 1);
        }

        // Tsunami start
        this.loadQueue.remove(LongHash.toLong(i, j));
        this.autoSaveQueue.add(chunk);
        // Tsunami end

        return chunk;
    }

    // Tsunami start
    public Chunk getChunkIfLoaded(int i, int j) {
        return this.chunks.get(LongHash.toLong(i, j));
    }
    // Tsunami end

    public Chunk getOrCreateChunk(int i, int j) {
        // CraftBukkit start
        Chunk chunk = this.chunks.get(LongHash.toLong(i, j)); // Tsunami - toLong

        //Poseidon chunk regenerate
        try {
            chunk = chunk == null ? (!this.world.isLoading && !this.forceChunkLoad ? this.emptyChunk : this.getChunkAt(i, j)) : chunk;
        } catch (Exception e) {
            //Poseidon chunk regenerate
            if (PoseidonConfig.getInstance().getConfigBoolean("emergency.debug.regenerate-corrupt-chunks.enable")) {
                System.out.println("Poseidon ran into a critical error when attempting to load a chunk (" + i + "," + j + "+. Regenerating chunk...");
                chunk = this.emptyChunk;
            } else  {
                System.out.println("Poseidon ran into a critical error when attempting to load a chunk (" + i + "," + j + "+. The server will now likely hang. Enabling \"emergency.debug.regenerate-corrupt-chunks.enable\" in the Poseidon.yml may help.");
                e.printStackTrace();
            }
            e.printStackTrace();
        }


        if (chunk == this.emptyChunk) return chunk;
        if (i != chunk.x || j != chunk.z) {
            MinecraftServer.log.info("Chunk (" + chunk.x + ", " + chunk.z + ") stored at  (" + i + ", " + j + ")");
            MinecraftServer.log.info(chunk.getClass().getName());
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
        return chunk;
        // CraftBukkit end
    }

    public Chunk loadChunk(int i, int j) { // CraftBukkit - private -> public
        if (this.e == null) {
            return null;
        } else {
            try {
                Chunk chunk = this.e.a(this.world, i, j);

                if (chunk != null) {
                    chunk.r = this.world.getTime();
                }

                return chunk;
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }
    }

    // Tsunami start
    public CompletableFuture<Chunk> loadChunkAsync(int i, int j) {
        ChunkLoadTask task = this.loadQueue.get(LongHash.toLong(i, j));
        if (task == null) {
            task = new ChunkLoadTask(i, j);
            this.loadQueue.put(LongHash.toLong(i, j), task);
            loadExecutor.execute(task);
        }

        return task.future;
    }
    // Tsunami end

    public void saveChunkNOP(Chunk chunk) { // CraftBukkit - private -> public
        if (this.e != null) {
            try {
                this.e.b(this.world, chunk);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void saveChunk(Chunk chunk) { // CraftBukkit - private -> public
        this.autoSaveQueue.remove(chunk); // Tsunami
        if (this.e != null) {
            try {
                chunk.r = this.world.getTime();
                this.e.a(this.world, chunk);
            } catch (Exception ioexception) { // CraftBukkit - IOException -> Exception
                ioexception.printStackTrace();
            }
        }
    }

    public void getChunkAt(IChunkProvider ichunkprovider, int i, int j) {
        Chunk chunk = this.getOrCreateChunk(i, j);

        if (!chunk.done) {
            chunk.done = true;
            if (this.chunkProvider != null) {
                this.chunkProvider.getChunkAt(ichunkprovider, i, j);

                // CraftBukkit start
                BlockSand.instaFall = true;
                Random random = new Random();
                random.setSeed(world.getSeed());
                long xRand = random.nextLong() / 2L * 2L + 1L;
                long zRand = random.nextLong() / 2L * 2L + 1L;
                random.setSeed((long) i * xRand + (long) j * zRand ^ world.getSeed());

                org.bukkit.World world = this.world.getWorld();
                if (world != null) {
                    for (BlockPopulator populator : world.getPopulators()) {
                        populator.populate(world, random, chunk.bukkitChunk);
                    }
                }
                BlockSand.instaFall = false;
                this.world.getServer().getPluginManager().callEvent(new ChunkPopulateEvent(chunk.bukkitChunk));
                // CraftBukkit end

                chunk.f();
            }
        }
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        // Tsunami start - rewrite world saving
        this.autoSaveReschedule.clear();
        long maxSaveTime = this.lastAutoSave - Tsunami.config().world().autoSaveInterval();
        int maxToSave = Tsunami.config().world().maxAutoSaveChunksPerTick();

        for (int autoSaved = 0; (flag || autoSaved < maxToSave) && !this.autoSaveQueue.isEmpty();) {
            Chunk chunk = this.autoSaveQueue.first();
            if (!flag && chunk.r > maxSaveTime) break;

            if (chunk.q || chunk.o) {
                this.saveChunk(chunk);
                chunk.o = false;
                autoSaved++;
            } else {
                this.autoSaveQueue.remove(chunk);
            }

            chunk.r = this.lastAutoSave;
            this.autoSaveReschedule.add(chunk);
        }

        this.autoSaveReschedule.forEach(this.autoSaveQueue::add);
        // Tsunami end

        return true;
    }

    public boolean unloadChunks() {
        if (!this.world.canSave) {
            // CraftBukkit start
            org.bukkit.Server server = this.world.getServer();
            for (int i = 0; i < 50 && !this.unloadQueue.isEmpty(); i++) {
                long chunkcoordinates = this.unloadQueue.removeFirstLong(); // Tsunami - removeFirstLong
                Chunk chunk = this.chunks.get(chunkcoordinates);
                if (chunk == null) continue;

                ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
                server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
//                    this.world.getWorld().preserveChunk((CraftChunk) chunk.bukkitChunk);

                    chunk.removeEntities();
                    this.saveChunk(chunk);
                    this.saveChunkNOP(chunk);
                    this.chunks.remove(chunkcoordinates); // CraftBukkit
                    //this.chunkList.remove(chunk); // Tsunami
                }
            }
            // CraftBukkit end

            if (this.e != null) {
                this.e.a();
            }
        }

        return this.chunkProvider.unloadChunks();
    }

    public boolean canSave() {
        return !this.world.canSave;
    }

    // Tsunami start
    private class ChunkLoadTask implements Runnable {

        private final int x;
        private final int z;
        private final CompletableFuture<Chunk> future = new CompletableFuture<>();

        private ChunkLoadTask(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public void run() {
            Chunk chunk = loadChunk(x, z);
            postLoadQueue.offer(() -> future.complete(postLoadChunk(chunk, x, z)));
        }

    }

    private static class ChunkLoaderThreadFactory implements ThreadFactory {
        private final AtomicInteger id = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task);
            thread.setName("Chunk loader thread #" + id.getAndIncrement());
            return thread;
        }
    }
    // Tsunami end

}

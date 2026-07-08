package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.entity.Player;
import org.bukkit.event.world.TreeGrowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockSapling extends BlockFlower {

    protected BlockSapling(int i, int j) {
        super(i, j);
        float f = 0.4F;

        this.a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
    }

    public void a(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            super.a(world, i, j, k, random);
            if (world.getLightLevel(i, j + 1, k) >= 9 && random.nextInt(30) == 0) {
                int l = world.getData(i, j, k);

                if ((l & 8) == 0) {
                    world.setData(i, j, k, l | 8);
                } else {
                    this.b(world, i, j, k, random, null); // Tsunami
                }
            }
        }
    }

    public int a(int i, int j) {
        j &= 3;
        return j == 1 ? 63 : (j == 2 ? 79 : super.a(i, j));
    }

    // Tsunami - change signature
    public void b(World world, int i, int j, int k, Random random, Player player) {
        int l = world.getData(i, j, k) & 3;

        world.setRawTypeId(i, j, k, 0);

        // CraftBukkit start - fixes client updates on recently grown trees
        boolean grownTree;
        // Tsunami start
        TreeType species;
        CapturingBlockChangeDelegate delegate = new CapturingBlockChangeDelegate(world);
        // Tsunami end

        if (l == 1) {
            species = TreeType.REDWOOD; // Tsunami
            grownTree = new WorldGenTaiga2().generate(delegate, random, i, j, k);
        } else if (l == 2) {
            species = TreeType.BIRCH; // Tsunami
            grownTree = new WorldGenForest().generate(delegate, random, i, j, k);
        } else {
            if (random.nextInt(10) == 0) {
                species = TreeType.BIG_TREE; // Tsunami
                grownTree = new WorldGenBigTree().generate(delegate, random, i, j, k);
            } else {
                species = TreeType.TREE; // Tsunami
                grownTree = new WorldGenTrees().generate(delegate, random, i, j, k);
            }
        }

        if (!grownTree) {
            world.setRawTypeIdAndData(i, j, k, this.id, l);
            return; // Tsunami
        }
        // CraftBukkit end

        // Tsunami start - add TreeGrowEvent
        TreeGrowEvent event = new TreeGrowEvent(new Location(world.getWorld(), i, j, k), species, player, delegate.getBlockStates());
        world.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.getBlocks().forEach(blockState -> blockState.update(true));
        }
        // Tsunami end
    }

    protected int a_(int i) {
        return i & 3;
    }

    // Tsunami start - add TreeGrowEvent
    private static final class CapturingBlockChangeDelegate implements BlockChangeDelegate {
        private final World world;
        private final Long2ObjectOpenHashMap<CraftBlockState> blockStates = new Long2ObjectOpenHashMap<>();

        private CapturingBlockChangeDelegate(World world) {
            this.world = world;
        }

        public boolean setRawTypeId(int x, int y, int z, int type) {
            CraftBlockState blockState = this.blockStates.computeIfAbsent(LongHash.toLong(x, y, z), k ->
                    CraftBlockState.getBlockState(this.world, x, y, z));
            blockState.setTypeId(type);
            blockState.setData((byte) 0);
            return true;
        }

        public boolean setRawTypeIdAndData(int x, int y, int z, int type, int data) {
            CraftBlockState blockState = this.blockStates.computeIfAbsent(LongHash.toLong(x, y, z), k ->
                    CraftBlockState.getBlockState(this.world, x, y, z));
            blockState.setTypeId(type);
            blockState.setData((byte) data);
            return true;
        }

        public int getTypeId(int x, int y, int z) {
            CraftBlockState blockState = this.blockStates.get(LongHash.toLong(x, y, z));
            if (blockState != null) {
                return blockState.getTypeId();
            }
            return this.world.getTypeId(x, y, z);
        }

        public List<BlockState> getBlockStates() {
            List<BlockState> blockStates = new ArrayList<>(this.blockStates.values());
            this.blockStates.clear();
            return blockStates;
        }
    }
    // Tsunami end
}

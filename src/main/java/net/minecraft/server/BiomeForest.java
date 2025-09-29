package net.minecraft.server;

import java.util.Random;

public class BiomeForest extends BiomeBase {

    // Tsunami start
    private final WorldGenForest GEN_FOREST = new WorldGenForest();
    private final WorldGenTrees GEN_TREES = new WorldGenTrees();
    // Tsunami end

    public BiomeForest() {
        this.t.add(new BiomeMeta(EntityWolf.class, 2));
    }

    public WorldGenerator a(Random random) {
        return (WorldGenerator) (random.nextInt(5) == 0 ? GEN_FOREST : (random.nextInt(3) == 0 ? new WorldGenBigTree() : GEN_TREES)); // Tsunami
    }
}

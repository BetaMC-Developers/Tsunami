package net.minecraft.server;

import java.util.Random;

public class BiomeRainforest extends BiomeBase {

    private final WorldGenTrees GEN_TREES = new WorldGenTrees(); // Tsunami

    public BiomeRainforest() {}

    public WorldGenerator a(Random random) {
        return (WorldGenerator) (random.nextInt(3) == 0 ? new WorldGenBigTree() : GEN_TREES); // Tsunami
    }
}

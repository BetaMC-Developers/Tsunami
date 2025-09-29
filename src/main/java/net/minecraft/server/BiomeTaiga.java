package net.minecraft.server;

import java.util.Random;

public class BiomeTaiga extends BiomeBase {

    // Tsunami start
    private final WorldGenTaiga1 GEN_TAIGA_1 = new WorldGenTaiga1();
    private final WorldGenTaiga2 GEN_TAIGA_2 = new WorldGenTaiga2();
    // Tsunami end

    public BiomeTaiga() {
        this.t.add(new BiomeMeta(EntityWolf.class, 2));
    }

    public WorldGenerator a(Random random) {
        return (WorldGenerator) (random.nextInt(3) == 0 ? GEN_TAIGA_1 : GEN_TAIGA_2); // Tsunami
    }
}

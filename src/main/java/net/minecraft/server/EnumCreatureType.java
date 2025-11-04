package net.minecraft.server;

import org.betamc.tsunami.Tsunami;

public enum EnumCreatureType {

    // Tsunami - make mob caps configurable
    MONSTER("monster", 0, IMonster.class, Tsunami.config().mobSpawning().mobCaps().monsters(), Material.AIR, false),
    CREATURE("creature", 1, EntityAnimal.class, Tsunami.config().mobSpawning().mobCaps().animals(), Material.AIR, true),
    WATER_CREATURE("waterCreature", 2, EntityWaterAnimal.class, Tsunami.config().mobSpawning().mobCaps().waterMobs(), Material.WATER, true);

    private final Class d;
    private final int e;
    private final Material f;
    private final boolean g;

    private static final EnumCreatureType[] h = new EnumCreatureType[] { MONSTER, CREATURE, WATER_CREATURE};

    private EnumCreatureType(String s, int i, Class oclass, int j, Material material, boolean flag) {
        this.d = oclass;
        this.e = j;
        this.f = material;
        this.g = flag;
    }

    public Class a() {
        return this.d;
    }

    public int b() {
        return this.e;
    }

    public Material c() {
        return this.f;
    }

    public boolean d() {
        return this.g;
    }
}

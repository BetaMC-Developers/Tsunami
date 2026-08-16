package org.betamc.tsunami.util;

import net.minecraft.server.*;

public class RaycastBoundingBox {

    public static MovingObjectPosition raycast(World world, Vec3D start, Vec3D end, Entity entity)
    {
        AxisAlignedBB boundingBox = entity.boundingBox.a(entity.m(), entity.m(), entity.m());

        Vec3D dir = Vec3D.create( end.a - start.a, end.b - start.b, end.c - start.c ).b();

        dir = Vec3D.create(dir.a/2, dir.b/2, dir.c/2);

        Vec3D step = start;

        for (double dist = start.a(end); dist >= 0; dist-= 0.5)
        {
            int tileId = world.getTypeId((int)step.a, (int)step.b, (int)step.c);

            Block block = Block.byId[tileId];

            int data = world.getData((int)step.a, (int)step.b, (int)step.c);

            if (block != null && block.a(data, true))
                return null;

            if (boundingBox.a(step))
                return new MovingObjectPosition(entity);

            step = step.add(dir.a, dir.b, dir.c);

            /*double d0 = world.random.nextGaussian() * 0.02;
            double d1 = world.random.nextGaussian() * 0.02;
            double d2 = world.random.nextGaussian() * 0.02;
            world.getWorld().playEffect(new Location(world.getWorld(), step.a, step.b, step.c), Effect.STEP_SOUND,  Block.DIAMOND_BLOCK.id);
                */
        }

        return null;
    }
}

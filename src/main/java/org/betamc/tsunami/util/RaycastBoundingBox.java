package org.betamc.tsunami.util;

import net.minecraft.server.*;

public class RaycastBoundingBox {

    public static MovingObjectPosition raycastDir(World world, Vec3D start, Vec3D dir, double maxDist, Entity entity)
    {
        AxisAlignedBB boundingBox = entity.boundingBox.a(entity.m(), entity.m(), entity.m());

        dir = Vec3D.create(dir.a/2, dir.b/2, dir.c/2);

        Vec3D step = start;

        for (double dist = maxDist; dist >= 0; dist-= 0.5)
        {
            int tileId = world.getTypeId((int)step.a, (int)step.b, (int)step.c);

            Block block = Block.byId[tileId];

            int data = world.getData((int)step.a, (int)step.b, (int)step.c);

            if (block != null && block.a(data, false))
                return null;

            if (boundingBox.a(step))
                return new MovingObjectPosition(entity);

            step = step.add(dir.a, dir.b, dir.c);
        }

        return null;
    }
}

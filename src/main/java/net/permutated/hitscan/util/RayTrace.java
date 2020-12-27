package net.permutated.hitscan.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.CheckForNull;
import java.util.Optional;
import java.util.function.Predicate;

public class RayTrace {

    @CheckForNull
    public static EntityRayTraceResult getEntityLookingAt(PlayerEntity player, double range)
    {
        return getEntityLookingAt(player, range, 1.0F);
    }

    @CheckForNull
    public static EntityRayTraceResult getEntityLookingAt(PlayerEntity player, double range, float ticks) {
        World world = player.world;

        Vector3d look = player.getLookVec();
        Vector3d start = player.getEyePosition(ticks);

        Vector3d end = new Vector3d(player.getPosX() + look.x * range, player.getPosYEye() + look.y * range, player.getPosZ() + look.z * range);
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);

        RayTraceResult rayTraceResult = world.rayTraceBlocks(context);
        double traceDistance = rayTraceResult.getHitVec().squareDistanceTo(start);

        AxisAlignedBB playerBox = player.getBoundingBox().expand(look.scale(traceDistance)).expand(1.0D, 1.0D, 1.0D);

        Predicate<Entity> filter = entity -> !entity.isSpectator() && entity.canBeCollidedWith() && entity instanceof LivingEntity;
        for (Entity possible : world.getEntitiesInAABBexcluding(player, playerBox, filter)) {
            AxisAlignedBB entityBox = possible.getBoundingBox().grow(0.3D);
            Optional<Vector3d> optional = entityBox.rayTrace(start, end);
            if (optional.isPresent()) {
                Vector3d position = optional.get();
                double distance = start.squareDistanceTo(position);

                if (distance < traceDistance) {
                    return new EntityRayTraceResult(possible, position);
                }
            }
        }
        return null;
    }

    @CheckForNull
    public static EntityRayTraceResult traceToEntity(PlayerEntity player, Entity target)
    {
        return traceToEntity(player, target, 1.0F);
    }

    @CheckForNull
    public static EntityRayTraceResult traceToEntity(PlayerEntity player, Entity target, float ticks) {
        Vector3d start = player.getEyePosition(ticks);
        Vector3d end = target.getPositionVec();

        AxisAlignedBB targetBox = target.getBoundingBox().grow(0.3D);
        Optional<Vector3d> optional = targetBox.rayTrace(start, end);

        return optional.map(vector3d -> new EntityRayTraceResult(target, vector3d)).orElse(null);
    }
}

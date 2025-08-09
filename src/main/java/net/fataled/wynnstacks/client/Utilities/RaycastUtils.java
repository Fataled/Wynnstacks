package net.fataled.wynnstacks.client.Utilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.List;


public final class RaycastUtils {


    public static Entity getLookedAtEntity(MinecraftClient client, double range, double coneAngle, boolean ignorePlayers) {
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null || client.world == null) return null;

        Vec3d camPos = cameraEntity.getCameraPosVec(1.0f);
        Vec3d camDir = cameraEntity.getRotationVec(1.0f);

        Box scanBox = cameraEntity.getBoundingBox().stretch(camDir.multiply(range)).expand(1.0);

        List<Entity> candidates = client.world.getOtherEntities(cameraEntity, scanBox, e ->
                e.isAlive()
                        && (!ignorePlayers || !(e instanceof PlayerEntity))
                        && !isDamageTextDisplay(e)
                        && !MobLabelUtils.getStatLines(e).isEmpty()
        );

        Entity best = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity e : candidates) {
            Vec3d entityPos = e.getPos().add(0, e.getHeight() / 2.0, 0);
            Vec3d toEntity = entityPos.subtract(camPos).normalize();
            double angle = Math.acos(camDir.dotProduct(toEntity));

            if (angle <= Math.toRadians(coneAngle)) {
                double dist = camPos.squaredDistanceTo(entityPos);
                double score = dist + angle * 5; // Angle is weighted higher than distance

                if (score < bestScore) {
                    bestScore = score;
                    best = e;
                }
            }
        }

        return best;
    }

    public static boolean isDamageTextDisplay(Entity entity) {
        if (!(entity instanceof TextDisplayEntity td)) return false;
        String text = td.getText() != null ? td.getText().getString().trim() : "";
        return MobLabelUtils.isProbablyDamageLine(text);
    }
}

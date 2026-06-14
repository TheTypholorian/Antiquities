package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.index.AntiqueParticles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WallJumpPacketReceiver {
    public static void registerServerPacket() {
        ServerPlayNetworking.registerGlobalReceiver(WallJumpPacketPayload.ID, (payload, context) -> context.server().execute(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());

            if (entity != null) {
                entity.level().playSound(null, entity.blockPosition(), SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1.0F, 1.0F);
                Vec3 pushVector = new Vec3(0, 0, 0);
                AABB box = entity.getBoundingBox();
                double offset = 1;

                boolean collidingWest = collidesWithSolidBlock(entity.level(), box.move(-offset, 0, 0), entity);
                boolean collidingEast = collidesWithSolidBlock(entity.level(), box.move(offset, 0, 0), entity);
                boolean collidingNorth = collidesWithSolidBlock(entity.level(), box.move(0, 0, -offset), entity);
                boolean collidingSouth = collidesWithSolidBlock(entity.level(), box.move(0, 0, offset), entity);

                double x = entity.getX();
                double z = entity.getZ();

                double particleX = entity.getX();
                double particleZ = entity.getZ();

                if (collidingWest) {
                    pushVector = pushVector.add(10, 0, 0); // Push east
                    x -= 0.15;
                    particleX += 0.1;
                }
                if (collidingEast) {
                    pushVector = pushVector.add(-10, 0, 0); // Push west
                    x += 0.15;
                    particleX -= 0.1;
                }
                if (collidingNorth) {
                    pushVector = pushVector.add(0, 0, 10); // Push south
                    z -= 0.15;
                    particleZ += 0.1;
                }
                if (collidingSouth) {
                    pushVector = pushVector.add(0, 0, -10); // Push north
                    z += 0.15;
                    particleZ -= 0.1;
                }

                if (entity instanceof ServerPlayer serverPlayer) {
                    for (ServerPlayer playerEntity : serverPlayer.level().players().stream().toList()) {
                        ServerPlayNetworking.send(playerEntity, new WallJumpParticlePacketPayload((float) x, (float) entity.getY(), (float) z, (float) particleX, (float) particleZ, pushVector));
                    }
                }
            }
        }));
    }

    private static boolean collidesWithSolidBlock(Level world, AABB box, Entity entity) {
        return world.getBlockCollisions(entity, box).iterator().hasNext();
    }

    public static void particles(Level world, float x, float y, float z, float particleX, float particleZ, Vec3 pushVector) {
        for (int i = 0; i < 10; i++) {
            world.addAlwaysVisibleParticle(
                    AntiqueParticles.DUST_PARTICLE,
                    particleX + Math.random() * 0.5 - 0.25,
                    y + 0.5 + Math.random() * 0.5 - 0.5,
                    particleZ + Math.random() * 0.5 - 0.25,
                    pushVector.x * 0.0025,
                    pushVector.y * Math.random() * 0.05,
                    pushVector.z * 0.0025
            );
        }

        world.addAlwaysVisibleParticle(AntiqueParticles.SPARKLE_PARTICLE, x, y + 0.5, z, pushVector.x, 0, pushVector.z);
    }
}

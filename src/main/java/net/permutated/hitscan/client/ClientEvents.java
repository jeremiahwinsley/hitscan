package net.permutated.hitscan.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.permutated.hitscan.ModRegistry;
import net.permutated.hitscan.item.ItemHitscanWeapon;
import net.permutated.hitscan.network.NetworkDispatcher;
import net.permutated.hitscan.network.PacketWeaponFired;
import net.permutated.hitscan.network.PacketWeaponReload;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    public static final KeyBinding RELOAD_KEY = new KeyBinding("key.hitscan.reload",
        KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.hitscan.category");

    private ClientEvents() {
        // nothing to do
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(RELOAD_KEY);
    }

    @SubscribeEvent
    public static void onRenderPlayerEvent(RenderPlayerEvent event) {
        PlayerEntity playerEntity = event.getPlayer();
        if (!ItemHitscanWeapon.getWeapon(playerEntity).isEmpty()) {
            event.getRenderer().getEntityModel().rightArmPose = BipedModel.ArmPose.CROSSBOW_HOLD;
            event.getRenderer().getEntityModel().leftArmPose = BipedModel.ArmPose.CROSSBOW_HOLD;
        }
    }


    @SubscribeEvent
    public static void handleReloadEvent(TickEvent.ClientTickEvent event) {
        if (RELOAD_KEY.isPressed() && event.phase == TickEvent.Phase.START) {
            Optional<PlayerEntity> playerEntity = Optional.ofNullable(Minecraft.getInstance().player);
            Optional<Item> item = playerEntity.map(ItemHitscanWeapon::getWeapon).map(ItemStack::getItem);
            boolean isWeapon = item.map(i -> i instanceof ItemHitscanWeapon).orElse(false);

            if (playerEntity.isPresent() && item.isPresent() && isWeapon) {
                boolean hasCooldown = playerEntity.get().getCooldownTracker().hasCooldown(item.get());
                if (!hasCooldown) {
                    NetworkDispatcher.INSTANCE.sendToServer(new PacketWeaponReload());
                    playerEntity.get().playSound(ModRegistry.PP7_RELOAD.get(), 1.0F, 1.0F);
                }
            }
        }
    }

    public static void handlePacketWeaponFired(PacketWeaponFired event) {
        Optional<PlayerEntity> playerEntity = Optional.ofNullable(Minecraft.getInstance().player);
        Optional<World> world = playerEntity.map(PlayerEntity::getEntityWorld);
        Optional<Entity> target = world.map(w -> w.getEntityByID(event.getTargetId()));

        Boolean validItem = world.map(w -> w.getPlayerByUuid(event.getPlayerUUID()))
            .map(ItemHitscanWeapon::getWeapon)
            .map(ItemStack::getItem)
            .map(item -> item instanceof ItemHitscanWeapon)
            .orElse(false);

        if (world.isPresent() && target.isPresent() && world.get().isRemote && validItem.equals(true)) {
            Random random = world.get().getRandom();
            DoubleUnaryOperator randomize = d -> d + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1.0D : -1.0D);

            IntStream.range(0, 8).forEach(i -> {
                double posX = randomize.applyAsDouble(target.get().getPosX() + 0.25D);
                double posY = randomize.applyAsDouble(target.get().getPosY() + 0.8D);
                double posZ = randomize.applyAsDouble(target.get().getPosZ() + 0.25D);
                world.get().addParticle(ParticleTypes.CRIT, posX, posY, posZ, 0.0D, 0.005D, 0.0D);
            });
        }
    }
}

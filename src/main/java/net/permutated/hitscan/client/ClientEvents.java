package net.permutated.hitscan.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.permutated.hitscan.ModRegistry;
import net.permutated.hitscan.item.ItemHitscanWeapon;
import net.permutated.hitscan.network.NetworkDispatcher;
import net.permutated.hitscan.network.PacketWeaponFired;
import net.permutated.hitscan.network.PacketWeaponReload;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    public static final KeyBinding RELOAD_KEY = new KeyBinding("key.hitscan.reload",
        KeyConflictContext.UNIVERSAL, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.hitscan.category");

    @SubscribeEvent
    public void onClientSetupEvent(final FMLClientSetupEvent event) {
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
    public static void handleReloadEvent(InputEvent.KeyInputEvent event) {
        if (RELOAD_KEY.isPressed()) {
            Optional<PlayerEntity> playerEntity = Optional.ofNullable(Minecraft.getInstance().player);
            Boolean validItem = playerEntity.map(ItemHitscanWeapon::getWeapon)
                .map(ItemStack::getItem).map(item -> item instanceof ItemHitscanWeapon).orElse(false);

            if (playerEntity.isPresent() && validItem) {
                NetworkDispatcher.INSTANCE.sendToServer(new PacketWeaponReload());
                playerEntity.get().playSound(ModRegistry.PP7_RELOAD.get(), 1.0F, 1.0F);
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

        if (playerEntity.isPresent() && target.isPresent() && world.get().isRemote && validItem) {
            Random random = world.get().getRandom();
            Function<Double, Double> randomize = (d) -> {
                return d + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1.0D : -1.0D);
            };

            IntStream.range(0, 8).forEach(i -> {
                double posX = randomize.apply(target.get().getPosX() + 0.25D);
                double posY = randomize.apply(target.get().getPosY() + 0.8D);
                double posZ = randomize.apply(target.get().getPosZ() + 0.25D);
                world.get().addParticle(ParticleTypes.CRIT, posX, posY, posZ, 0.0D, 0.005D, 0.0D);
            });
        }
    }
}
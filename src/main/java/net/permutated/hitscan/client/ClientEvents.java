package net.permutated.hitscan.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
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
    public static final KeyMapping RELOAD_KEY = new KeyMapping("key.hitscan.reload",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.hitscan.category");

    private ClientEvents() {
        // nothing to do
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(RELOAD_KEY);
    }

    @SubscribeEvent
    public static void onRenderPlayerEvent(RenderPlayerEvent event) {
        Player playerEntity = event.getPlayer();
        if (!ItemHitscanWeapon.getWeapon(playerEntity).isEmpty()) {
            event.getRenderer().getModel().rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
            event.getRenderer().getModel().leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
    }


    @SubscribeEvent
    public static void handleReloadEvent(TickEvent.ClientTickEvent event) {
        if (RELOAD_KEY.consumeClick() && event.phase == TickEvent.Phase.START) {
            Optional<Player> playerEntity = Optional.ofNullable(Minecraft.getInstance().player);
            Optional<Item> item = playerEntity.map(ItemHitscanWeapon::getWeapon).map(ItemStack::getItem);

            if (playerEntity.isPresent() && item.isPresent() && item.get() instanceof ItemHitscanWeapon weapon) {
                boolean hasCooldown = playerEntity.get().getCooldowns().isOnCooldown(weapon);
                if (!hasCooldown) {
                    NetworkDispatcher.INSTANCE.sendToServer(new PacketWeaponReload());
                    playerEntity.get().playSound(ModRegistry.PP7_RELOAD.get(), 1.0F, 1.0F);
                }
            }
        }
    }

    public static void handlePacketWeaponFired(PacketWeaponFired event) {
        Optional<Player> playerEntity = Optional.ofNullable(Minecraft.getInstance().player);
        Optional<Level> level = playerEntity.map(Player::getCommandSenderWorld);
        Optional<Entity> target = level.map(w -> w.getEntity(event.getTargetId()));

        Boolean validItem = level.map(w -> w.getPlayerByUUID(event.getPlayerUUID()))
            .map(ItemHitscanWeapon::getWeapon)
            .map(ItemStack::getItem)
            .map(item -> item instanceof ItemHitscanWeapon)
            .orElse(false);

        if (level.isPresent() && target.isPresent() && level.get().isClientSide && validItem.equals(true)) {
            Random random = level.get().getRandom();
            DoubleUnaryOperator randomize = d -> d + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1.0D : -1.0D);

            IntStream.range(0, 8).forEach(i -> {
                double posX = randomize.applyAsDouble(target.get().getX() + 0.25D);
                double posY = randomize.applyAsDouble(target.get().getY() + 0.8D);
                double posZ = randomize.applyAsDouble(target.get().getZ() + 0.25D);
                level.get().addParticle(ParticleTypes.CRIT, posX, posY, posZ, 0.0D, 0.005D, 0.0D);
            });
        }
    }
}

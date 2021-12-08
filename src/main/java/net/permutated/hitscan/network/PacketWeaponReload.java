package net.permutated.hitscan.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.permutated.hitscan.item.ItemHitscanWeapon;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketWeaponReload {
    public PacketWeaponReload() {
        // nothing to do
    }

    public PacketWeaponReload(FriendlyByteBuf buffer) {
        // nothing to do
    }

    @SuppressWarnings("java:S1172")
    public static void handle(PacketWeaponReload event, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Optional<Player> player = Optional.ofNullable(ctx.get().getSender());
            Optional<Level> world = player.map(Player::getCommandSenderWorld);

            if (player.isPresent() && !world.get().isClientSide) {
                ItemStack heldItem = ItemHitscanWeapon.getWeapon(player.get());
                boolean hasCooldown = player.get().getCooldowns().isOnCooldown(heldItem.getItem());
                if (heldItem.getItem() instanceof ItemHitscanWeapon && !hasCooldown) {
                    ItemHitscanWeapon.doReload(world.get(), player.get(), heldItem);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        // nothing to do
    }
}

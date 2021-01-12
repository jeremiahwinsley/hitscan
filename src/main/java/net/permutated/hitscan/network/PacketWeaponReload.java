package net.permutated.hitscan.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.permutated.hitscan.item.ItemHitscanWeapon;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketWeaponReload {
    public PacketWeaponReload() {
        // nothing to do
    }

    public PacketWeaponReload(PacketBuffer buffer) {
        // nothing to do
    }

    @SuppressWarnings("java:S1172")
    public static void handle(PacketWeaponReload event, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Optional<PlayerEntity> player = Optional.ofNullable(ctx.get().getSender());
            Optional<World> world = player.map(PlayerEntity::getEntityWorld);

            if (player.isPresent() && !world.get().isRemote) {
                ItemStack heldItem = ItemHitscanWeapon.getWeapon(player.get());
                if (heldItem.getItem() instanceof ItemHitscanWeapon) {
                    ItemHitscanWeapon.doReload(world.get(), player.get(), heldItem);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(PacketBuffer buffer) {
        // nothing to do
    }
}

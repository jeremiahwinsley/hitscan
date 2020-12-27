package net.permutated.hitscan.network;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.permutated.hitscan.client.ClientEvents;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketWeaponFired {
    private final UUID player;
    private final int target;

    public PacketWeaponFired(PlayerEntity playerEntity, LivingEntity targetEntity) {
        this.player = playerEntity.getUniqueID();
        this.target = targetEntity.getEntityId();
    }

    public PacketWeaponFired(PacketBuffer buffer) {
        player = buffer.readUniqueId();
        target = buffer.readInt();
    }

    public static void handle(PacketWeaponFired event, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientEvents.handlePacketWeaponFired(event));
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(this.player);
        buffer.writeInt(this.target);
    }

    public UUID getPlayerUUID() {
        return this.player;
    }

    public int getTargetId() {
        return this.target;
    }
}

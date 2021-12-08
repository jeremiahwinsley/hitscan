package net.permutated.hitscan.network;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.permutated.hitscan.client.ClientEvents;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketWeaponFired {
    private final UUID player;
    private final int target;

    public PacketWeaponFired(Player playerEntity, LivingEntity targetEntity) {
        this.player = playerEntity.getUUID();
        this.target = targetEntity.getId();
    }

    public PacketWeaponFired(FriendlyByteBuf buffer) {
        player = buffer.readUUID();
        target = buffer.readInt();
    }

    public static void handle(PacketWeaponFired event, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientEvents.handlePacketWeaponFired(event));
        ctx.get().setPacketHandled(true);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.player);
        buffer.writeInt(this.target);
    }

    public UUID getPlayerUUID() {
        return this.player;
    }

    public int getTargetId() {
        return this.target;
    }
}

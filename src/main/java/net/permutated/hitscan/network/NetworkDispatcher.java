package net.permutated.hitscan.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.permutated.hitscan.Hitscan;

public class NetworkDispatcher {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Hitscan.MODID, "main"),
        () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int packetIndex = 0;
        INSTANCE.registerMessage(packetIndex++, PacketWeaponFired.class, PacketWeaponFired::toBytes, PacketWeaponFired::new, PacketWeaponFired::handle);
        INSTANCE.registerMessage(packetIndex++, PacketWeaponReload.class, PacketWeaponReload::toBytes, PacketWeaponReload::new, PacketWeaponReload::handle);

        Hitscan.LOGGER.info("Registered {} network packets", packetIndex);
    }
}

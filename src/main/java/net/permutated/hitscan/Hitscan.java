package net.permutated.hitscan;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.permutated.hitscan.client.ClientEvents;
import net.permutated.hitscan.network.NetworkDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hitscan.MODID)
public class Hitscan {

    public static final String MODID = "hitscan";

    public static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("java:S1118")
    public Hitscan() {
        LOGGER.info("Registering mod: {}", MODID);
        ModRegistry.register();
        NetworkDispatcher.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetupEvent);
    }

    public void onClientSetupEvent(final FMLClientSetupEvent event) {
        ClientEvents.register();
    }
}

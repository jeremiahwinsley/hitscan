package net.permutated.hitscan;

import net.minecraftforge.fml.common.Mod;
import net.permutated.hitscan.network.NetworkDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hitscan.MODID)
public class Hitscan {

    public static final String MODID = "hitscan";

    public static final Logger LOGGER = LogManager.getLogger();

    public Hitscan() {
        LOGGER.info("Registering mod: {}", MODID);
        ModRegistry.register();
        NetworkDispatcher.register();
    }
}

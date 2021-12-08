package net.permutated.hitscan;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.permutated.hitscan.item.ItemHitscanWeapon;

import javax.annotation.Nonnull;
import java.util.function.Supplier;


public class ModRegistry {
    private ModRegistry() {
        // nothing to do
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hitscan.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Hitscan.MODID);

    public static final CreativeModeTab CREATIVE_MODE_TAB = new ModCreativeModeTab(Hitscan.MODID,
        () -> new ItemStack(ModRegistry.SILVER_PP7.get()));

    // Items
    public static final RegistryObject<Item> SILVER_PP7 = ITEMS.register("silver_pp7", ItemHitscanWeapon::new);

    // Sounds
    public static final RegistryObject<SoundEvent> PP7_GUNSHOT = SOUNDS.register("pp7_gunshot",
        () -> new SoundEvent(new ResourceLocation(Hitscan.MODID, "pp7_gunshot")));

    public static final RegistryObject<SoundEvent> PP7_RELOAD = SOUNDS.register("pp7_reload",
        () -> new SoundEvent(new ResourceLocation(Hitscan.MODID, "pp7_reload")));

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final class ModCreativeModeTab extends CreativeModeTab
    {
        private final Supplier<ItemStack> iconSupplier;

        public ModCreativeModeTab(final String name, final Supplier<ItemStack> iconSupplier)
        {
            super(name);
            this.iconSupplier = iconSupplier;
        }

        @Nonnull
        @Override
        public ItemStack makeIcon()
        {
            return iconSupplier.get();
        }
    }
}

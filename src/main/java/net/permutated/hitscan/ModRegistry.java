package net.permutated.hitscan;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.permutated.hitscan.item.ItemHitscanWeapon;

import java.util.function.Supplier;


public class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Hitscan.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Hitscan.MODID);

    public static final ItemGroup ITEM_GROUP = new ModItemGroup(Hitscan.MODID,
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

    public static final class ModItemGroup extends ItemGroup
    {
        private final Supplier<ItemStack> iconSupplier;

        public ModItemGroup(final String name, final Supplier<ItemStack> iconSupplier)
        {
            super(name);
            this.iconSupplier = iconSupplier;
        }

        @Override
        public ItemStack createIcon()
        {
            return iconSupplier.get();
        }
    }
}

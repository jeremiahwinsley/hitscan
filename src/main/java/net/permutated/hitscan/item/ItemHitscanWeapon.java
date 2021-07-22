package net.permutated.hitscan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.permutated.hitscan.ModRegistry;
import net.permutated.hitscan.network.NetworkDispatcher;
import net.permutated.hitscan.network.PacketWeaponFired;
import net.permutated.hitscan.util.RayTrace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ItemHitscanWeapon extends Item {

    private final int weaponRange;
    private final int weaponDamage;
    private final int maxAmmo;

    public ItemHitscanWeapon() {
        super(new Properties().stacksTo(1).tab(ModRegistry.CREATIVE_MODE_TAB).setNoRepair());

        weaponRange = 50;
        weaponDamage = 16;
        maxAmmo = 7;
    }

    public int getMaxAmmo() {
        return this.maxAmmo;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, level, tooltip, flagIn);

        tooltip.add(new TranslatableComponent("tooltip.hitscan.silver_pp7").withStyle(ChatFormatting.GOLD));
        tooltip.add(new TextComponent(""));
        tooltip.add(new TranslatableComponent("tooltip.hitscan.pp7_damage", this.weaponDamage).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(new TranslatableComponent("tooltip.hitscan.pp7_range", this.weaponRange).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(new TranslatableComponent("tooltip.hitscan.pp7_ammo", getAmmoCount(stack), getMaxAmmo()).withStyle(ChatFormatting.DARK_GREEN));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
        ItemStack weapon = playerIn.getItemInHand(handIn);

        if (doReloadIfEmpty(level, playerIn, weapon)) {
            return InteractionResultHolder.consume(weapon);
        }

        // TODO upgrades
        // TODO dual wield
        EntityHitResult result = RayTrace.getEntityLookingAt(playerIn, this.weaponRange);
        if (result != null) {

            if (!level.isClientSide) {
                // do damage
                Entity target = result.getEntity();
                if (target instanceof LivingEntity livingEntity) {

                    DamageSource damageSource = DamageSource.playerAttack(playerIn);
                    playerIn.setLastHurtMob(livingEntity);

                    double ratioX = Mth.sin(playerIn.getYRot() * ((float) Math.PI / 180F));
                    double ratioZ = -Mth.cos(playerIn.getYRot() * ((float) Math.PI / 180F));

                    livingEntity.knockback(0.5F, ratioX, ratioZ);
                    livingEntity.hurt(damageSource, weaponDamage);
                    useOneAmmo(weapon);

                    NetworkDispatcher.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> livingEntity),
                        new PacketWeaponFired(playerIn, livingEntity));

                    level.playSound(null, playerIn.blockPosition(),
                        ModRegistry.PP7_GUNSHOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    if (getAmmoCount(weapon) > 0) {
                        playerIn.getCooldowns().addCooldown(this, 20);
                    }
                }
            }

            return InteractionResultHolder.consume(weapon);
        } else {
            return InteractionResultHolder.pass(weapon);
        }
    }

    public static boolean doReloadIfEmpty(Level level, Player playerIn, ItemStack weapon) {
        if (getAmmoCount(weapon) == 0) {
            doReload(level, playerIn, weapon);
            return true;
        } else {
            return false;
        }
    }

    public static void doReload(Level level, Player playerIn, ItemStack weapon) {
        if (!level.isClientSide) {
            reloadAmmo(weapon);
            playerIn.getCooldowns().addCooldown(weapon.getItem(), 60);
        } else {
            playerIn.playSound(ModRegistry.PP7_RELOAD.get(), 1.0F, 1.0F);
        }
    }

    public static ItemStack getWeapon(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof ItemHitscanWeapon)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof ItemHitscanWeapon)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public static Optional<ItemHitscanWeapon> resolve(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemHitscanWeapon) {
            return Optional.of((ItemHitscanWeapon) item);
        } else {
            return Optional.empty();
        }
    }

    public static int getAmmoCount(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getOrCreateTag();

        if (!nbt.contains("ammo")) {
            reloadAmmo(itemStack);
        }

        return nbt.getInt("ammo");
    }

    private static void reloadAmmo(ItemStack itemStack) {
        resolve(itemStack).ifPresent(item -> itemStack.getOrCreateTag().putInt("ammo", item.getMaxAmmo()));
    }

    private static void useOneAmmo(ItemStack itemStack) {
        resolve(itemStack).ifPresent(item -> itemStack.getOrCreateTag().putInt("ammo", getAmmoCount(itemStack) - 1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack itemStack) {
        return getAmmoCount(itemStack) < getMaxAmmo();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return (getMaxAmmo() - getAmmoCount(itemStack)) / 7.0D;
    }
}

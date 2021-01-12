package net.permutated.hitscan.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
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
        super(new Properties().maxStackSize(1).group(ModRegistry.ITEM_GROUP).setNoRepair());

        weaponRange = 50;
        weaponDamage = 16;
        maxAmmo = 7;
    }

    public int getMaxAmmo() {
        return this.maxAmmo;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(new TranslationTextComponent("tooltip.hitscan.silver_pp7").mergeStyle(TextFormatting.GOLD));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("tooltip.hitscan.pp7_damage", this.weaponDamage).mergeStyle(TextFormatting.DARK_GREEN));
        tooltip.add(new TranslationTextComponent("tooltip.hitscan.pp7_range", this.weaponRange).mergeStyle(TextFormatting.DARK_GREEN));
        tooltip.add(new TranslationTextComponent("tooltip.hitscan.pp7_ammo", getAmmoCount(stack), getMaxAmmo()).mergeStyle(TextFormatting.DARK_GREEN));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack weapon = playerIn.getHeldItem(handIn);

        if (doReloadIfEmpty(worldIn, playerIn, weapon)) {
            return ActionResult.resultConsume(weapon);
        }

        // TODO upgrades
        // TODO dual wield
        EntityRayTraceResult result = RayTrace.getEntityLookingAt(playerIn, this.weaponRange);
        if (result != null) {

            if (!worldIn.isRemote) {
                // do damage
                Entity target = result.getEntity();
                if (target instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)target;

                    DamageSource damageSource = DamageSource.causePlayerDamage(playerIn);
                    playerIn.setLastAttackedEntity(livingEntity);

                    double ratioX = MathHelper.sin(playerIn.rotationYaw * ((float)Math.PI / 180F));
                    double ratioZ = -MathHelper.cos(playerIn.rotationYaw * ((float)Math.PI / 180F));

                    livingEntity.applyKnockback(0.5F, ratioX, ratioZ);
                    livingEntity.attackEntityFrom(damageSource, weaponDamage);
                    useOneAmmo(weapon);

                    NetworkDispatcher.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> livingEntity),
                        new PacketWeaponFired(playerIn, livingEntity));

                    worldIn.playSound(null, playerIn.getPosition(),
                        ModRegistry.PP7_GUNSHOT.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);

                    if (getAmmoCount(weapon) > 0) {
                        playerIn.getCooldownTracker().setCooldown(this, 20);
                    }
                }
            }

            return ActionResult.resultConsume(weapon);
        } else {
            return ActionResult.resultPass(weapon);
        }
    }

    public static boolean doReloadIfEmpty(World worldIn, PlayerEntity playerIn, ItemStack weapon) {
        if (getAmmoCount(weapon) == 0) {
            doReload(worldIn, playerIn, weapon);
            return true;
        } else {
            return false;
        }
    }

    public static void doReload(World worldIn, PlayerEntity playerIn, ItemStack weapon) {
        if (!worldIn.isRemote) {
            reloadAmmo(weapon);
            playerIn.getCooldownTracker().setCooldown(weapon.getItem(), 60);
        } else {
            playerIn.playSound(ModRegistry.PP7_RELOAD.get(), 1.0F, 1.0F);
        }
    }

    public static ItemStack getWeapon(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof ItemHitscanWeapon)) {
            heldItem = player.getHeldItemOffhand();
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
        CompoundNBT nbt = itemStack.getOrCreateTag();

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

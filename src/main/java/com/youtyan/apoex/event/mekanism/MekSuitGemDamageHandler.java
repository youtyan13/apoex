package com.youtyan.apoex.event.mekanism;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MekSuitGemDamageHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        List<Runnable> energyUsageCallbacks = new ArrayList<>(4);
        float absorbed = getDamageAbsorbed(player, event.getSource(), event.getAmount(), energyUsageCallbacks);
        if (absorbed > 0) {
            for (Runnable energyUsageCallback : energyUsageCallbacks) {
                energyUsageCallback.run();
            }
            float newDamage = event.getAmount() * (1 - absorbed);
            if (newDamage <= 0) {
                event.setCanceled(true);
            } else {
                event.setAmount(newDamage);
            }
        }
    }

    private static float getDamageAbsorbed(Player player, DamageSource source, float amount, @Nullable List<Runnable> energyUseCallbacks) {
        if (amount <= 0) {
            return 0;
        }
        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        for (ItemStack stack : player.getArmorSlots()) {
            if (MekSuitGemIntegration.isMekArmor(stack) && stack.getItem() instanceof ArmorItem armor) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null && !energyContainer.getEnergy().isZero()) {
                    FoundArmorDetails details = new FoundArmorDetails(energyContainer, armor);
                    armorDetails.add(details);
                    for (IModule<?> module : ApoExModuleHelper.getModules(stack)) {
                        if (module.isEnabled()) {
                            ICustomModule.ModuleDamageAbsorbInfo damageAbsorbInfo = getModuleDamageAbsorbInfo(module, source);
                            if (damageAbsorbInfo != null) {
                                float absorption = damageAbsorbInfo.absorptionRatio().getAsFloat();
                                ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, damageAbsorbInfo.energyCost());
                                if (ratioAbsorbed >= 1) {
                                    break;
                                }
                            }
                        }
                    }
                    if (ratioAbsorbed >= 1) {
                        break;
                    }
                }
            }
        }
        if (ratioAbsorbed < 1) {
            Float absorbRatio = null;
            for (FoundArmorDetails details : armorDetails) {
                if (absorbRatio == null) {
                    if (!source.is(MekanismTags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED) && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                        break;
                    }
                    ResourceLocation damageTypeName = source.typeHolder().unwrapKey()
                            .map(ResourceKey::location)
                            .orElseGet(() -> player.level().registryAccess().registry(Registries.DAMAGE_TYPE)
                                    .map(registry -> registry.getKey(source.type()))
                                    .orElse(null)
                            );
                    if (damageTypeName != null) {
                        absorbRatio = MekanismConfig.gear.mekaSuitDamageRatios.get().get(damageTypeName);
                    }
                    if (absorbRatio == null) {
                        absorbRatio = MekanismConfig.gear.mekaSuitUnspecifiedDamageRatio.getAsFloat();
                    }
                    if (absorbRatio == 0) {
                        break;
                    }
                }
                float absorption = details.absorption * absorbRatio;
                ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage);
                if (ratioAbsorbed >= 1) {
                    break;
                }
            }
        }
        for (FoundArmorDetails details : armorDetails) {
            if (!details.usageInfo.energyUsed.isZero()) {
                if (energyUseCallbacks == null) {
                    details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
                } else {
                    energyUseCallbacks.add(() -> details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL));
                }
            }
        }
        return Math.min(ratioAbsorbed, 1);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> ICustomModule.ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
    }

    private static float absorbDamage(EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, FloatingLongSupplier energyCost) {
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            FloatingLong usage = energyCost.get().multiply(toAbsorb);
            if (usage.isZero()) {
                return absorption;
            } else if (usageInfo.energyAvailable.greaterOrEqual(usage)) {
                usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usage);
                usageInfo.energyAvailable = usageInfo.energyAvailable.minusEqual(usage);
                return absorption;
            } else if (!usageInfo.energyAvailable.isZero()) {
                float absorbedPercent = usageInfo.energyAvailable.divide(usage).floatValue();
                usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usageInfo.energyAvailable);
                usageInfo.energyAvailable = FloatingLong.ZERO;
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    private static class FoundArmorDetails {

        private final IEnergyContainer energyContainer;
        private final EnergyUsageInfo usageInfo;
        private final float absorption;

        public FoundArmorDetails(IEnergyContainer energyContainer, ArmorItem armor) {
            this.energyContainer = energyContainer;
            this.usageInfo = new EnergyUsageInfo(energyContainer.getEnergy());
            this.absorption = getAbsorption(armor.getType());
        }

        private float getAbsorption(ArmorItem.Type armorType) {
            return switch (armorType) {
                case HELMET, BOOTS -> 0.15F;
                case CHESTPLATE -> 0.4F;
                case LEGGINGS -> 0.3F;
                default -> 0F;
            };
        }
    }

    private static class EnergyUsageInfo {

        private FloatingLong energyAvailable;
        private FloatingLong energyUsed = FloatingLong.ZERO;

        public EnergyUsageInfo(FloatingLong energyAvailable) {
            this.energyAvailable = energyAvailable.copy();
        }
    }
}
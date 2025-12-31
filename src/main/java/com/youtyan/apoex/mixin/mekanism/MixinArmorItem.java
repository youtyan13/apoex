package com.youtyan.apoex.mixin.mekanism;

import com.youtyan.apoex.integration.mekanism.MekSuitGemIntegration;
import com.youtyan.apoex.util.mekanism.ApoExModuleHelper;
import com.youtyan.apoex.util.mekanism.IMekArmorAccessor;
import mekanism.api.Action;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.chemical.item.RateLimitMultiTankGasHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler;
import mekanism.common.capabilities.fluid.item.RateLimitMultiTankFluidHandler.FluidTankSpec;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.gear.ItemHazmatSuitArmor;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.api.chemical.gas.Gas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.IntSupplier;

@Mixin(ArmorItem.class)
public abstract class MixinArmorItem extends Item implements IJetpackItem, IModeItem, IMekArmorAccessor, IItemHUDProvider {

    @Shadow public abstract ArmorItem.Type getType();

    @Unique private final List<ChemicalTankSpec<Gas>> gasTankSpecs = new ArrayList<>();
    @Unique private final List<FluidTankSpec> fluidTankSpecs = new ArrayList<>();

    public MixinArmorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
    }

    @Unique
    private static class CustomGasTankSpec extends ChemicalTankSpec<Gas> {
        public CustomGasTankSpec(LongSupplier rate, LongSupplier capacity) {
            super(rate, capacity,
                    (gas, automationType) -> true,
                    (gas, automationType, stack) -> gas == MekanismGases.HYDROGEN.get(),
                    (gas) -> gas == MekanismGases.HYDROGEN.get(),
                    (stack) -> ApoExModuleHelper.getModule(stack, MekanismModules.JETPACK_UNIT.get()) != null
            );
        }
    }

    @Unique
    private static class CustomFluidTankSpec extends FluidTankSpec {
        public CustomFluidTankSpec(IntSupplier rate, IntSupplier capacity) {
            super(rate, capacity,
                    (fluid, automationType) -> true,
                    (fluid, automationType, stack) -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid(),
                    (fluid) -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid(),
                    (stack) -> ApoExModuleHelper.getModule(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT.get()) != null
            );
        }
    }

    @Override
    public List<ChemicalTankSpec<Gas>> apoex$getGasTankSpecs() {
        if (this.getType() == ArmorItem.Type.CHESTPLATE) {
            return List.of(new CustomGasTankSpec(
                    MekanismConfig.gear.mekaSuitJetpackTransferRate,
                    MekanismConfig.gear.mekaSuitJetpackMaxStorage
            ));
        }
        return Collections.emptyList();
    }

    @Override
    public List<FluidTankSpec> apoex$getFluidTankSpecs() {
        if (this.getType() == ArmorItem.Type.HELMET) {
            return List.of(new CustomFluidTankSpec(
                    MekanismConfig.gear.mekaSuitNutritionalTransferRate,
                    MekanismConfig.gear.mekaSuitNutritionalMaxStorage
            ));
        }
        return Collections.emptyList();
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (MekSuitGemIntegration.isMekArmor(stack)) {
            gasTankSpecs.clear();
            fluidTankSpecs.clear();
            List<ItemCapability> capabilities = new ArrayList<>();
            capabilities.add(RateLimitEnergyHandler.create(() -> getChargeRate(stack), () -> getMaxEnergy(stack), BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
            List<ChemicalTankSpec<Gas>> gasSpecs = apoex$getGasTankSpecs();
            if (!gasSpecs.isEmpty()) capabilities.add(RateLimitMultiTankGasHandler.create(gasSpecs));
            List<FluidTankSpec> fluidSpecs = apoex$getFluidTankSpecs();
            if (!fluidSpecs.isEmpty()) capabilities.add(RateLimitMultiTankFluidHandler.create(fluidSpecs));
            capabilities.add(RadiationShieldingHandler.create(item -> isModuleEnabled(item, MekanismModules.RADIATION_SHIELDING_UNIT.get()) ?
                  ItemHazmatSuitArmor.getShieldingByArmor(getType()) : 0));
            capabilities.add(LaserDissipationHandler.create(item -> isModuleEnabled(item, MekanismModules.LASER_DISSIPATION_UNIT.get()) ? 0.15 : 0,
                    item -> isModuleEnabled(item, MekanismModules.LASER_DISSIPATION_UNIT.get()) ? 0.85 : 0));
            if (!capabilities.isEmpty()) return new ItemCapabilityWrapper(stack, capabilities.toArray(new ItemCapability[0]));
        }
        return super.initCapabilities(stack, nbt);
    }

    @Unique private FloatingLong getMaxEnergy(ItemStack stack) { IModule<ModuleEnergyUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.ENERGY_UNIT.get()); return module == null ? MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module); }
    @Unique private FloatingLong getChargeRate(ItemStack stack) { IModule<ModuleEnergyUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.ENERGY_UNIT.get()); return module == null ? MekanismConfig.gear.mekaSuitBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module); }

    @Override public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) { if (MekSuitGemIntegration.isMekArmor(stack)) return 0; return super.damageItem(stack, amount, entity, onBroken); }
    @Override public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) { if (MekSuitGemIntegration.isMekArmor(stack)) return true; return super.makesPiglinsNeutral(stack, wearer); }
    @Override public boolean isEnderMask(@NotNull ItemStack stack, @NotNull Player player, @NotNull EnderMan enderman) { if (MekSuitGemIntegration.isMekArmor(stack) && this.getType() == ArmorItem.Type.HELMET) return true; return super.isEnderMask(stack, player, enderman); }
    @Override public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) { if (MekSuitGemIntegration.isMekArmor(stack) && this.getType() == ArmorItem.Type.BOOTS) return true; return super.canWalkOnPowderedSnow(stack, wearer); }
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isEquipped) {
        super.inventoryTick(stack, level, entity, slot, isEquipped);
        if (!MekSuitGemIntegration.isMekArmor(stack)) return;
        if (entity instanceof Player player) {
            if (player.getItemBySlot(this.getType().getSlot()) == stack) {
                for (IModule<?> module : ApoExModuleHelper.getModules(stack)) {
                    if (module instanceof Module) ((Module<?>) module).tick(player);
                }
            }
        }
    }
    @Override public boolean canUseJetpack(ItemStack stack) { if (!MekSuitGemIntegration.isMekArmor(stack) || this.getType() != ArmorItem.Type.CHESTPLATE) return false; IModule<ModuleJetpackUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.JETPACK_UNIT.get()); if (module != null && module.isEnabled()) return ChemicalUtil.hasChemical(stack, MekanismGases.HYDROGEN.get()); return ApoExModuleHelper.getModules(stack).stream().anyMatch(m -> m.isEnabled() && m.getData().isExclusive(ModuleData.ExclusiveFlag.OVERRIDE_JUMP.getMask())); }
    @Override public JetpackMode getJetpackMode(ItemStack stack) { if (MekSuitGemIntegration.isMekArmor(stack)) { IModule<ModuleJetpackUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.JETPACK_UNIT.get()); if (module != null && module.isEnabled()) return module.getCustomInstance().getMode(); } return JetpackMode.DISABLED; }
    @Override public void useJetpackFuel(ItemStack stack) { if (MekSuitGemIntegration.isMekArmor(stack)) { stack.getCapability(Capabilities.GAS_HANDLER).ifPresent(handler -> { if (handler.getTanks() > 0) handler.extractChemical(1, Action.EXECUTE); }); } }
    @Override public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) { if (MekSuitGemIntegration.isMekArmor(stack)) { for (IModule<?> module : ApoExModuleHelper.getModules(stack)) { if (module instanceof Module && ((Module<?>) module).handlesModeChange()) { ((Module<?>) module).changeMode(player, stack, shift, displayChange); return; } } } }
    @Override public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) { if (MekSuitGemIntegration.isMekArmor(stack) && stack.getItem() instanceof ArmorItem armorItem && slotType == armorItem.getType().getSlot()) { for (IModule<?> module : ApoExModuleHelper.getModules(stack)) { if (module.handlesModeChange()) return true; } } return false; }
    @Override public @NotNull Component getScrollTextComponent(@NotNull ItemStack stack) { if (MekSuitGemIntegration.isMekArmor(stack)) { for (IModule<?> module : ApoExModuleHelper.getModules(stack)) { if (module instanceof Module && ((Module<?>) module).handlesModeChange()) return ((Module<?>) module).getModeScrollComponent(stack); } } return IModeItem.super.getScrollTextComponent(stack); }
    @Unique private boolean isHydrogenTankEmpty(ItemStack stack) { return stack.getCapability(Capabilities.GAS_HANDLER).map(handler -> { if (handler.getTanks() > 0) return handler.getChemicalInTank(0).isEmpty(); return true; }).orElse(true); }
    @Override public boolean canElytraFly(ItemStack stack, LivingEntity entity) { if (MekSuitGemIntegration.isMekArmor(stack) && this.getType() == ArmorItem.Type.CHESTPLATE && !entity.isShiftKeyDown()) { IModule<ModuleElytraUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.ELYTRA_UNIT.get()); if (module != null && module.isEnabled() && module.canUseEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get())) { IModule<ModuleJetpackUnit> jetpack = ApoExModuleHelper.getModule(stack, MekanismModules.JETPACK_UNIT.get()); if (jetpack == null || !jetpack.isEnabled() || jetpack.getCustomInstance().getMode() != JetpackMode.HOVER || isHydrogenTankEmpty(stack)) { return true; } } } return super.canElytraFly(stack, entity); }
    @Override public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) { if (MekSuitGemIntegration.isMekArmor(stack) && !entity.level().isClientSide) { int nextFlightTicks = flightTicks + 1; if (nextFlightTicks % 10 == 0) { if (nextFlightTicks % 20 == 0) { IModule<ModuleElytraUnit> module = ApoExModuleHelper.getModule(stack, MekanismModules.ELYTRA_UNIT.get()); if (module != null && module.isEnabled()) module.useEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get()); } entity.gameEvent(GameEvent.ELYTRA_GLIDE); } return true; } return super.elytraFlightTick(stack, entity, flightTicks); }

    @Unique
    private boolean isModuleEnabled(ItemStack stack, ModuleData<?> type) {
        IModule<?> module = ApoExModuleHelper.getModule(stack, type);
        return module != null && module.isEnabled();
    }
}
package enchantingreimagined.resourceconditions;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import enchantingreimagined.EnchantingReimagined;
import enchantingreimagined.EnchantingReimaginedConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;

public record EnchantmentUpgradingResourceCondition() implements ResourceCondition {

    public static final MapCodec<EnchantmentUpgradingResourceCondition> CODEC = MapCodec
            .unit(EnchantmentUpgradingResourceCondition::new);

    @Override
    public ResourceConditionType<?> getType() {
        return EnchantingReimagined.ENCHANTMENT_UPGRADING_RESOURCE_CONDITION;
    }

    @Override
    public boolean test(@Nullable RegistryInfoGetter registryInfo) {
        return AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
                .getConfig().upgrading.allowUpgrading;
    }

}

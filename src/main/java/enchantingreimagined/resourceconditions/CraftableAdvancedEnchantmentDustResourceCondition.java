package enchantingreimagined.resourceconditions;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import enchantingreimagined.EnchantingReimagined;
import enchantingreimagined.EnchantingReimaginedConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;

public record CraftableAdvancedEnchantmentDustResourceCondition() implements ResourceCondition {

    public static final MapCodec<CraftableAdvancedEnchantmentDustResourceCondition> CODEC = MapCodec
            .unit(CraftableAdvancedEnchantmentDustResourceCondition::new);

    @Override
    public ResourceConditionType<?> getType() {
        return EnchantingReimagined.CRAFTABLE_ADVANCED_ENCHANTMENT_DUST_RESOURCE_CONDITION;
    }

    @Override
    public boolean test(@Nullable RegistryInfoGetter registryInfo) {
        return AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
                .getConfig().craftableAdvancedEnchantmentDust;
    }

}

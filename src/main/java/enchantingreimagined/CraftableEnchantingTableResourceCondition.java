package enchantingreimagined;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryOps.RegistryInfoGetter;

public record CraftableEnchantingTableResourceCondition() implements ResourceCondition {

    public static final MapCodec<CraftableEnchantingTableResourceCondition> CODEC = MapCodec
            .unit(CraftableEnchantingTableResourceCondition::new);

    @Override
    public ResourceConditionType<?> getType() {
        return EnchantingReimagined.CRAFTABLE_ENCHANTING_TABLE_RESOURCE_CONDITION;
    }

    @Override
    public boolean test(@Nullable RegistryInfoGetter registryInfo) {
        return !AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
                .getConfig().removeEnchantingTableRecipe;
    }

}

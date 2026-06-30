package enchantingreimagined;

import java.util.Arrays;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import enchantingreimagined.resourceconditions.CraftableAdvancedEnchantmentDustResourceCondition;
import enchantingreimagined.resourceconditions.CraftableEnchantingTableResourceCondition;
import enchantingreimagined.resourceconditions.CurseScrubbingResourceCondition;
import enchantingreimagined.resourceconditions.EnchantmentUpgradingResourceCondition;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class EnchantingReimagined implements ModInitializer {
    public static TooltippedItem ENCHANTMENT_DUST;
    public static TooltippedItem ADVANCED_ENCHANTMENT_DUST;
    public static TooltippedItem CURSER_SCRUBBER;

    public static EnchantingWorkstationBlock ENCHANTING_WORKSTATION;

    public static MenuType<EnchantingWorkstationGui> ENCHANTING_WORKSTATION_SCREEN_TYPE;

    public static CreativeModeTab ENCHANTING_REIMAGINED_GROUP;

    public static final Identifier INTERACT_WITH_ENCHANTING_WORKSTATION = getId(
            "interact_with_enchanting_workstation");
    public static final Identifier CRAFT_IN_ENCHANTING_WORKSTATION = getId(
            "craft_in_enchanting_workstation");

    public static final ResourceConditionType<CraftableEnchantingTableResourceCondition> CRAFTABLE_ENCHANTING_TABLE_RESOURCE_CONDITION = ResourceConditionType
            .create(getId("craftable_enchanting_table"), CraftableEnchantingTableResourceCondition.CODEC);
    public static final ResourceConditionType<CraftableAdvancedEnchantmentDustResourceCondition> CRAFTABLE_ADVANCED_ENCHANTMENT_DUST_RESOURCE_CONDITION = ResourceConditionType
            .create(getId("craftable_advanced_enchantment_dust"),
                    CraftableAdvancedEnchantmentDustResourceCondition.CODEC);
    public static final ResourceConditionType<CurseScrubbingResourceCondition> CURSE_SCRUBBING_RESOURCE_CONDITION = ResourceConditionType
            .create(getId("curse_scrubbing_enabled"), CurseScrubbingResourceCondition.CODEC);
    public static final ResourceConditionType<EnchantmentUpgradingResourceCondition> ENCHANTMENT_UPGRADING_RESOURCE_CONDITION = ResourceConditionType
            .create(getId("enchantment_upgrading_enabled"), EnchantmentUpgradingResourceCondition.CODEC);

    public static Identifier getId(String id) {
        return Identifier.fromNamespaceAndPath("enchanting_reimagined", id);
    }

    @Override
    public void onInitialize() {
        registerConfig();
        registerItems();
        registerBlocks();
        registerGuis();
        registerItemGroup();
        registerStats();
        registerResourceConditions();
    }

    private void registerConfig() {
        AutoConfig.register(EnchantingReimaginedConfig.class, JanksonConfigSerializer::new);
    }

    private void registerItems() {
        ENCHANTMENT_DUST = Registry.register(BuiltInRegistries.ITEM, getId("enchantment_dust"),
                new TooltippedItem(
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, getId("enchantment_dust"))),
                        Arrays.asList("item.enchanting_reimagined.enchantment_dust.tooltip")));
        ADVANCED_ENCHANTMENT_DUST = Registry.register(BuiltInRegistries.ITEM, getId("advanced_enchantment_dust"),
                new TooltippedItem(
                        new Item.Properties()
                                .setId(ResourceKey.create(Registries.ITEM, getId("advanced_enchantment_dust"))),
                        Arrays.asList("item.enchanting_reimagined.advanced_enchantment_dust.tooltip",
                                "item.enchanting_reimagined.advanced_enchantment_dust.tooltip.2")));
        CURSER_SCRUBBER = Registry.register(BuiltInRegistries.ITEM, getId("curse_scrubber"),
                new TooltippedItem(
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, getId("curse_scrubber"))),
                        Arrays.asList("item.enchanting_reimagined.curse_scrubber.tooltip")));
    }

    private void registerBlocks() {
        ENCHANTING_WORKSTATION = new EnchantingWorkstationBlock(
                Block.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, getId("enchanting_workstation")))
                        .mapColor(MapColor.DEEPSLATE)
                        .instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
        registerBlock(ENCHANTING_WORKSTATION, "enchanting_workstation");
    }

    private void registerBlock(Block block, String name) {
        Registry.register(BuiltInRegistries.BLOCK, getId(name), block);
        Registry.register(BuiltInRegistries.ITEM, getId(name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, getId(name)))));
    }

    private void registerGuis() {
        ENCHANTING_WORKSTATION_SCREEN_TYPE = Registry.register(BuiltInRegistries.MENU,
                getId("enchanting_workstation"),
                new MenuType<>(
                        (syncId, inventory) -> new EnchantingWorkstationGui(syncId, inventory,
                                ContainerLevelAccess.NULL),
                        FeatureFlags.VANILLA_SET));
    }

    private void registerItemGroup() {
        ENCHANTING_REIMAGINED_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                getId("enchanting_reimagined"),
                FabricCreativeModeTab.builder()
                        .icon(() -> new ItemStack(ENCHANTING_WORKSTATION))
                        .title(Component.translatable("itemGroup.enchating_reimagined.enchanting_reimagined"))
                        .displayItems((context, entries) -> {
                            entries.accept(new ItemStack(ENCHANTING_WORKSTATION));
                            entries.accept(new ItemStack(ENCHANTMENT_DUST));
                            entries.accept(new ItemStack(ADVANCED_ENCHANTMENT_DUST));
                            entries.accept(new ItemStack(CURSER_SCRUBBER));
                        })
                        .build());
    }

    private void registerStats() {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, INTERACT_WITH_ENCHANTING_WORKSTATION,
                INTERACT_WITH_ENCHANTING_WORKSTATION);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, CRAFT_IN_ENCHANTING_WORKSTATION,
                CRAFT_IN_ENCHANTING_WORKSTATION);
        Stats.CUSTOM.get(INTERACT_WITH_ENCHANTING_WORKSTATION, StatFormatter.DEFAULT);
        Stats.CUSTOM.get(CRAFT_IN_ENCHANTING_WORKSTATION, StatFormatter.DEFAULT);
    }

    private void registerResourceConditions() {
        ResourceConditions.register(CRAFTABLE_ENCHANTING_TABLE_RESOURCE_CONDITION);
        ResourceConditions.register(CRAFTABLE_ADVANCED_ENCHANTMENT_DUST_RESOURCE_CONDITION);
        ResourceConditions.register(CURSE_SCRUBBING_RESOURCE_CONDITION);
        ResourceConditions.register(ENCHANTMENT_UPGRADING_RESOURCE_CONDITION);
    }
}
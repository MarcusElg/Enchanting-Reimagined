package enchantingreimagined;

import java.util.Arrays;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantingReimagined implements ModInitializer {
    public static TooltippedItem ENCHANTMENT_DUST;
    public static TooltippedItem ADVANCED_ENCHANTMENT_DUST;
    public static TooltippedItem CURSER_SCRUBBER;

    public static EnchantingWorkstationBlock ENCHANTING_WORKSTATION;

    public static ScreenHandlerType<EnchantingWorkstationGui> ENCHANTING_WORKSTATION_SCREEN_TYPE;

    public static ItemGroup ENCHANTING_REIMAGINED_GROUP;

    public static final Identifier INTERACT_WITH_ENCHANTING_WORKSTATION = getId(
            "interact_with_enchanting_workstation");
    public static final Identifier CRAFT_IN_ENCHANTING_WORKSTATION = getId(
            "craft_in_enchanting_workstation");

    public static final ResourceConditionType<CraftableEnchantingTableResourceCondition> CRAFTABLE_ENCHANTING_TABLE_RESOURCE_CONDITION = ResourceConditionType
            .create(getId("craftable_enchanting_table"), CraftableEnchantingTableResourceCondition.CODEC);

    public static Identifier getId(String id) {
        return Identifier.of("enchanting_reimagined", id);
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
        ENCHANTMENT_DUST = Registry.register(Registries.ITEM, getId("enchantment_dust"),
                new TooltippedItem(
                        new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, getId("enchantment_dust"))),
                        Arrays.asList("item.enchanting_reimagined.enchantment_dust.tooltip")));
        ADVANCED_ENCHANTMENT_DUST = Registry.register(Registries.ITEM, getId("advanced_enchantment_dust"),
                new TooltippedItem(
                        new Item.Settings()
                                .registryKey(RegistryKey.of(RegistryKeys.ITEM, getId("advanced_enchantment_dust"))),
                        Arrays.asList("item.enchanting_reimagined.advanced_enchantment_dust.tooltip",
                                "item.enchanting_reimagined.advanced_enchantment_dust.tooltip.2")));
        CURSER_SCRUBBER = Registry.register(Registries.ITEM, getId("curse_scrubber"),
                new TooltippedItem(
                        new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, getId("curse_scrubber"))),
                        Arrays.asList("item.enchanting_reimagined.curse_scrubber.tooltip")));
    }

    private void registerBlocks() {
        ENCHANTING_WORKSTATION = new EnchantingWorkstationBlock(
                Block.Settings.create()
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, getId("enchanting_workstation")))
                        .mapColor(MapColor.DEEPSLATE_GRAY)
                        .instrument(NoteBlockInstrument.BASS).strength(2.5F).sounds(BlockSoundGroup.WOOD).burnable());
        registerBlock(ENCHANTING_WORKSTATION, "enchanting_workstation");
    }

    private void registerBlock(Block block, String name) {
        Registry.register(Registries.BLOCK, getId(name), block);
        Registry.register(Registries.ITEM, getId(name),
                new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, getId(name)))));
    }

    private void registerGuis() {
        ENCHANTING_WORKSTATION_SCREEN_TYPE = Registry.register(Registries.SCREEN_HANDLER,
                getId("enchanting_workstation"),
                new ScreenHandlerType<>(
                        (syncId, inventory) -> new EnchantingWorkstationGui(syncId, inventory,
                                ScreenHandlerContext.EMPTY),
                        FeatureFlags.VANILLA_FEATURES));
    }

    private void registerItemGroup() {
        ENCHANTING_REIMAGINED_GROUP = Registry.register(Registries.ITEM_GROUP, getId("enchanting_reimagined"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ENCHANTING_WORKSTATION))
                        .displayName(Text.translatable("itemGroup.enchating_reimagined.enchanting_reimagined"))
                        .entries((context, entries) -> {
                            entries.add(new ItemStack(ENCHANTING_WORKSTATION));
                            entries.add(new ItemStack(ENCHANTMENT_DUST));
                            entries.add(new ItemStack(ADVANCED_ENCHANTMENT_DUST));
                            entries.add(new ItemStack(CURSER_SCRUBBER));
                        })
                        .build());
    }

    private void registerStats() {
        Registry.register(Registries.CUSTOM_STAT, INTERACT_WITH_ENCHANTING_WORKSTATION,
                INTERACT_WITH_ENCHANTING_WORKSTATION);
        Registry.register(Registries.CUSTOM_STAT, CRAFT_IN_ENCHANTING_WORKSTATION,
                CRAFT_IN_ENCHANTING_WORKSTATION);
        Stats.CUSTOM.getOrCreateStat(INTERACT_WITH_ENCHANTING_WORKSTATION, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(CRAFT_IN_ENCHANTING_WORKSTATION, StatFormatter.DEFAULT);
    }

    private void registerResourceConditions() {
        ResourceConditions.register(CRAFTABLE_ENCHANTING_TABLE_RESOURCE_CONDITION);
    }
}
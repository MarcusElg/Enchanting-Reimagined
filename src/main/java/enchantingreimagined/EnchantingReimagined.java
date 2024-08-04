package enchantingreimagined;

import java.util.ArrayList;
import java.util.Arrays;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
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

	public static final Identifier INTERACT_WITH_ENCHANTING_WORKSTATION = get_id(
			"interact_with_enchanting_workstation");
	public static final Identifier CRAFT_IN_ENCHANTING_WORKSTATION = get_id(
			"craft_in_enchanting_workstation");

	public static Identifier get_id(String id) {
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
		removeRecipes();
	}

	private void registerConfig() {
		AutoConfig.register(EnchantingReimaginedConfig.class, JanksonConfigSerializer::new);
	}

	private void registerItems() {
		ENCHANTMENT_DUST = Registry.register(Registries.ITEM, get_id("enchantment_dust"),
				new TooltippedItem(new Item.Settings(),
						Arrays.asList("item.enchanting_reimagined.enchantment_dust.tooltip")));
		ADVANCED_ENCHANTMENT_DUST = Registry.register(Registries.ITEM, get_id("advanced_enchantment_dust"),
				new TooltippedItem(new Item.Settings(),
						Arrays.asList("item.enchanting_reimagined.advanced_enchantment_dust.tooltip",
								"item.enchanting_reimagined.advanced_enchantment_dust.tooltip.2")));
		CURSER_SCRUBBER = Registry.register(Registries.ITEM, get_id("curse_scrubber"),
				new TooltippedItem(new Item.Settings(),
						Arrays.asList("item.enchanting_reimagined.curse_scrubber.tooltip")));
	}

	private void registerBlocks() {
		ENCHANTING_WORKSTATION = new EnchantingWorkstationBlock(Block.Settings.create());
		registerBlock(ENCHANTING_WORKSTATION, "enchanting_workstation");
	}

	private void registerBlock(Block block, String name) {
		Registry.register(Registries.BLOCK, get_id(name), block);
		Registry.register(Registries.ITEM, get_id(name), new BlockItem(block, new Item.Settings()));
	}

	private void registerGuis() {
		ENCHANTING_WORKSTATION_SCREEN_TYPE = Registry.register(Registries.SCREEN_HANDLER,
				get_id("enchanting_workstation"),
				new ScreenHandlerType<>(
						(syncId, inventory) -> new EnchantingWorkstationGui(syncId, inventory,
								ScreenHandlerContext.EMPTY),
						FeatureFlags.VANILLA_FEATURES));
	}

	private void registerItemGroup() {
		ENCHANTING_REIMAGINED_GROUP = Registry.register(Registries.ITEM_GROUP, get_id("enchanting_reimagined"),
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

	private void removeRecipes() {
		EnchantingReimaginedConfig config = AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
				.getConfig();
		if (!config.removeEnchantingTableRecipe) {
			return;
		}

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			final RecipeManager recipeManager = server.getRecipeManager();
			ArrayList<RecipeEntry<?>> newRecipes = new ArrayList<RecipeEntry<?>>();

			for (RecipeEntry<?> recipe : recipeManager.values()) {
				// Remove recipe by not adding it to the new list
				if (!recipe.id().equals(Identifier.ofVanilla("enchanting_table"))) {
					newRecipes.add(recipe);
				}
			}

			recipeManager.setRecipes(newRecipes);
		});
	}
}
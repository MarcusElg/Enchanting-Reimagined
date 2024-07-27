package enchantingreimagined;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class EnchantingReimagined implements ModInitializer {
	public static TooltippedItem ENCHANTMENT_DUST;
	public static TooltippedItem CURSER_SCRUBBER;

	public static Identifier get_id(String id) {
		return Identifier.of("enchanting_reimagined", id);
	}

	@Override
	public void onInitialize() {
		registerItems();
		registerBlocks();
		registerItemGroup();
	}

	private void registerItems() {
		ENCHANTMENT_DUST = Registry.register(Registries.ITEM, get_id("enchantment_dust"),
				new TooltippedItem(new Item.Settings(), "item.enchanting_reimagined.enchantment_dust.tooltip"));
		CURSER_SCRUBBER = Registry.register(Registries.ITEM, get_id("curse_scrubber"),
				new TooltippedItem(new Item.Settings(), "item.enchanting_reimagined.curse_scrubber.tooltip"));
	}

	private void registerBlocks() {

	}

	private void registerItemGroup() {
		Registry.register(Registries.ITEM_GROUP, get_id("enchanting_reimagined"), FabricItemGroup.builder()
				.icon(() -> new ItemStack(ENCHANTMENT_DUST))
				.displayName(Text.translatable("itemGroup.enchating_reimagined.enchanting_reimagined"))
				.entries((context, entries) -> {
					entries.add(new ItemStack(ENCHANTMENT_DUST));
					entries.add(new ItemStack(CURSER_SCRUBBER));
				})
				.build());
	}
}
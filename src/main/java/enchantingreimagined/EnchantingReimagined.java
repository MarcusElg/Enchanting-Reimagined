package enchantingreimagined;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class EnchantingReimagined implements ModInitializer {
	public static Item ENCHANTMENT_DUST;
	public static Item CURSER_SCRUBBER;

	public static Identifier get_id(String id) {
		return Identifier.of("enchanting_reimagined", id);
	}

	@Override
	public void onInitialize() {
		ENCHANTMENT_DUST = Registry.register(Registries.ITEM, get_id("enchantment_dust"),
				new Item(new Item.Settings()));
		CURSER_SCRUBBER = Registry.register(Registries.ITEM, get_id("curse_scrubber"),
				new Item(new Item.Settings().app));
	}
}
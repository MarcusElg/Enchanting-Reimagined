package enchantingreimagined;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class TooltippedItem extends Item {

    String tooltip_key;

    public TooltippedItem(Settings settings, String tooltip_key) {
        super(settings);
        this.tooltip_key = tooltip_key;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(tooltip_key));
	}
}
package enchantingreimagined;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public class TooltippedItem extends Item {

    List<String> tooltip_keys;

    public TooltippedItem(Settings settings, List<String> tooltip_keys) {
        super(settings);
        this.tooltip_keys = tooltip_keys;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer, TooltipType type) {
        for (String tooltip_key : tooltip_keys) {
            textConsumer.accept(Text.translatable(tooltip_key));
        }
    }
}
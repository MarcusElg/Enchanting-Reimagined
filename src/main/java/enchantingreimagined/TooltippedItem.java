package enchantingreimagined;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class TooltippedItem extends Item {

    List<String> tooltip_keys;

    public TooltippedItem(Properties settings, List<String> tooltip_keys) {
        super(settings);
        this.tooltip_keys = tooltip_keys;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent,
            Consumer<Component> textConsumer, TooltipFlag type) {
        for (String tooltip_key : tooltip_keys) {
            textConsumer.accept(Component.translatable(tooltip_key));
        }
    }
}
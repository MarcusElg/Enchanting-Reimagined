package enchantingreimagined.gui;

import java.util.function.Predicate;

import enchantingreimagined.EnchantingReimagined;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class GuiHelper {
    public static final int DISABLED_COLOUR = 8553090;
    public static final int NO_TINT = 0xFF_FFFFFF;
    public static final int EXPERIENCE_COLOR = 8453920;
    public static final int ERROR_COLOR = 16736352;

    public static final int LABEL_HEIGHT = 8;
    public static final int INVENTORY_Y_OFFSET = 64; // Distance from top of gui to start of player inventory

    public static Predicate<ItemStack> NO_ITEMS = (ItemStack stack) -> {
        return false;
    };

    // Textures
    public static final Identifier ARROW_TEXTURE = EnchantingReimagined
            .get_id("textures/gui/arrow_empty.png");
    public static final Identifier ERROR_TEXTURE = EnchantingReimagined
            .get_id("textures/gui/error.png");
    public static final Identifier PLUS_TEXTURE = EnchantingReimagined
            .get_id("textures/gui/plus.png");
}

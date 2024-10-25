package enchantingreimagined.gui;

import enchantingreimagined.EnchantingReimagined;
import net.minecraft.util.Identifier;

public class GuiHelper {
        public static final int EXPERIENCE_COLOR = 8453920;
        public static final int ERROR_COLOR = 16736352;

        public static final int LABEL_HEIGHT = 8;
        public static final int INVENTORY_Y_OFFSET = 64; // Distance from top of gui to start of player inventory

        // Textures
        public static final Identifier ARROW_TEXTURE = EnchantingReimagined
                        .getId("textures/gui/arrow_empty.png");
        public static final Identifier ERROR_TEXTURE = EnchantingReimagined
                        .getId("textures/gui/error.png");
        public static final Identifier PLUS_TEXTURE = EnchantingReimagined
                        .getId("textures/gui/plus.png");
}

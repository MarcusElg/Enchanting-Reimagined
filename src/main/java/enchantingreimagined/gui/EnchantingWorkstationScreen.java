package enchantingreimagined.gui;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class EnchantingWorkstationScreen extends CottonInventoryScreen<EnchantingWorkstationGui> {
    public EnchantingWorkstationScreen(EnchantingWorkstationGui gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}

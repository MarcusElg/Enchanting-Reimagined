package enchantingreimagined;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class EnchantingWorkstationScreen extends CottonInventoryScreen<EnchantingWorkstationGui> {
    public EnchantingWorkstationScreen(EnchantingWorkstationGui gui, Player player, Component title) {
        super(gui, player, title);
    }
}

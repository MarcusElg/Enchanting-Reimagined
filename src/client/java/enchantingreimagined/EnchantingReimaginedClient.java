package enchantingreimagined;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantingReimaginedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.<EnchantingWorkstationGui, EnchantingWorkstationScreen>register(
                EnchantingReimagined.ENCHANTING_WORKSTATION_SCREEN_TYPE,
                (gui, inventory, title) -> new EnchantingWorkstationScreen(gui, inventory.player, title));
    }
}
package enchantingreimagined.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import enchantingreimagined.EnchantingReimaginedConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

@Mixin(ServerRecipeManager.class)
public class ServerRecipeManagerMixin {
    @Inject(method = "collectServerRecipes", at = @At("RETURN"), cancellable = true)
    private static void collectServerRecipes(
            Iterable<ServerRecipeManager.ServerRecipe> recipes,
            FeatureSet featureSet,
            CallbackInfoReturnable<List<ServerRecipeManager.ServerRecipe>> callbackReturnable) {
        List<ServerRecipeManager.ServerRecipe> original = callbackReturnable.getReturnValue();

        // Remove enchanting table recipe
        EnchantingReimaginedConfig config = AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
                .getConfig();
        if (config.removeEnchantingTableRecipe) {
            original.removeIf(
                    recipe -> recipe.parent().id().getValue().equals(Identifier.ofVanilla("enchanting_table")));
        }
        callbackReturnable.setReturnValue(original);
    }
}

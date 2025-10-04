package enchantingreimagined;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "enchanting_reimagined")
public class EnchantingReimaginedConfig implements ConfigData {
    // If true removes enchanting table recipe
    public boolean removeEnchantingTableRecipe = false;
    public boolean craftableAdvancedEnchantmentDust = true;

    @ConfigEntry.Gui.CollapsibleObject
    public RepairConfig repairing = new RepairConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public ExtractionConfig extraction = new ExtractionConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public ScrubbingConfig scrubbing = new ScrubbingConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public ApplyingConfig applying = new ApplyingConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public CombiningConfig combining = new CombiningConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public SplittingConfig splitting = new SplittingConfig();
    @ConfigEntry.Gui.CollapsibleObject
    public UpgradingConfig upgrading = new UpgradingConfig();

    public static class RepairConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to repair in an enchanting workstation
        public boolean allowRepair = true;
        @ConfigEntry.Gui.Tooltip
        // If true repairing always costs the same
        public boolean consistentRepairPrice = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        // Sets repair price
        public int repairPrice = 2;
    }

    public static class ExtractionConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to extract enchantments from enchanted
        // items in an enchanting workstation
        public boolean allowExtraction = true;
        @ConfigEntry.Gui.Tooltip
        // If true the item is consumed
        public boolean consumeItem = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 3)
        // Cost for extracting enchantments from book
        public int extractionCost = 1;
        @ConfigEntry.Gui.Tooltip
        // If true extraction costs are calculated per level instead of per enchantment
        public boolean extractionCostPerLevel = true;
    }

    public static class ScrubbingConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to scrub away cuses from enchanted
        // items and books in an enchanting workstation
        public boolean allowScrubbing = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        // Cost for scrubbing curse from book
        public int scrubbingCost = 5;
    }

    public static class ApplyingConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to apply enchanted book to items in an
        // enchanting workstation
        public boolean allowApplying = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 3)
        // Cost for applying enchantments to an item
        public int applyingCost = 1;
        @ConfigEntry.Gui.Tooltip
        // If true applying costs are calculated per level instead of per enchantment
        public boolean applyingCostPerLevel = true;
    }

    public static class CombiningConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to combine enchanted books in an enchanting
        // workstation
        public boolean allowCombining = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        // Cost for applying enchantments to an item
        public int combiningCost = 0;
        @ConfigEntry.Gui.Tooltip
        // If true allows for invalid combinations such as fortune and silk touch
        public boolean allowInvalidCombinations = false;
    }

    public static class SplittingConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to split enchanted books in an enchanting
        // workstation
        public boolean allowSplitting = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        // Cost for splitting an enchanted book
        public int splittingCost = 0;
    }

    public static class UpgradingConfig {
        @ConfigEntry.Gui.Tooltip
        // If false disables the possibility to upgrade enchanted books in an enchanting
        // workstation
        public boolean allowUpgrading = true;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 3)
        // Cost for upgrading an enchanted book one level
        public int upgradingCost = 1;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 3)
        // Amount of extra levels that advanced enchanting dust can upgrade
        public int advancedDustExtraLevelCount = 1;
    }
}

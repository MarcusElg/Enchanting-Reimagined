package enchantingreimagined.gui;

import java.util.Set;

import enchantingreimagined.EnchantingReimagined;
import enchantingreimagined.EnchantingReimaginedConfig;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantingWorkstationGui extends SyncedGuiDescription {
    private static final int INPUT1_ID = 0;
    private static final int INPUT2_ID = 1;
    private static final int OUTPUT1_ID = 2;
    private static final int OUTPUT2_ID = 3;
    private static final int INPUT_SLOT_IDS[] = { INPUT1_ID, INPUT2_ID };
    private static final int OUTPUT_SLOT_IDS[] = { OUTPUT1_ID, OUTPUT2_ID };
    private static final int INVENTORY_SIZE = 4;

    private enum State {
        None, Crafted, NoItem, IncorrectSecondItem, NotEnoughXp, HasCurse, AlreadyMax, HasNoCurse, OnlyOneEnchant,
        NoNewEnchantments, NotRepairable
    };

    private static boolean hasCrafted = false;
    private static int xpCost = 0;
    private static int secondInputUsageCount = 0;
    private static int outputSlotsUsed = 0;

    private final ContainerLevelAccess context;
    private static WGridPanel gridPanel;
    private static WSprite errorSprite;
    private static String errorTooltip;
    private static WText experienceText;
    private static WItemSlot outputSlot1;
    private static WItemSlot outputSlot2;

    public EnchantingWorkstationGui(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(EnchantingReimagined.ENCHANTING_WORKSTATION_SCREEN_TYPE, syncId, playerInventory,
                getBlockInventory(context, INVENTORY_SIZE),
                getBlockPropertyDelegate(context));

        this.context = context;
        resetValues();
        hasCrafted = false;

        WPlainPanel root = new WPlainPanel();
        root.setInsets(Insets.ROOT_PANEL);
        setRootPanel(root);

        gridPanel = new WGridPanel();
        setTitleAlignment(HorizontalAlignment.CENTER);

        WItemSlot input1 = WItemSlot.of(blockInventory, INPUT1_ID);
        gridPanel.add(input1, 1, 1);

        WSprite plus = new WSprite(GuiHelper.PLUS_TEXTURE);
        gridPanel.add(plus, 2, 1);

        WItemSlot input2 = WItemSlot.of(blockInventory, INPUT2_ID);
        gridPanel.add(input2, 3, 1);

        WSprite arrowBackground = new WSprite(GuiHelper.ARROW_TEXTURE);
        gridPanel.add(arrowBackground, 4, 1, 2, 1);

        errorSprite = new WSprite(GuiHelper.ERROR_TEXTURE) {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                super.addTooltip(tooltip);

                if (errorTooltip != null) {
                    tooltip.add(Component.translatable(errorTooltip));
                }
            }
        };

        outputSlot1 = WItemSlot.of(blockInventory, OUTPUT1_ID).setInsertingAllowed(false)
                .setOutputFilter(stack -> allowTakingOutput());
        gridPanel.add(outputSlot1, 6, 1);

        outputSlot2 = new WItemSlot(blockInventory, OUTPUT2_ID, 1, 1, false).setInsertingAllowed(false)
                .setOutputFilter(stack -> allowTakingOutput());
        gridPanel.add(outputSlot2, 7, 1);

        experienceText = new WText(
                Component.translatable(""), 0).setVerticalAlignment(VerticalAlignment.CENTER)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT).setDrawShadows(true);

        root.add(this.createPlayerInventoryPanel(), 0, GuiHelper.INVENTORY_Y_OFFSET);

        root.add(gridPanel, 0, GuiHelper.LABEL_HEIGHT);
        root.validate(this);
    }

    private boolean allowTakingOutput() {
        return hasCrafted || (playerInventory.player.experienceLevel >= xpCost
                && blockInventory.getItem(INPUT2_ID).getCount() >= secondInputUsageCount);
    }

    private State calculateOutput() {
        if (hasCrafted) {
            return State.Crafted;
        }

        ItemStack input1 = blockInventory.getItem(INPUT1_ID);
        ItemStack input2 = blockInventory.getItem(INPUT2_ID);

        if (input1.isEmpty()) {
            resetValues();
            return State.None;
        }

        if (input2.isEmpty()) {
            resetValues();
            return State.NoItem;
        }

        EnchantingReimaginedConfig config = AutoConfig.getConfigHolder(EnchantingReimaginedConfig.class)
                .getConfig();

        // Calculate output
        if (input1.getItem() == Items.ENCHANTED_BOOK) {
            /// Combining
            if (input2.getItem() == Items.ENCHANTED_BOOK) {
                State state = applyEnchantments(config, input1, input2);
                if (state != null) {
                    return state;
                }
            }

            // Splitting
            else if (input2.getItem() == Items.BOOK) {
                State state = splitBook(config, input1);
                if (state != null) {
                    return state;
                }
            }

            // Upgrading
            else if (input2.getItem() == EnchantingReimagined.ENCHANTMENT_DUST
                    || input2.getItem() == EnchantingReimagined.ADVANCED_ENCHANTMENT_DUST) {
                State state = upgradeBook(config, input1, input2);
                if (state != null) {
                    return state;
                }
            }

            // Scrubbing
            else if (input2.getItem() == EnchantingReimagined.CURSER_SCRUBBER) {
                State state = scrubAwayCurses(config, input1);
                if (state != null) {
                    return state;
                }
            }

            else {
                resetValues();
                return State.IncorrectSecondItem;
            }
        } else

        // Extracting
        if (input1.isEnchanted() && input2.getItem() == Items.BOOK) {
            State state = extractEnchantments(config, input1);
            if (state != null) {
                return state;
            }
        } else

        // Scrubbing
        if (input1.isEnchanted() && input2.getItem() == EnchantingReimagined.CURSER_SCRUBBER) {
            State state = scrubAwayCurses(config, input1);
            if (state != null) {
                return state;
            }
        }

        // Repairing
        else if (input1.isDamageableItem() && input1.isValidRepairItem(input2)) {
            State state = repairItem(config, input1, input2);
            if (state != null) {
                return state;
            }
        }

        // Applying
        else if (input2.get(DataComponents.STORED_ENCHANTMENTS) != null) {
            State state = applyEnchantments(config, input1, input2);
            if (state != null) {
                return state;
            }
        }

        else {
            resetValues();
            return State.IncorrectSecondItem;
        }

        if (playerInventory.player.isCreative()) {
            xpCost = 0;
        }

        // Check xp
        if (xpCost > playerInventory.player.experienceLevel) {
            return State.NotEnoughXp;
        }

        return State.None;
    }

    // Extract enchantments from item
    private State extractEnchantments(EnchantingReimaginedConfig config, ItemStack input1) {
        if (!config.extraction.allowExtraction) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        ItemEnchantments enchantmentComponent = input1.getEnchantments();
        Set<Holder<Enchantment>> enchantments = enchantmentComponent.keySet();
        int xpExtractionCost = 0;

        for (Holder<Enchantment> enchantment : enchantments) {
            if (enchantment.is(EnchantmentTags.CURSE)) {
                resetValues();
                return State.HasCurse;
            }

            xpExtractionCost += config.extraction.extractionCostPerLevel
                    ? enchantmentComponent.getLevel(enchantment) * config.extraction.extractionCost
                    : config.extraction.extractionCost;
        }

        boolean consumeItem = config.extraction.consumeItem;

        ItemStack echantmentOutputStack = new ItemStack(Items.ENCHANTED_BOOK);
        echantmentOutputStack.set(DataComponents.STORED_ENCHANTMENTS,
                enchantmentComponent);

        if (consumeItem) {
            blockInventory.setItem(OUTPUT1_ID, echantmentOutputStack);
        } else {
            ItemStack outputStack = input1.copy();
            outputStack.set(DataComponents.ENCHANTMENTS,
                    outputStack.getPrototype().get(DataComponents.ENCHANTMENTS));
            blockInventory.setItem(OUTPUT1_ID, outputStack);
            blockInventory.setItem(OUTPUT2_ID, echantmentOutputStack);
        }

        xpCost = xpExtractionCost;
        secondInputUsageCount = 1;
        outputSlotsUsed = (consumeItem ? 1 : 2);
        return null;
    }

    // Scrub away curses from item or enchanted book
    private State scrubAwayCurses(EnchantingReimaginedConfig config, ItemStack input1) {
        if (!config.scrubbing.allowScrubbing) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        ItemStack outputStack = input1.copy();
        ItemEnchantments enchantmentComponent;
        boolean isBook = input1.getItem() == Items.ENCHANTED_BOOK;

        if (isBook) {
            enchantmentComponent = outputStack.get(DataComponents.STORED_ENCHANTMENTS);
        } else {
            enchantmentComponent = outputStack.getEnchantments();
        }

        Set<Holder<Enchantment>> enchantments = enchantmentComponent.keySet();
        int xpScrubbingCost = 0;
        boolean curseFound = false;

        // Calculate cost
        for (Holder<Enchantment> enchantment : enchantments) {
            if (enchantment.is(EnchantmentTags.CURSE)) {
                curseFound = true;
                xpScrubbingCost += config.scrubbing.scrubbingCost;
            }
        }

        // No curses
        if (!curseFound) {
            resetValues();
            return State.HasNoCurse;
        }

        // Create output component
        ItemEnchantments.Mutable outputComponentBuilder = new ItemEnchantments.Mutable(
                enchantmentComponent);
        outputComponentBuilder.removeIf(enchantment -> enchantment.is(EnchantmentTags.CURSE));
        ItemEnchantments outputComponent = outputComponentBuilder.toImmutable();

        if (isBook) {
            if (!outputComponent.isEmpty()) {
                outputStack.set(DataComponents.STORED_ENCHANTMENTS, outputComponent);
            } else {
                outputStack = new ItemStack(Items.BOOK);
            }
        } else {
            if (!outputComponent.isEmpty()) {
                outputStack.set(DataComponents.ENCHANTMENTS, outputComponent);
            } else {
                outputStack.set(DataComponents.ENCHANTMENTS,
                        outputStack.getPrototype().get(DataComponents.ENCHANTMENTS));
            }
        }

        blockInventory.setItem(OUTPUT1_ID, outputStack);
        xpCost = xpScrubbingCost;
        secondInputUsageCount = 1;
        outputSlotsUsed = 1;

        return null;
    }

    // Repair item
    private State repairItem(EnchantingReimaginedConfig config, ItemStack input1, ItemStack input2) {
        if (!config.repairing.allowRepair) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        if (!input1.isDamaged()) {
            resetValues();
            return State.NotRepairable;
        }

        int repairCost = config.repairing.consistentRepairPrice ? config.repairing.repairPrice
                : config.repairing.repairPrice
                        + input1.getOrDefault(DataComponents.REPAIR_COST, 0);

        ItemStack outputStack = input1.copy();
        int repairAmount = Math.min(input1.getDamageValue(), input1.getMaxDamage() / 4);
        int materialUsage;

        // Iteratively repair with each item in the second input slot
        for (materialUsage = 0; repairAmount > 0 && materialUsage < input2.getCount(); materialUsage++) {
            int newDamage = outputStack.getDamageValue() - repairAmount;
            outputStack.setDamageValue(newDamage);
            repairAmount = Math.min(outputStack.getDamageValue(), outputStack.getMaxDamage() / 4);
        }

        blockInventory.setItem(OUTPUT1_ID, outputStack);
        xpCost = repairCost;
        secondInputUsageCount = materialUsage;
        outputSlotsUsed = 1;

        return null;
    }

    // Apply enchantments from enchanted book onto item or book
    private State applyEnchantments(EnchantingReimaginedConfig config, ItemStack input1, ItemStack input2) {
        if (!config.applying.allowApplying) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        ItemStack outputStack = input1.copy();
        ItemEnchantments enchantmentComponent;
        boolean isBook = input1.getItem() == Items.ENCHANTED_BOOK;

        if (isBook) {
            enchantmentComponent = outputStack.get(DataComponents.STORED_ENCHANTMENTS);
        } else {
            enchantmentComponent = outputStack.getEnchantments();
        }

        ItemEnchantments.Mutable outputEnchantments = new ItemEnchantments.Mutable(
                enchantmentComponent);
        ItemEnchantments secondInputEnchantments = input2.get(DataComponents.STORED_ENCHANTMENTS);

        if (secondInputEnchantments == null) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        int xpApplyingCost = 0;
        boolean addedEnchantment = false;

        for (Holder<Enchantment> enchantment : secondInputEnchantments.keySet()) {
            if (!isBook && !enchantment.value().canEnchant(input1)) {
                continue;
            }

            boolean continueOuter = false;
            for (Holder<Enchantment> enchantment2 : outputEnchantments.keySet()) {
                // Cannot combine
                if (enchantment.value() != enchantment2.value()
                        && !config.combining.allowInvalidCombinations
                        && (enchantment.value().exclusiveSet().contains(enchantment2)
                                || enchantment2.value().exclusiveSet().contains(enchantment))) {
                    continueOuter = true;
                    break;
                }
            }

            if (continueOuter) {
                continue;
            }

            // Combine enchantment
            int currentLevel = outputEnchantments.getLevel(enchantment);
            int bookLevel = secondInputEnchantments.getLevel(enchantment);
            int newLevel = Math.max(currentLevel, bookLevel);
            if (currentLevel == bookLevel && currentLevel < enchantment.value().getMaxLevel()) {
                newLevel += 1;
            }
            outputEnchantments.set(enchantment, newLevel);

            // Calculate cost
            if (newLevel > currentLevel) {
                addedEnchantment = true;

                if (!isBook) {
                    xpApplyingCost += config.applying.applyingCostPerLevel
                            ? (newLevel - currentLevel) * config.applying.applyingCost
                            : config.applying.applyingCost;
                }
            }
        }

        if (isBook) {
            xpApplyingCost = config.combining.combiningCost;
        }

        // No new enchantments added
        if (!addedEnchantment) {
            resetValues();
            return State.NoNewEnchantments;
        }

        if (isBook) {
            outputStack.set(DataComponents.STORED_ENCHANTMENTS, outputEnchantments.toImmutable());
        } else {
            outputStack.set(DataComponents.ENCHANTMENTS, outputEnchantments.toImmutable());
        }

        blockInventory.setItem(OUTPUT1_ID, outputStack);
        xpCost = xpApplyingCost;
        secondInputUsageCount = 1;
        outputSlotsUsed = 1;

        return null;
    }

    // Transfer first enchantment from book over to new book
    private State splitBook(EnchantingReimaginedConfig config, ItemStack input1) {
        if (!config.splitting.allowSplitting) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        ItemStack outputStack1 = input1.copy();
        ItemStack outputStack2 = input1.copy();
        ItemEnchantments.Mutable outputEnchantments1 = new ItemEnchantments.Mutable(
                input1.get(DataComponents.STORED_ENCHANTMENTS));
        ItemEnchantments.Mutable outputEnchantments2 = new ItemEnchantments.Mutable(
                ItemEnchantments.EMPTY);
        ItemEnchantments secondInputEnchantments = input1.get(DataComponents.STORED_ENCHANTMENTS);

        if (secondInputEnchantments.keySet().stream()
                .anyMatch(enchantment -> enchantment.is(EnchantmentTags.CURSE))) {
            resetValues();
            return State.HasCurse;
        }

        if (secondInputEnchantments.keySet().size() < 2) {
            resetValues();
            return State.OnlyOneEnchant;
        }

        // Move one enchantment from book onto new book
        Holder<Enchantment> chosenEnchantment = secondInputEnchantments.keySet().iterator().next();
        outputEnchantments1.removeIf(enchantment -> enchantment.equals(chosenEnchantment));
        outputEnchantments2.upgrade(chosenEnchantment, secondInputEnchantments.getLevel(chosenEnchantment));

        outputStack1.set(DataComponents.STORED_ENCHANTMENTS, outputEnchantments1.toImmutable());
        outputStack2.set(DataComponents.STORED_ENCHANTMENTS, outputEnchantments2.toImmutable());
        blockInventory.setItem(OUTPUT1_ID, outputStack1);
        blockInventory.setItem(OUTPUT2_ID, outputStack2);
        xpCost = config.splitting.splittingCost;
        secondInputUsageCount = 1;
        outputSlotsUsed = 2;

        return null;
    }

    // Increase levels of enchanted book
    private State upgradeBook(EnchantingReimaginedConfig config, ItemStack input1, ItemStack input2) {
        if (!config.upgrading.allowUpgrading) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        int xpUpgradingCost = 0;
        int dustUsage = 0;

        ItemStack outputStack = input1.copy();
        ItemEnchantments.Mutable outputEnchantments = new ItemEnchantments.Mutable(
                ItemEnchantments.EMPTY);
        ItemEnchantments secondInputEnchantments = input1.get(DataComponents.STORED_ENCHANTMENTS);

        for (Holder<Enchantment> enchantment : secondInputEnchantments.keySet()) {
            int level = secondInputEnchantments.getLevel(enchantment);
            int maxLevel = enchantment.value().getMaxLevel();
            if (maxLevel > 1 && input2.getItem() == EnchantingReimagined.ADVANCED_ENCHANTMENT_DUST) {
                maxLevel += config.upgrading.advancedDustExtraLevelCount;
            }

            while (dustUsage < input2.getCount() && level < maxLevel) {
                level += 1;
                dustUsage += 1;
                xpUpgradingCost += config.upgrading.upgradingCost;
            }

            outputEnchantments.upgrade(enchantment, level);
        }

        if (dustUsage == 0) {
            resetValues();
            return State.AlreadyMax;
        }

        outputStack.set(DataComponents.STORED_ENCHANTMENTS, outputEnchantments.toImmutable());
        blockInventory.setItem(OUTPUT1_ID, outputStack);
        xpCost = xpUpgradingCost;
        secondInputUsageCount = dustUsage;
        outputSlotsUsed = 1;

        return null;
    }

    private void resetValues() {
        if (!hasCrafted) {
            blockInventory.setItem(OUTPUT1_ID, ItemStack.EMPTY);
            blockInventory.setItem(OUTPUT2_ID, ItemStack.EMPTY);
        }

        xpCost = 0;
        secondInputUsageCount = 0;
        outputSlotsUsed = 0;
        errorTooltip = null;
    }

    private void updateGui(State state) {
        gridPanel.remove(errorSprite);
        gridPanel.remove(experienceText);

        // Error cross
        if (state != State.None && state != State.Crafted) {
            switch (state) {
                case AlreadyMax ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.already_max";
                case HasCurse ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.has_curse";
                case HasNoCurse ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.has_no_curse";
                case NoNewEnchantments ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.no_new_enchantments";
                case NotEnoughXp ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.not_enought_xp";
                case NotRepairable ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.not_repairable";
                case OnlyOneEnchant ->
                    errorTooltip = "container.enchanting_reimagined.enchanting_workstation.error.only_one_enchant";
                default -> {
                }
            }

            gridPanel.add(errorSprite, 4, 1, 2, 1);
        }

        // Experience text
        if (xpCost > 0) {
            experienceText.setText(Component
                    .translatable("container.enchanting_reimagined.enchanting_workstation.experience_cost", xpCost));
            experienceText.setColor(state == State.NotEnoughXp ? GuiHelper.ERROR_COLOR : GuiHelper.EXPERIENCE_COLOR);
            gridPanel.add(experienceText, 0, 2, 9, 1);
        }

        gridPanel.layout();
    }

    @Override
    public void clicked(final int slotIndex, final int button, final ContainerInput containerInput,
            final Player player) {
        super.clicked(slotIndex, button, containerInput, player);

        // Detect taking first item from output
        if (outputSlotsUsed > 0
                && (slotIndex == outputSlot1.getPeerStartId() || slotIndex == outputSlot2.getPeerStartId())) {
            int currentOutputSlotsUsed = 0;
            if (!blockInventory.getItem(OUTPUT1_ID).isEmpty()) {
                currentOutputSlotsUsed += 1;
            }
            if (!blockInventory.getItem(OUTPUT2_ID).isEmpty()) {
                currentOutputSlotsUsed += 1;
            }

            if (currentOutputSlotsUsed < outputSlotsUsed) {
                blockInventory.removeItem(INPUT1_ID, 1);
                blockInventory.removeItem(INPUT2_ID, secondInputUsageCount);
                player.giveExperienceLevels(-xpCost);
                player.awardStat(EnchantingReimagined.CRAFT_IN_ENCHANTING_WORKSTATION);
                context.execute((contextWorld, pos) -> {
                    contextWorld.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f,
                            contextWorld.getRandom().nextFloat() * 0.3f + 0.7f);
                });
                hasCrafted = true;

                // Let detection be done on both client and server
                if (!getLevel().isClientSide()) {
                    resetValues();
                }
            }
        }

        // Detect all output items taken
        if (hasCrafted) {
            if (blockInventory.getItem(OUTPUT1_ID).isEmpty() && blockInventory.getItem(OUTPUT2_ID).isEmpty()) {
                if (!getLevel().isClientSide()) {
                    hasCrafted = false;
                } else {
                    return;
                }
            }
        }

        updateGui(calculateOutput());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Return items to player
        for (int slot_id : INPUT_SLOT_IDS) {
            ItemStack stack = blockInventory.getItem(slot_id);
            playerInventory.placeItemBackInInventory(stack);
        }

        if (hasCrafted) {
            for (int slot_id : OUTPUT_SLOT_IDS) {
                ItemStack stack = blockInventory.getItem(slot_id);
                playerInventory.placeItemBackInInventory(stack);
            }
        }
    }
}

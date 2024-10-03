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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

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

    private final ScreenHandlerContext context;
    private static WGridPanel gridPanel;
    private static WSprite errorSprite;
    private static String errorTooltip;
    private static WText experienceText;
    private static WItemSlot outputSlot1;
    private static WItemSlot outputSlot2;

    public EnchantingWorkstationGui(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
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
                    tooltip.add(Text.translatable(errorTooltip));
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
                Text.translatable(""), 0).setVerticalAlignment(VerticalAlignment.CENTER)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT).setDrawShadows(true);

        root.add(this.createPlayerInventoryPanel(), 0, GuiHelper.INVENTORY_Y_OFFSET);

        root.add(gridPanel, 0, GuiHelper.LABEL_HEIGHT);
        root.validate(this);
    }

    private boolean allowTakingOutput() {
        return hasCrafted || (playerInventory.player.experienceLevel >= xpCost
                && blockInventory.getStack(INPUT2_ID).getCount() >= secondInputUsageCount);
    }

    private State calculateOutput() {
        if (hasCrafted) {
            return State.Crafted;
        }

        ItemStack input1 = blockInventory.getStack(INPUT1_ID);
        ItemStack input2 = blockInventory.getStack(INPUT2_ID);

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
        if (input1.hasEnchantments() && input2.getItem() == Items.BOOK) {
            State state = extractEnchantments(config, input1);
            if (state != null) {
                return state;
            }
        } else

        // Scrubbing
        if (input1.hasEnchantments() && input2.getItem() == EnchantingReimagined.CURSER_SCRUBBER) {
            State state = scrubAwayCurses(config, input1);
            if (state != null) {
                return state;
            }
        }

        // Repairing
        else if (input1.isDamageable() && input1.getItem().canRepair(input1, input2)) {
            State state = repairItem(config, input1, input2);
            if (state != null) {
                return state;
            }
        }

        // Applying
        else if (input2.get(DataComponentTypes.STORED_ENCHANTMENTS) != null) {
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

        ItemEnchantmentsComponent enchantmentComponent = input1.getEnchantments();
        Set<RegistryEntry<Enchantment>> enchantments = enchantmentComponent.getEnchantments();
        int xpExtractionCost = 0;

        for (RegistryEntry<Enchantment> enchantment : enchantments) {
            if (enchantment.isIn(EnchantmentTags.CURSE)) {
                resetValues();
                return State.HasCurse;
            }

            xpExtractionCost += config.extraction.extractionCostPerLevel
                    ? enchantmentComponent.getLevel(enchantment) * config.extraction.extractionCost
                    : config.extraction.extractionCost;
        }

        boolean consumeItem = config.extraction.consumeItem;

        ItemStack echantmentOutputStack = new ItemStack(Items.ENCHANTED_BOOK);
        echantmentOutputStack.set(DataComponentTypes.STORED_ENCHANTMENTS,
                enchantmentComponent);

        if (consumeItem) {
            blockInventory.setStack(OUTPUT1_ID, echantmentOutputStack);
        } else {
            ItemStack outputStack = input1.copy();
            outputStack.remove(DataComponentTypes.ENCHANTMENTS);
            blockInventory.setStack(OUTPUT1_ID, outputStack);
            blockInventory.setStack(OUTPUT2_ID, echantmentOutputStack);
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
        ItemEnchantmentsComponent enchantmentComponent;
        boolean isBook = input1.getItem() == Items.ENCHANTED_BOOK;

        if (isBook) {
            enchantmentComponent = outputStack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        } else {
            enchantmentComponent = outputStack.getEnchantments();
        }

        Set<RegistryEntry<Enchantment>> enchantments = enchantmentComponent.getEnchantments();
        int xpScrubbingCost = 0;
        boolean curseFound = false;

        // Calculate cost
        for (RegistryEntry<Enchantment> enchantment : enchantments) {
            if (enchantment.isIn(EnchantmentTags.CURSE)) {
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
        ItemEnchantmentsComponent.Builder outputComponentBuilder = new ItemEnchantmentsComponent.Builder(
                enchantmentComponent);
        outputComponentBuilder.remove(enchantment -> enchantment.isIn(EnchantmentTags.CURSE));
        ItemEnchantmentsComponent outputComponent = outputComponentBuilder.build();

        if (isBook) {
            if (!outputComponent.isEmpty()) {
                outputStack.set(DataComponentTypes.STORED_ENCHANTMENTS, outputComponent);
            } else {
                outputStack = new ItemStack(Items.BOOK);
            }
        } else {
            if (!outputComponent.isEmpty()) {
                outputStack.set(DataComponentTypes.ENCHANTMENTS, outputComponent);
            } else {
                outputStack.remove(DataComponentTypes.ENCHANTMENTS);
            }
        }

        blockInventory.setStack(OUTPUT1_ID, outputStack);
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
                        + input1.getOrDefault(DataComponentTypes.REPAIR_COST, 0);

        ItemStack outputStack = input1.copy();
        int repairAmount = Math.min(input1.getDamage(), input1.getMaxDamage() / 4);
        int materialUsage;

        // Iteratively repair with each item in the second input slot
        for (materialUsage = 0; repairAmount > 0 && materialUsage < input2.getCount(); materialUsage++) {
            int newDamage = outputStack.getDamage() - repairAmount;
            outputStack.setDamage(newDamage);
            repairAmount = Math.min(outputStack.getDamage(), outputStack.getMaxDamage() / 4);
        }

        blockInventory.setStack(OUTPUT1_ID, outputStack);
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
        ItemEnchantmentsComponent enchantmentComponent;
        boolean isBook = input1.getItem() == Items.ENCHANTED_BOOK;

        if (isBook) {
            enchantmentComponent = outputStack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        } else {
            enchantmentComponent = outputStack.getEnchantments();
        }

        ItemEnchantmentsComponent.Builder outputEnchantments = new ItemEnchantmentsComponent.Builder(
                enchantmentComponent);
        ItemEnchantmentsComponent secondInputEnchantments = input2.get(DataComponentTypes.STORED_ENCHANTMENTS);

        if (secondInputEnchantments == null) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        int xpApplyingCost = 0;
        boolean addedEnchantment = false;

        for (RegistryEntry<Enchantment> enchantment : secondInputEnchantments.getEnchantments()) {
            if (!isBook && !enchantment.value().isAcceptableItem(input1)) {
                continue;
            }

            boolean continueOuter = false;
            for (RegistryEntry<Enchantment> enchantment2 : outputEnchantments.getEnchantments()) {
                // Cannot combine
                if (enchantment.value() != enchantment2.value()
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
            outputStack.set(DataComponentTypes.STORED_ENCHANTMENTS, outputEnchantments.build());
        } else {
            outputStack.set(DataComponentTypes.ENCHANTMENTS, outputEnchantments.build());
        }

        blockInventory.setStack(OUTPUT1_ID, outputStack);
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
        ItemEnchantmentsComponent.Builder outputEnchantments1 = new ItemEnchantmentsComponent.Builder(
                input1.get(DataComponentTypes.STORED_ENCHANTMENTS));
        ItemEnchantmentsComponent.Builder outputEnchantments2 = new ItemEnchantmentsComponent.Builder(
                ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent secondInputEnchantments = input1.get(DataComponentTypes.STORED_ENCHANTMENTS);

        if (secondInputEnchantments.getEnchantments().stream()
                .anyMatch(enchantment -> enchantment.isIn(EnchantmentTags.CURSE))) {
            resetValues();
            return State.HasCurse;
        }

        if (secondInputEnchantments.getEnchantments().size() < 2) {
            resetValues();
            return State.OnlyOneEnchant;
        }

        // Move one enchantment from book onto new book
        RegistryEntry<Enchantment> chosenEnchantment = secondInputEnchantments.getEnchantments().iterator().next();
        outputEnchantments1.remove(enchantment -> enchantment.equals(chosenEnchantment));
        outputEnchantments2.add(chosenEnchantment, secondInputEnchantments.getLevel(chosenEnchantment));

        outputStack1.set(DataComponentTypes.STORED_ENCHANTMENTS, outputEnchantments1.build());
        outputStack2.set(DataComponentTypes.STORED_ENCHANTMENTS, outputEnchantments2.build());
        blockInventory.setStack(OUTPUT1_ID, outputStack1);
        blockInventory.setStack(OUTPUT2_ID, outputStack2);
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
        ItemEnchantmentsComponent.Builder outputEnchantments = new ItemEnchantmentsComponent.Builder(
                ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent secondInputEnchantments = input1.get(DataComponentTypes.STORED_ENCHANTMENTS);

        for (RegistryEntry<Enchantment> enchantment : secondInputEnchantments.getEnchantments()) {
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

            outputEnchantments.add(enchantment, level);
        }

        if (dustUsage == 0) {
            resetValues();
            return State.AlreadyMax;
        }

        outputStack.set(DataComponentTypes.STORED_ENCHANTMENTS, outputEnchantments.build());
        blockInventory.setStack(OUTPUT1_ID, outputStack);
        xpCost = xpUpgradingCost;
        secondInputUsageCount = dustUsage;
        outputSlotsUsed = 1;

        return null;
    }

    private void resetValues() {
        if (!hasCrafted) {
            blockInventory.setStack(OUTPUT1_ID, ItemStack.EMPTY);
            blockInventory.setStack(OUTPUT2_ID, ItemStack.EMPTY);
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
            experienceText.setText(Text
                    .translatable("container.enchanting_reimagined.enchanting_workstation.experience_cost", xpCost));
            experienceText.setColor(state == State.NotEnoughXp ? GuiHelper.ERROR_COLOR : GuiHelper.EXPERIENCE_COLOR);
            gridPanel.add(experienceText, 0, 2, 9, 1);
        }

        gridPanel.layout();
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);

        // Detect taking first item from output
        if (outputSlotsUsed > 0
                && (slotIndex == outputSlot1.getPeerStartId() || slotIndex == outputSlot2.getPeerStartId())) {
            int currentOutputSlotsUsed = 0;
            if (!blockInventory.getStack(OUTPUT1_ID).isEmpty()) {
                currentOutputSlotsUsed += 1;
            }
            if (!blockInventory.getStack(OUTPUT2_ID).isEmpty()) {
                currentOutputSlotsUsed += 1;
            }

            if (currentOutputSlotsUsed < outputSlotsUsed) {
                blockInventory.removeStack(INPUT1_ID, 1);
                blockInventory.removeStack(INPUT2_ID, secondInputUsageCount);
                player.addExperienceLevels(-xpCost);
                player.incrementStat(EnchantingReimagined.CRAFT_IN_ENCHANTING_WORKSTATION);
                context.run((contextWorld, pos) -> {
                    contextWorld.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f,
                            contextWorld.getRandom().nextFloat() * 0.3f + 0.7f);
                });
                hasCrafted = true;

                // Let detection be done on both client and server
                if (!world.isClient) {
                    resetValues();
                }
            }
        }

        // Detect all output items taken
        if (hasCrafted) {
            if (blockInventory.getStack(OUTPUT1_ID).isEmpty() && blockInventory.getStack(OUTPUT2_ID).isEmpty()) {
                if (!world.isClient) {
                    hasCrafted = false;
                } else {
                    return;
                }
            }
        }

        updateGui(calculateOutput());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        // Return items to player
        for (int slot_id : INPUT_SLOT_IDS) {
            ItemStack stack = blockInventory.getStack(slot_id);
            playerInventory.offerOrDrop(stack);
        }

        if (hasCrafted) {
            for (int slot_id : OUTPUT_SLOT_IDS) {
                ItemStack stack = blockInventory.getStack(slot_id);
                playerInventory.offerOrDrop(stack);
            }
        }
    }
}

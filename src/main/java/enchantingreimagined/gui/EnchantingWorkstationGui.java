package enchantingreimagined.gui;

import java.util.Iterator;
import java.util.Set;

import enchantingreimagined.EnchantingReimagined;
import enchantingreimagined.EnchantingReimaginedConfig;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
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
        None, Crafted, NotEnoughXp, HasCurse, AlreadyMax, NoItem, IncorrectSecondItem, NotRepairable
    };

    private static boolean hasCrafted = false;
    private static int xpCost = 0;
    private static int secondInputUsageCount = 0;

    private static WGridPanel gridPanel;
    private static WSprite errorSprite;
    private static WText experienceText;
    private static WItemSlot outputSlot1;
    private static WItemSlot outputSlot2;

    public EnchantingWorkstationGui(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(EnchantingReimagined.ENCHANTING_WORKSTATION_SCREEN_TYPE, syncId, playerInventory,
                getBlockInventory(context, INVENTORY_SIZE),
                getBlockPropertyDelegate(context));

        hasCrafted = false;
        xpCost = 0;

        WPlainPanel root = new WPlainPanel();
        root.setInsets(Insets.ROOT_PANEL);
        setRootPanel(root);

        gridPanel = new WGridPanel();
        setTitleAlignment(HorizontalAlignment.CENTER);

        WItemSlot input1 = WItemSlot.of(blockInventory, INPUT1_ID)
                .setInputFilter(stack -> stack.getItem() == Items.ENCHANTED_BOOK || stack.isDamaged()
                        || stack.hasEnchantments());
        gridPanel.add(input1, 1, 1);

        WSprite plus = new WSprite(GuiHelper.PLUS_TEXTURE);
        gridPanel.add(plus, 2, 1);

        WItemSlot input2 = WItemSlot.of(blockInventory, INPUT2_ID);
        gridPanel.add(input2, 3, 1);

        WSprite arrowBackground = new WSprite(GuiHelper.ARROW_TEXTURE);
        gridPanel.add(arrowBackground, 4, 1, 2, 1);

        errorSprite = new WSprite(GuiHelper.ERROR_TEXTURE);

        outputSlot1 = WItemSlot.of(blockInventory, OUTPUT1_ID).setInsertingAllowed(false)
                .setOutputFilter(stack -> allowTakingOutput());
        gridPanel.add(outputSlot1, 6, 1);

        outputSlot2 = new WItemSlot(blockInventory, OUTPUT2_ID, 1, 1, false).setInsertingAllowed(false)
                .setOutputFilter(stack -> allowTakingOutput());
        gridPanel.add(outputSlot2, 7, 1);

        experienceText = new WText(
                Text.translatable(""), 0);
        experienceText.setVerticalAlignment(VerticalAlignment.CENTER);
        experienceText.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        root.add(this.createPlayerInventoryPanel(), 0, GuiHelper.INVENTORY_Y_OFFSET);

        root.add(gridPanel, 0, GuiHelper.LABEL_HEIGHT);
        root.validate(this);
    }

    private boolean allowTakingOutput() {
        return playerInventory.player.experienceLevel >= xpCost
                && blockInventory.getStack(INPUT2_ID).getCount() >= secondInputUsageCount;
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

            }

            // Splitting
            else if (input2.getItem() == Items.BOOK) {

            }

            // Upgrading
            else if (input2.getItem() == EnchantingReimagined.ENCHANTMENT_DUST) {

            }

            // Scrubbing
            else if (input2.getItem() == EnchantingReimagined.CURSER_SCRUBBER) {

            }

            else {
                resetValues();
                return State.IncorrectSecondItem;
            }
        } else if (input1.hasEnchantments()) {
            // Extracting
            if (input2.getItem() == Items.BOOK) {
                State state = extractEnchantments(config, input1);
                if (state != null) {
                    return state;
                }
            }

            // Scrubbing
            else if (input2.getItem() == EnchantingReimagined.CURSER_SCRUBBER) {
                State state = scrubCursesItem(config, input1, input2);
                if (state != null) {
                    return state;
                }
            } else {
                resetValues();
                return State.IncorrectSecondItem;
            }
        }
        // Repairing
        else if (input1.isDamaged()
                && input1.getItem().canRepair(input1, input2)) {
            State state = repairItem(config, input1, input2);
            if (state != null) {
                return state;
            }
        }
        // Applying
        else if (input2.get(DataComponentTypes.STORED_ENCHANTMENTS) != null) {

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

        Iterator<RegistryEntry<Enchantment>> enchanmentIterator = enchantments.iterator();
        while (enchanmentIterator.hasNext()) {
            RegistryEntry<Enchantment> enchantment = enchanmentIterator.next();
            if (enchantment.isIn(EnchantmentTags.CURSE)) {
                resetValues();
                return State.HasCurse;
            }

            xpExtractionCost += config.extraction.extractionCostPerLevel
                    ? enchantmentComponent.getLevel(enchantment) * config.extraction.extractionCost
                    : config.extraction.extractionCost;
        }

        ItemStack outputStack = new ItemStack(Items.ENCHANTED_BOOK);
        System.out.println("OUT" + outputStack);
        outputStack.set(DataComponentTypes.STORED_ENCHANTMENTS,
                enchantmentComponent);
        blockInventory.setStack(OUTPUT1_ID, outputStack);
        xpCost = xpExtractionCost;

        return null;
    }

    // Scrub away curses from item
    private State scrubCursesItem(EnchantingReimaginedConfig config, ItemStack input1, ItemStack input2) {
        if (!config.scrubbing.allowScrubbing) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        ItemStack outputStack = input1.copy();
        Set<RegistryEntry<Enchantment>> enchantments = outputStack.getEnchantments().getEnchantments();
        int xpScrubbingCost = 0;
        // Calculate cost
        for (RegistryEntry<Enchantment> enchantment : enchantments) {
            if (enchantment.isIn(EnchantmentTags.CURSE)) {
                xpScrubbingCost += config.scrubbing.scrubbingCost;
            }
        }

        // No curses
        if (xpScrubbingCost == 0) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        // Create output component
        ItemEnchantmentsComponent.Builder outputComponentBuilder = new ItemEnchantmentsComponent.Builder(
                outputStack.getEnchantments());
        outputComponentBuilder.remove(enchantment -> enchantment.isIn(EnchantmentTags.CURSE));
        ItemEnchantmentsComponent outputComponent = outputComponentBuilder.build();

        System.out.println("SC");

        if (!outputComponent.isEmpty()) {
            outputStack.set(DataComponentTypes.ENCHANTMENTS, outputComponent);
        } else {
            outputStack.remove(DataComponentTypes.ENCHANTMENTS);
        }

        blockInventory.setStack(OUTPUT1_ID, outputStack);
        xpCost = xpScrubbingCost;

        return null;
    }

    // Repair item
    private State repairItem(EnchantingReimaginedConfig config, ItemStack input1, ItemStack input2) {
        if (!config.repairing.allowRepair) {
            resetValues();
            return State.IncorrectSecondItem;
        }

        int repairCost = config.repairing.consistentRepairPrice ? config.repairing.repairPrice
                : config.repairing.repairPrice
                        + input1.getOrDefault(DataComponentTypes.REPAIR_COST, Integer.valueOf(0)).intValue();

        ItemStack outputStack = input1.copy();
        int repairAmount = Math.min(input1.getDamage(), input1.getMaxDamage() / 4);
        // Iteratively repair with each item in the second input slot
        for (int i = 0; repairAmount > 0 && i < input2.getCount(); i++) {
            int newDamage = outputStack.getDamage() - repairAmount;
            outputStack.setDamage(newDamage);
            repairAmount = Math.min(outputStack.getDamage(), outputStack.getMaxDamage() / 4);
        }

        blockInventory.setStack(OUTPUT1_ID, outputStack);
        xpCost = repairCost;
        secondInputUsageCount = repairAmount;

        return null;
    }

    private void resetValues() {
        blockInventory.setStack(OUTPUT1_ID, ItemStack.EMPTY);
        blockInventory.setStack(OUTPUT2_ID, ItemStack.EMPTY);
        xpCost = 0;
        secondInputUsageCount = 0;
    }

    private void updateGui(State state) {
        gridPanel.remove(errorSprite);
        gridPanel.remove(experienceText);

        // Error cross
        if (state != State.None && state != State.Crafted) {
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

        // Taken from output
        // System.out.println("CLICK" + slots.get(slotIndex).getStack() +
        // getCursorStack());
        // if ((slotIndex == slots.size() - 1 || slotIndex == slots.size() - 2) &&
        // !getCursorStack().ise

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
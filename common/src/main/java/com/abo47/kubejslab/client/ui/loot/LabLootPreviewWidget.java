package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.picker.LabPickerEntries;
import com.abo47.kubejslab.loot.model.LabLootEntryValues;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootPreviewWidget extends WidgetGroup {
    private static final int FRONT_ENTITY_YAW = 205;
    private static final double FILL = 0.82D;
    private static final double MAX_SCALE = 96.0D;
    private static final int SLOT = 18;
    private static final int GAP = 2;
    private static final int PAD = 4;
    private static final int TARGET_BOX = 44;
    private static final int FOOTER_H = 10;

    public interface DropClick {
        void onDropClick(int poolIndex, int entryIndex);
    }

    private record DropSlot(int poolIndex, int entryIndex, LabLootEntryValues entry, LabLootPoolValues pool) {
    }

    private ResourceLocation entryId;
    private String lootType = LabLootService.LOOT_TYPE_BLOCK;
    private LabLootFieldValues values;
    private final List<DropSlot> drops = new ArrayList<>();
    private int scroll;
    private int scrollMax;
    private DropClick dropClickListener;

    public LabLootPreviewWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        lootType = LabLootService.LOOT_TYPE_BLOCK;
    }

    public void setOnDropClick(DropClick listener) {
        dropClickListener = listener;
    }

    public void setEntry(ResourceLocation id, String lootType) {
        setEntry(id, lootType, null);
    }

    public void setEntry(ResourceLocation id, String lootType, LabLootFieldValues values) {
        this.entryId = id;
        this.lootType = lootType == null || lootType.isBlank() ? LabLootService.LOOT_TYPE_BLOCK : lootType;
        this.values = values;
        rebuildDrops();
    }

    private void rebuildDrops() {
        int oldScroll = scroll;
        drops.clear();
        scrollMax = 0;
        if (values == null) {
            return;
        }
        List<LabLootPoolValues> pools = values.pools();
        for (int p = 0; p < pools.size(); p++) {
            LabLootPoolValues pool = pools.get(p);
            List<LabLootEntryValues> entries = pool.entries();
            for (int e = 0; e < entries.size(); e++) {
                LabLootEntryValues entry = entries.get(e);
                if (isBlankEntry(entry)) {
                    continue;
                }
                drops.add(new DropSlot(p, e, entry, pool));
            }
        }
        scrollMax = Math.max(0, contentHeight() - gridHeight());
        scroll = Math.max(0, Math.min(oldScroll, scrollMax));
    }

    private static boolean isBlankEntry(LabLootEntryValues entry) {
        if (entry == null) {
            return true;
        }
        return switch (entry.type()) {
            case "tag" -> entry.tag() == null || entry.tag().isBlank();
            case "loot_table" -> entry.lootTable() == null || entry.lootTable().isBlank();
            case "empty" -> false;
            default -> entry.item() == null || entry.item().isBlank();
        };
    }

    private int gridX() {
        return TARGET_BOX + PAD + GAP;
    }

    private int gridW() {
        return Math.max(1, getSizeWidth() - gridX() - PAD);
    }

    private int gridHeight() {
        return Math.max(1, getSizeHeight() - PAD * 2 - (overflowText().isBlank() ? 0 : FOOTER_H));
    }

    private String overflowText() {
        if (values == null) {
            return "";
        }
        int pools = values.droppedPools();
        int entries = values.droppedEntries();
        if (pools > 0 && entries > 0) {
            return I18n.get(LabGuiKeys.LAB_LOOT_PREVIEW_OVERFLOW_BOTH, pools, entries);
        }
        if (pools > 0) {
            return I18n.get(LabGuiKeys.LAB_LOOT_PREVIEW_OVERFLOW_POOLS, pools);
        }
        if (entries > 0) {
            return I18n.get(LabGuiKeys.LAB_LOOT_PREVIEW_OVERFLOW_ENTRIES, entries);
        }
        return "";
    }

    private int columns() {
        return Math.max(1, (gridW() + GAP) / (SLOT + GAP));
    }

    private int contentHeight() {
        int rows = (drops.size() + columns() - 1) / columns();
        return Math.max(1, rows * SLOT + Math.max(0, rows - 1) * GAP);
    }

    private int slotX(int col) {
        return getPositionX() + gridX() + col * (SLOT + GAP);
    }

    private int slotY(int row) {
        return getPositionY() + PAD - scroll + row * (SLOT + GAP);
    }

    private int dropAt(double mx, double my) {
        int cols = columns();
        for (int i = 0; i < drops.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int x = slotX(col);
            int y = slotY(row);
            if (mx >= x && my >= y && mx < x + SLOT && my < y + SLOT) {
                int gridTop = getPositionY() + PAD;
                int gridBottom = gridTop + gridHeight();
                if (my < gridTop || my >= gridBottom) {
                    return -1;
                }
                return i;
            }
        }
        return -1;
    }

    private boolean overTarget(double mx, double my) {
        int x = getPositionX() + PAD;
        int y = getPositionY() + PAD;
        return mx >= x && my >= y && mx < x + TARGET_BOX && my < y + TARGET_BOX;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        drawTarget(g, mx, my);
        int cols = columns();
        int gridTop = getPositionY() + PAD;
        int gridBottom = gridTop + gridHeight();
        g.flush();
        g.enableScissor(getPositionX() + gridX(), gridTop,
                getPositionX() + gridX() + gridW(), gridBottom);
        for (int i = 0; i < drops.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int x = slotX(col);
            int y = slotY(row);
            if (y + SLOT < gridTop || y > gridBottom) {
                continue;
            }
            SlotWidget.ITEM_SLOT_TEXTURE.draw(g, mx, my, x, y, SLOT, SLOT);
            ItemStack icon = dropIcon(drops.get(i).entry());
            if (!icon.isEmpty()) {
                new ItemStackTexture(icon).draw(g, mx, my, x + 1, y + 1, SLOT - 2, SLOT - 2);
            }
        }
        g.flush();
        g.disableScissor();
        String overflow = overflowText();
        if (!overflow.isBlank()) {
            overflowTexture(overflow).draw(g, mx, my, getPositionX() + gridX(),
                    getPositionY() + getSizeHeight() - PAD - FOOTER_H + 1, gridW(), FOOTER_H);
        }
        int hovered = dropAt(mx, my);
        if (hovered >= 0) {
            int row = hovered / cols;
            int col = hovered % cols;
            LabGlow.drawGlow(g, mx, my, slotX(col), slotY(row), SLOT, SLOT);
            setHoverTooltips(dropTips(drops.get(hovered)));
        } else if (overTarget(mx, my)) {
            LabGlow.drawGlow(g, mx, my, getPositionX() + PAD, getPositionY() + PAD, TARGET_BOX, TARGET_BOX);
            setHoverTooltips(targetTips());
        } else {
            setHoverTooltips(List.of());
        }
    }

    private void drawTarget(GuiGraphics g, int mx, int my) {
        int x = getPositionX() + PAD;
        int y = getPositionY() + PAD;
        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType) && targetEgg().isEmpty()) {
            if (entryId != null && renderEntity(g, x + TARGET_BOX / 2, y + TARGET_BOX / 2,
                    TARGET_BOX, TARGET_BOX, entryId)) {
                return;
            }
        }
        SlotWidget.ITEM_SLOT_TEXTURE.draw(g, mx, my, x, y, TARGET_BOX, TARGET_BOX);
        ItemStack target = targetEgg().isEmpty() ? stackFor() : targetEgg();
        if (!target.isEmpty()) {
            new ItemStackTexture(target).draw(g, mx, my, x + 2, y + 2, TARGET_BOX - 4, TARGET_BOX - 4);
        }
    }

    private ItemStack targetEgg() {
        if (!LabLootService.LOOT_TYPE_ENTITY.equals(lootType) || entryId == null) {
            return ItemStack.EMPTY;
        }
        ResourceLocation eggId = ResourceLocation.tryBuild(entryId.getNamespace(), entryId.getPath() + "_spawn_egg");
        if (eggId != null && BuiltInRegistries.ITEM.containsKey(eggId)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(eggId));
        }
        return ItemStack.EMPTY;
    }

    private List<Component> targetTips() {
        if (entryId != null) {
            return List.of(Component.literal(entryId.toString()));
        }
        if (values != null && !values.targetId().isBlank()) {
            return List.of(Component.literal(values.targetId()));
        }
        return List.of(Component.literal(lootType));
    }

    private TextTexture overflowTexture(String text) {
        return new TextTexture(text, LabColors.TEXT_MUTED)
                .setType(TextTexture.TextType.LEFT)
                .setWidth(gridW());
    }

    private static ItemStack dropIcon(LabLootEntryValues entry) {
        if (entry == null) {
            return ItemStack.EMPTY;
        }
        return switch (entry.type()) {
            case "tag" -> {
                if (entry.tag().isBlank()) {
                    yield ItemStack.EMPTY;
                }
                ResourceLocation tagId = ResourceLocation.tryParse(entry.tag());
                yield tagId == null ? ItemStack.EMPTY : LabPickerEntries.tagPreview(tagId);
            }
            case "loot_table" -> new ItemStack(Items.CHEST);
            case "empty" -> new ItemStack(Items.BARRIER);
            case "dynamic" -> new ItemStack(Items.BUNDLE);
            default -> {
                if (entry.item().isBlank()) {
                    yield ItemStack.EMPTY;
                }
                ResourceLocation id = ResourceLocation.tryParse(entry.item());
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                    yield ItemStack.EMPTY;
                }
                yield new ItemStack(BuiltInRegistries.ITEM.get(id));
            }
        };
    }

    private static List<Component> dropTips(DropSlot slot) {
        List<Component> tips = new ArrayList<>();
        LabLootEntryValues entry = slot.entry();
        LabLootPoolValues pool = slot.pool();
        ItemStack icon = dropIcon(entry);
        String name = icon.isEmpty() ? entryName(entry) : icon.getHoverName().getString();
        tips.add(Component.literal(name).withStyle(ChatFormatting.WHITE));
        String idLine = entryIdLine(entry);
        if (!idLine.isBlank()) {
            tips.add(Component.literal(idLine).withStyle(ChatFormatting.GRAY));
        }
        tips.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_POOL_ENTRY,
                slot.poolIndex() + 1, slot.entryIndex() + 1).withStyle(ChatFormatting.YELLOW));
        if (entry.alternativeGroup() > 0) {
            tips.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_GROUP, entry.alternativeGroup())
                    .withStyle(ChatFormatting.YELLOW));
        }
        tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_WEIGHT, Component.literal(Integer.toString(entry.weight()))));
        tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_QUALITY, Component.literal(Integer.toString(entry.quality()))));
        tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_COUNT, Component.literal(countLine(entry))));
        if (!entry.toolRequirement().isBlank()) {
            tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_REQUIRES,
                    Component.translatable("enchantment.minecraft." + entry.toolRequirement())));
        }
        if (entry.entryKilledByPlayer()) {
            tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_REQUIRES,
                    Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_KILL)));
        }
        if (entry.entryChance() < 1f) {
            String percent = Math.round(entry.entryChance() * 100f) + "%";
            Component chance = entry.entryChanceLooting() > 0f
                    ? Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_ENTRY_CHANCE_LOOTING, percent,
                            formatCount(entry.entryChanceLooting() * 100f) + "%")
                    : Component.literal(percent);
            tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_ENTRY_CHANCE, chance));
        }
        if (entry.fortuneBonus()) {
            tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_FORTUNE,
                    Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_ORE_BONUS)));
        }
        if (entry.explosionDecay()) {
            tips.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_EXPLOSION)
                    .withStyle(ChatFormatting.WHITE));
        }
        tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_CHANCE,
                Component.literal(Math.round(pool.randomChance() * 100f) + "%")));
        tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_ROLLS, rollsValue(pool)));
        if (entry.lootBonusMax() > 0f) {
            tips.add(statLine(LabGuiKeys.LAB_LOOT_PREVIEW_LOOTING_BONUS,
                    Component.literal("+" + lootBonusLine(entry))));
        }
        Component conditions = conditionsLine(pool);
        if (conditions != null) {
            tips.add(conditions.copy().withStyle(ChatFormatting.GOLD));
        }
        for (String note : pool.poolConditionNotes()) {
            Component resolved = LabLootNoteText.resolve(note);
            if (resolved != null) {
                tips.add(resolved.copy().withStyle(ChatFormatting.GOLD));
            }
        }
        for (String note : entry.conditionNotes()) {
            Component resolved = LabLootNoteText.resolve(note);
            if (resolved != null) {
                tips.add(resolved.copy().withStyle(ChatFormatting.GOLD));
            }
        }
        tips.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_CLICK_EDIT)
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        return tips;
    }

    private static String lootBonusLine(LabLootEntryValues entry) {
        if (entry.lootBonusMin() == entry.lootBonusMax()) {
            return formatCount(entry.lootBonusMax());
        }
        return formatCount(entry.lootBonusMin()) + "-" + formatCount(entry.lootBonusMax());
    }

    private static Component statLine(String labelKey, Component value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
                .append(value.copy().withStyle(ChatFormatting.WHITE));
    }

    private static String entryName(LabLootEntryValues entry) {
        return switch (entry.type()) {
            case "tag" -> entry.tag().isBlank() ? "tag" : "#" + entry.tag();
            case "loot_table" -> entry.lootTable().isBlank() ? "loot table" : entry.lootTable();
            case "empty" -> "empty";
            default -> entry.item().isBlank() ? "item" : entry.item();
        };
    }

    private static String entryIdLine(LabLootEntryValues entry) {
        return switch (entry.type()) {
            case "tag" -> entry.tag();
            case "loot_table" -> entry.lootTable();
            case "empty" -> "";
            default -> entry.item();
        };
    }

    private static String countLine(LabLootEntryValues entry) {
        if ("uniform".equals(entry.countType())) {
            return formatCount(entry.countMin()) + "-" + formatCount(entry.countMax());
        }
        return formatCount(entry.countValue());
    }

    private static Component rollsValue(LabLootPoolValues pool) {
        Component base = switch (pool.rollsType()) {
            case "uniform" -> Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_ROLLS_RANGE,
                    formatCount(pool.rollsMin()), formatCount(pool.rollsMax()));
            case "binomial" -> Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_ROLLS_BINOMIAL,
                    pool.rollsN(), formatCount(pool.rollsP()));
            default -> Component.literal(formatCount(pool.rollsValue()));
        };
        if (pool.bonusRolls() <= 0f) {
            return base;
        }
        return base.copy().append(Component.literal(" ")).append(Component.translatable(
                LabGuiKeys.LAB_LOOT_PREVIEW_ROLLS_BONUS, formatCount(pool.bonusRolls())));
    }

    @Nullable
    private static Component conditionsLine(LabLootPoolValues pool) {
        List<Component> parts = new ArrayList<>();
        if (pool.killedByPlayer()) {
            parts.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_COND_KILLED));
        }
        if (pool.furnaceSmelt()) {
            parts.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_COND_SMELTED));
        }
        if (pool.lootingEnchant()) {
            parts.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_COND_LOOTING));
        }
        if (!pool.survivesExplosion()) {
            parts.add(Component.translatable(LabGuiKeys.LAB_LOOT_PREVIEW_COND_NO_GUARD));
        }
        if (parts.isEmpty()) {
            return null;
        }
        MutableComponent out = parts.get(0).copy();
        for (int i = 1; i < parts.size(); i++) {
            out.append(Component.literal(", ")).append(parts.get(i));
        }
        return out;
    }

    private static String formatCount(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == LabColors.MOUSE_BUTTON_LEFT && dropClickListener != null) {
            int hovered = dropAt(mx, my);
            if (hovered >= 0) {
                DropSlot slot = drops.get(hovered);
                dropClickListener.onDropClick(slot.poolIndex(), slot.entryIndex());
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY) || scrollMax <= 0) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        int step = Math.max(SLOT + GAP, (SLOT + GAP) * 2);
        int next = wheel(scroll, scrollMax, step, wheelDelta);
        if (next != scroll) {
            scroll = next;
        }
        return true;
    }

    private static int wheel(int value, int max, int step, double delta) {
        int next = value - (int) Math.signum(delta) * step;
        return Math.max(0, Math.min(max, next));
    }

    private ItemStack stackFor() {
        if (entryId != null && LabLootService.LOOT_TYPE_BLOCK.equals(lootType)) {
            Block block = BuiltInRegistries.BLOCK.get(entryId);
            if (block != null) {
                return new ItemStack(block);
            }
        }
        if (LabLootService.LOOT_TYPE_FISHING.equals(lootType)) {
            return new ItemStack(Items.FISHING_ROD);
        }
        if (LabLootService.LOOT_TYPE_GIFT.equals(lootType)) {
            return new ItemStack(Items.EMERALD);
        }
        return new ItemStack(Items.CHEST);
    }

    public static boolean renderEntity(GuiGraphics g, int centerX, int centerY, int boxW, int boxH,
            ResourceLocation entityId) {
        if (entityId == null || Minecraft.getInstance().level == null) {
            return false;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (type == null) {
            return false;
        }
        Entity entity = type.create(Minecraft.getInstance().level);
        if (entity == null) {
            return false;
        }
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        entity.yRotO = 0.0F;
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0.0F;
            living.yBodyRotO = 0.0F;
            living.yHeadRot = 0.0F;
            living.yHeadRotO = 0.0F;
        }
        double scale = Math.max(1.0D, Math.min(MAX_SCALE,
                Math.min(boxW / Math.max(0.25D, entity.getBbWidth()), boxH / Math.max(0.25D, entity.getBbHeight()))
                        * FILL));
        renderEntityInInventory(g, centerX, centerY, scale, entity, FRONT_ENTITY_YAW, 0.0F);
        return true;
    }

    @SuppressWarnings("deprecation")
    private static void renderEntityInInventory(GuiGraphics g, int x, int y, double scale, Entity entity, float yawDegrees, float partialTicks) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        Quaternionf rotation = new Quaternionf().rotateXYZ(0.0F, (float) Math.toRadians(yawDegrees), (float) Math.PI);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0D);
        g.pose().mulPoseMatrix(new Matrix4f().scaling((float) scale, (float) scale, (float) -scale));
        g.pose().mulPose(rotation);
        g.pose().translate(0.0D, -entity.getBbHeight() / 2.0D, 0.0D);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(new Quaternionf());
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, g.pose(), g.bufferSource(), 15728880));
        g.flush();
        dispatcher.setRenderShadow(true);
        g.pose().popPose();
        Lighting.setupFor3DItems();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }
}

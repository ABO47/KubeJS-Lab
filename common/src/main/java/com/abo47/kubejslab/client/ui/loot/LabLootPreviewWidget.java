package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabGlow;
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
        return Math.max(1, getSizeHeight() - PAD * 2);
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
        tips.add(Component.literal("Pool " + (slot.poolIndex() + 1) + " - Entry " + (slot.entryIndex() + 1))
                .withStyle(ChatFormatting.YELLOW));
        tips.add(statLine("Weight: ", Integer.toString(entry.weight())));
        tips.add(statLine("Quality: ", Integer.toString(entry.quality())));
        tips.add(statLine("Count: ", countLine(entry)));
        tips.add(statLine("Chance: ", Math.round(pool.randomChance() * 100f) + "%"));
        tips.add(statLine("Rolls: ", rollsLine(pool)));
        String conditions = conditionsLine(pool);
        if (!conditions.isBlank()) {
            tips.add(Component.literal(conditions).withStyle(ChatFormatting.GOLD));
        }
        tips.add(Component.literal("Click to edit").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        return tips;
    }

    private static Component statLine(String label, String value) {
        return Component.literal(label).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
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

    private static String rollsLine(LabLootPoolValues pool) {
        return switch (pool.rollsType()) {
            case "uniform" -> formatCount(pool.rollsMin()) + "-" + formatCount(pool.rollsMax());
            case "binomial" -> "n=" + pool.rollsN() + " p=" + pool.rollsP();
            default -> formatCount(pool.rollsValue());
        };
    }

    private static String conditionsLine(LabLootPoolValues pool) {
        List<String> parts = new ArrayList<>();
        if (pool.killedByPlayer()) {
            parts.add("killed by player");
        }
        if (pool.furnaceSmelt()) {
            parts.add("smelted");
        }
        if (pool.lootingEnchant()) {
            parts.add("looting");
        }
        if (!pool.survivesExplosion()) {
            parts.add("no explosion guard");
        }
        return String.join(", ", parts);
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

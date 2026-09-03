package com.abo47.kubejslab.client.ui.loot;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootPreviewWidget extends WidgetGroup {
    private static final int FRONT_ENTITY_YAW = 205;
    private static final double FILL = 0.82D;
    private static final double MAX_SCALE = 96.0D;

    private ResourceLocation entryId;
    private String lootType = LabLootService.LOOT_TYPE_BLOCK;
    private long animTick;

    public LabLootPreviewWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
        lootType = LabLootService.LOOT_TYPE_BLOCK;
    }

    public void setEntry(ResourceLocation id, String lootType) {
        this.entryId = id;
        this.lootType = lootType == null || lootType.isBlank() ? LabLootService.LOOT_TYPE_BLOCK : lootType;
        animTick = System.nanoTime();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics g, int mx, int my, float pt) {
        int px = getPositionX();
        int py = getPositionY();
        int pw = getSizeWidth();
        int ph = getSizeHeight();

        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            drawEntity(g, px, py, pw, ph);
            return;
        }
        int size = Math.max(32, Math.min(pw, ph) - 16);
        int iconX = px + (pw - size) / 2;
        int iconY = py + (ph - size) / 2;
        new ItemStackTexture(stackFor()).draw(g, mx, my, iconX, iconY, size, size);
    }

    private ItemStack stackFor() {
        if (entryId != null && LabLootService.LOOT_TYPE_BLOCK.equals(lootType)) {
            Block block = BuiltInRegistries.BLOCK.get(entryId);
            if (block != null) {
                return new ItemStack(block);
            }
        }
        return new ItemStack(Items.CHEST);
    }

    private void drawEntity(GuiGraphics g, int px, int py, int pw, int ph) {
        if (entryId == null || Minecraft.getInstance().level == null) {
            int size = Math.max(32, Math.min(pw, ph) - 16);
            new ItemStackTexture(new ItemStack(Items.ZOMBIE_SPAWN_EGG)).draw(g, 0, 0, px + (pw - size) / 2,
                    py + (ph - size) / 2, size, size);
            return;
        }
        renderEntity(g, px + pw / 2, py + ph / 2, pw, ph, entryId);
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
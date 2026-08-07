package com.abo47.kubejslab.client.ui.machines;

import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.platform.Services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


final class LabGridLayoutBuilder {
    private final LabMachineLayoutWidget widget;

    LabGridLayoutBuilder(LabMachineLayoutWidget widget) {
        this.widget = widget;
    }

    void build(Map<Long, LabSlotData> snapshot) {
        int w = widget.effectiveGridWidth();
        int h = widget.effectiveGridHeight();
        int ox = LabLayout.MACHINE_PAD;
        int oy = Math.max(LabLayout.MACHINE_PAD, (widget.getSizeHeight() - h * 18) / 2);

        Recipe<?> original = widget.original();
        KubeJSLab.LOGGER.info("[MechCrafting] rebuildGrid: grid {}x{}, snapshot cells={}, original={}", w, h,
                snapshot.size(), original == null ? "null" : original.getId());

        for (int gy = 0; gy < h; gy++) {
            for (int gx = 0; gx < w; gx++) {
                LabSlotData data = snapshot.get(gridKey(gx, gy));
                if (data == null) {
                    data = new LabSlotData();
                }
                if (data.isEmpty() && original != null) {
                    Ingredient ingredient = gridIngredientAt(original, gy, gx);
                    if (!ingredient.isEmpty()) {
                        data.setItemValue(firstIngredientStack(ingredient));
                        JsonElement json = ingredient.toJson();
                        if (json != null && json.isJsonObject()) {
                            JsonObject object = json.getAsJsonObject();
                            if (object.has("tag")) {
                                data.setTagValue(new ResourceLocation(object.get("tag").getAsString()));
                            }
                        }
                        KubeJSLab.LOGGER.info("[MechCrafting] cell ({},{}) loaded from original: {}", gy, gx, json);
                    }
                }
                LabPhantomHandler handler = new LabPhantomHandler(data);
                LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0, ox + gx * 18, oy + gy * 18);
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(widget);
                slot.setRole(RecipeIngredientRole.INPUT);
                widget.addWidget(slot);
                widget.addSlotPair(new LabSlotPair(data, null, RecipeIngredientRole.INPUT, gx, gy));
            }
        }

        IRecipeLayoutDrawable<?> source = widget.jeiLayout();
        if (source == null) {
            source = widget.sampleLayout();
        }
        if (source != null) {
            for (IRecipeSlotView view : source.getRecipeSlotsView().getSlotViews()) {
                if (view.getRole() != RecipeIngredientRole.OUTPUT || !(view instanceof IRecipeSlotDrawable drawable)) {
                    continue;
                }
                Rect2i rect;
                try {
                    rect = drawable.getRect();
                } catch (RuntimeException | LinkageError ignored) {
                    continue;
                }
                LabSlotData data = new LabSlotData();
                boolean fluidSlot = widget.entry() != null
                        && Services.platform().readFluidIngredient(view).map(data::setFluidValue).orElse(false);
                if (fluidSlot) {
                    LabPhantomFluidSlotWidget slot = new LabPhantomFluidSlotWidget(data, ox + rect.getX(), oy + rect.getY());
                    slot.setClientSideWidget();
                    slot.setDragOwner(widget);
                    slot.setRole(RecipeIngredientRole.OUTPUT);
                    widget.addWidget(slot);
                } else {
                    ItemStack stack = ItemStack.EMPTY;
                    if (widget.entry() != null) {
                        for (ItemStack s : view.getIngredients(VanillaTypes.ITEM_STACK).toList()) {
                            stack = s;
                            break;
                        }
                    }
                    data.setItemValue(stack);
                    LabPhantomHandler handler = new LabPhantomHandler(data);
                    LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0, ox + rect.getX(), oy + rect.getY());
                    slot.setClearSlotOnRightClick(true);
                    slot.setClientSideWidget();
                    slot.setDragOwner(widget);
                    slot.setRole(RecipeIngredientRole.OUTPUT);
                    widget.addWidget(slot);
                }
                widget.addSlotPair(new LabSlotPair(data, view, RecipeIngredientRole.OUTPUT, (rect.getX() - 1) / 18,
                        (rect.getY() - 1) / 18));
            }
        }
        widget.notifyOutputsChanged();
    }

    static long gridKey(int gx, int gy) {
        return (long) gx << 32 | gy;
    }

    private static Ingredient gridIngredientAt(Recipe<?> original, int row, int col) {
        if (original == null) {
            return Ingredient.EMPTY;
        }
        List<Ingredient> ingredients = original.getIngredients();
        if (ingredients.isEmpty()) {
            return Ingredient.EMPTY;
        }
        if (original instanceof ShapedRecipe shaped) {
            int width = Math.max(1, Math.min(9, shaped.getWidth()));
            int height = Math.max(1, Math.min(9, shaped.getHeight()));
            if (col >= width || row >= height) {
                return Ingredient.EMPTY;
            }
            int index = row * width + col;
            Ingredient result = index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
            if (!result.isEmpty()) {
                KubeJSLab.LOGGER.debug("[MechCrafting] gridIngredientAt({},{}): width={}, index={} -> {}", row, col, width,
                        index, result.toJson());
            }
            return result;
        }
        int index = row * 3 + col;
        Ingredient result = index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
        if (!result.isEmpty()) {
            KubeJSLab.LOGGER.debug("[MechCrafting] gridIngredientAt({},{}): flat index={} -> {} (from {})", row, col,
                    index, result.toJson(), original.getClass().getSimpleName());
        }
        return result;
    }

    private static ItemStack firstIngredientStack(Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
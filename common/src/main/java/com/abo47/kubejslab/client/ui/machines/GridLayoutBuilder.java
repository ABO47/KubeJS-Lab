package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.theme.UiLayout;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.model.SlotTint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


final class GridLayoutBuilder {
    private final MachineLayoutWidget widget;

    GridLayoutBuilder(MachineLayoutWidget widget) {
        this.widget = widget;
    }

    void build(Map<Long, SlotData> snapshot) {
        int w = widget.effectiveGridWidth();
        int h = widget.effectiveGridHeight();
        int ox = 0;
        int oy = Math.max(UiLayout.MACHINE_PAD, (widget.getSizeHeight() - h * 18) / 2);

        Recipe<?> original = widget.original();

        for (int gy = 0; gy < h; gy++) {
            for (int gx = 0; gx < w; gx++) {
                SlotData data = snapshot.get(gridKey(gx, gy));
                if (data == null) {
                    data = new SlotData();
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
                    }
                }
                PhantomHandler handler = new PhantomHandler(data);
                PaintSlotWidget slot = new PaintSlotWidget(handler, 0, ox + gx * 18, oy + gy * 18);
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(widget);
                slot.setRole(RecipeIngredientRole.INPUT);
                widget.addWidget(slot);
                widget.addSlotPair(new SlotPair(data, null, RecipeIngredientRole.INPUT, gx, gy, SlotTint.NORMAL));
            }
        }

        IRecipeLayoutDrawable<?> source = widget.jeiLayout();
        if (source == null) {
            source = widget.sampleLayout();
        }
        if (source != null) {
            List<IRecipeSlotView> outputViews = new ArrayList<>();
            int minCol = Integer.MAX_VALUE;
            int maxCol = -1;
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
                outputViews.add(view);
                int col = (rect.getX() - 1) / 18;
                minCol = Math.min(minCol, col);
                maxCol = Math.max(maxCol, col);
            }
            int blockCols = maxCol >= minCol ? Math.min(maxCol - minCol + 1, UiLayout.MACHINE_COLS) : 0;
            int outputOX = ox + (UiLayout.MACHINE_COLS - blockCols) * 18;
            for (IRecipeSlotView view : outputViews) {
                Rect2i rect = ((IRecipeSlotDrawable) view).getRect();
                int x = outputOX + ((rect.getX() - 1) / 18 - minCol) * 18;
                SlotData data = new SlotData();
                boolean fluidSlot = Services.platform().readFluidIngredient(view).map(data::setFluidValue).orElse(false);
                if (fluidSlot) {
                    PhantomFluidSlotWidget slot = new PhantomFluidSlotWidget(data, x, oy + rect.getY());
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
                    PhantomHandler handler = new PhantomHandler(data);
                    PaintSlotWidget slot = new PaintSlotWidget(handler, 0, x, oy + rect.getY());
                    slot.setClearSlotOnRightClick(true);
                    slot.setClientSideWidget();
                    slot.setDragOwner(widget);
                    slot.setRole(RecipeIngredientRole.OUTPUT);
                    widget.addWidget(slot);
                }
                widget.addSlotPair(new SlotPair(data, view, RecipeIngredientRole.OUTPUT, (rect.getX() - 1) / 18,
                        (rect.getY() - 1) / 18, SlotTint.NORMAL));
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
            return index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
        }
        int index = row * 3 + col;
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
    }

    private static ItemStack firstIngredientStack(Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
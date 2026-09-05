package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.picker.Pick;
import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.RecipeSettingsWidget;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotTint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


public final class MachineLayoutWidget extends WidgetGroup {
    private MachineView machine;
    private RecipeIndex.RecipeEntry entry;
    private IRecipeLayoutDrawable<?> jeiLayout;
    private IRecipeLayoutDrawable<?> sampleLayout;
    private final List<SlotPair> slotPairs = new ArrayList<>();
    private final PaintController paint = new PaintController(
            () -> gui == null ? null : gui.getModularUIContainer(), this::notifyOutputsChanged);
    private Runnable outputsChangedListener;
    private int gridWidth = 3;
    private int gridHeight = 3;

    public MachineLayoutWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setGridSize(int width, int height) {
        RecipeHandler support = support();
        if (support == null || !support.supportsGridSize()) {
            return;
        }
        int w = Math.max(1, Math.min(9, width));
        int h = Math.max(1, Math.min(9, height));
        if (gridWidth == w && gridHeight == h) {
            return;
        }
        gridWidth = w;
        gridHeight = h;
        rebuild();
    }

    int effectiveGridWidth() {
        RecipeHandler support = support();
        return support != null && support.supportsGridSize() ? Math.max(1, gridWidth) : 3;
    }

    int effectiveGridHeight() {
        RecipeHandler support = support();
        return support != null && support.supportsGridSize() ? Math.max(1, gridHeight) : 3;
    }

    public void setMachine(MachineView machine) {
        if (this.machine != machine) {
            this.gridWidth = 3;
            this.gridHeight = 3;
            this.slotPairs.clear();
            paint.setPendingPick(null);
        }
        this.machine = machine;
        this.entry = null;
        this.jeiLayout = null;
        this.sampleLayout = machine == null ? null : JeiLayoutFinder.findSampleLayout(machine);
        rebuild();
    }

    public void showRecipe(RecipeIndex.RecipeEntry entry) {
        this.entry = entry;
        this.jeiLayout = entry == null ? null : JeiLayoutFinder.findJeiLayout(machine, entry);
        this.slotPairs.clear();
        rebuild();
    }

    public void setPendingPick(Pick pendingPick) {
        paint.setPendingPick(pendingPick);
    }

    public void clearPendingPick() {
        paint.clearPendingPick();
    }

    public void setOutputsChangedListener(Runnable outputsChangedListener) {
        this.outputsChangedListener = outputsChangedListener;
    }

    public List<RecipeIngredient> getInputs() {
        List<SlotPair> inputs = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.INPUT && !pair.data().isEmpty()
                    && pair.tint() != SlotTint.BLUEPRINT && pair.tint() != SlotTint.MOLD) {
                inputs.add(pair);
            }
        }
        RecipeHandler support = support();
        if (support != null && support.gridLayout()) {
            int width = effectiveGridWidth();
            int height = effectiveGridHeight();
            RecipeIngredient[] cells = new RecipeIngredient[width * height];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new RecipeIngredient.Item(ItemStack.EMPTY);
            }
            for (SlotPair pair : inputs) {
                if (pair.gx() >= 0 && pair.gx() < width && pair.gy() >= 0 && pair.gy() < height) {
                    cells[pair.gy() * width + pair.gx()] = pair.data().toIngredient();
                }
            }
            return List.of(cells);
        }
        if (support != null && isFixedLayout(support)) {
            List<RecipeIngredient> ordered = new ArrayList<>(inputs.size());
            for (SlotPair pair : inputs) {
                ordered.add(pair.data().toIngredient());
            }
            return ordered;
        }
        inputs.sort((SlotPair a, SlotPair b) -> {
            int row = Integer.compare(a.gy(), b.gy());
            return row != 0 ? row : Integer.compare(a.gx(), b.gx());
        });
        List<RecipeIngredient> ordered = new ArrayList<>(inputs.size());
        for (SlotPair pair : inputs) {
            ordered.add(pair.data().toIngredient());
        }
        return ordered;
    }

    public List<SurfaceSlot> surfaceSlots() {
        List<SurfaceSlot> surfaces = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.tint() == SlotTint.BLUEPRINT) {
                surfaces.add(new SurfaceSlot(pair.tint(), blueprintCategoryOf(pair)));
            } else if (pair.tint() == SlotTint.MOLD) {
                surfaces.add(new SurfaceSlot(pair.tint(), moldOf(pair)));
            }
        }
        return surfaces;
    }

    private static String blueprintCategoryOf(SlotPair pair) {
        ItemStack stack = pair.data().stack;
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return "";
        }
        String category = stack.getTag().getString("blueprint");
        return category == null ? "" : category;
    }

    private static String moldOf(SlotPair pair) {
        ItemStack stack = pair.data().stack;
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    public List<RecipeOutput> getOutputs() {
        List<RecipeOutput> outputs = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.OUTPUT) {
                RecipeOutput output = pair.data().toOutput();
                if (output != null) {
                    outputs.add(output);
                }
            }
        }
        return outputs;
    }

    public List<RecipeSettingsWidget.OutputRow> getOutputRows() {
        RecipeHandler support = support();
        if (support == null || !support.supportsChance()) {
            return List.of();
        }
        List<RecipeSettingsWidget.OutputRow> rows = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.OUTPUT
                    && pair.data().kind == SlotKind.ITEM && !pair.data().stack.isEmpty()) {
                SlotData data = pair.data();
                rows.add(new RecipeSettingsWidget.OutputRow(data.stack,
                        () -> data.chance,
                        value -> data.setChance(value)));
            }
        }
        return rows;
    }

    public void clearPhantoms() {
        for (SlotPair pair : slotPairs) {
            pair.data().clear();
        }
        paint.setPendingPick(null);
        notifyOutputsChanged();
    }

    private void rebuild() {
        RecipeHandler support = support();
        if (support != null && support.gridLayout()) {
            Map<Long, SlotData> snapshot = new HashMap<>();
            for (SlotPair pair : slotPairs) {
                if (pair.role() == RecipeIngredientRole.INPUT && pair.gx() >= 0 && pair.gy() >= 0) {
                    snapshot.put(GridLayoutBuilder.gridKey(pair.gx(), pair.gy()), pair.data());
                }
            }
            clearAllWidgets();
            slotPairs.clear();
            new GridLayoutBuilder(this).build(snapshot);
            return;
        }
        if (support != null && isFixedLayout(support)) {
            clearAllWidgets();
            slotPairs.clear();
            new FixedLayoutBuilder(this).build(support);
            return;
        }
        clearAllWidgets();
        slotPairs.clear();
    }

    private static boolean isFixedLayout(RecipeHandler support) {
        return !support.inputSlots().isEmpty() || !support.outputSlots().isEmpty();
    }

    MachineView machine() {
        return machine;
    }

    RecipeIndex.RecipeEntry entry() {
        return entry;
    }

    IRecipeLayoutDrawable<?> jeiLayout() {
        return jeiLayout;
    }

    IRecipeLayoutDrawable<?> sampleLayout() {
        return sampleLayout;
    }

    Recipe<?> original() {
        return entry == null ? null : RecipeIndex.recipeById(entry.id());
    }

    private RecipeHandler support() {
        return machine == null ? null : MachineRegistry.get(machine.recipeTypeUid());
    }

    void addSlotPair(SlotPair pair) {
        slotPairs.add(pair);
    }

    void notifyOutputsChanged() {
        if (outputsChangedListener != null) {
            outputsChangedListener.run();
        }
    }

    void applyIngredientKind(SlotData data, Recipe<?> original, int index, boolean input) {
        if (!input || original == null) {
            return;
        }
        RecipeHandler support = support();
        ResourceLocation ieTag = support == null ? null : support.tagForInput(original, index);
        if (ieTag != null) {
            data.setTagValue(ieTag);
            return;
        }
        List<Ingredient> ingredients = original.getIngredients();
        if (index >= ingredients.size()) {
            return;
        }
        Ingredient ingredient = ingredients.get(index);
        JsonElement json = ingredient.toJson();
        if (json != null && json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("tag")) {
                data.setTagValue(new ResourceLocation(object.get("tag").getAsString()));
            }
        }
    }

    void applyChance(SlotData data, List<?> rollable, List<Float> ieChances, int index) {
        if (!ieChances.isEmpty() && index < ieChances.size()) {
            data.setChance(ieChances.get(index));
            return;
        }
        if (index < rollable.size()) {
            Object obj = rollable.get(index);
            try {
                var m = obj.getClass().getMethod("getChance");
                Object v = m.invoke(obj);
                if (v instanceof Number n) data.setChance(n.floatValue());
            } catch (Throwable ignored) {
            }
        }
    }

    static List<?> rollableResults(Recipe<?> original) {
        if (original == null) return List.of();
        if (!Platform.isModLoaded("create")) return List.of();
        try {
            Class<?> clazz = Class.forName("com.simibubi.create.content.processing.recipe.ProcessingRecipe");
            if (clazz.isInstance(original)) {
                var m = clazz.getMethod("getRollableResults");
                Object res = m.invoke(original);
                if (res instanceof List<?> list) return list;
            }
        } catch (Throwable ignored) {
        }
        return List.of();
    }

    void beginPaint(int button) {
        paint.beginPaint(button);
    }

    boolean isPainting(int button) {
        return paint.isPainting(button);
    }

    void endPaint() {
        paint.endPaint();
    }

    void paintSlot(int button, SlotData data) {
        paint.paintSlot(button, data);
    }

    void adjustStackCount(SlotData data, int delta) {
        paint.adjustStackCount(data, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        if (paint.isPainting(button)) {
            paint.endPaint();
            handled = true;
        }
        return handled;
    }
}
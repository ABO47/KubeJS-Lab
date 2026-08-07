package com.abo47.kubejslab.client.ui.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeSettingsWidget;
import com.abo47.kubejslab.platform.Services;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;


public final class LabMachineLayoutWidget extends WidgetGroup {
    private LabMachine machine;
    private LabRecipeIndex.LabRecipeEntry entry;
    private IRecipeLayoutDrawable<?> jeiLayout;
    private IRecipeLayoutDrawable<?> sampleLayout;
    private final List<LabSlotPair> slotPairs = new ArrayList<>();
    private final LabPaintController paint = new LabPaintController(
            () -> gui == null ? null : gui.getModularUIContainer(), this::notifyOutputsChanged);
    private Runnable outputsChangedListener;
    private int gridWidth = 3;
    private int gridHeight = 3;

    public LabMachineLayoutWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setGridSize(int width, int height) {
        LabRecipeMachine support = support();
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
        LabRecipeMachine support = support();
        return support != null && support.supportsGridSize() ? Math.max(1, gridWidth) : 3;
    }

    int effectiveGridHeight() {
        LabRecipeMachine support = support();
        return support != null && support.supportsGridSize() ? Math.max(1, gridHeight) : 3;
    }

    public void setMachine(LabMachine machine) {
        if (this.machine != machine) {
            this.gridWidth = 3;
            this.gridHeight = 3;
            this.slotPairs.clear();
            paint.setPendingPick(null);
        }
        this.machine = machine;
        this.entry = null;
        this.jeiLayout = null;
        this.sampleLayout = machine == null ? null : LabJeiLayoutFinder.findSampleLayout(machine);
        rebuild();
    }

    public void showRecipe(LabRecipeIndex.LabRecipeEntry entry) {
        this.entry = entry;
        this.jeiLayout = entry == null ? null : LabJeiLayoutFinder.findJeiLayout(machine, entry);
        this.slotPairs.clear();
        rebuild();
    }

    public void setPendingPick(LabPick pendingPick) {
        paint.setPendingPick(pendingPick);
    }

    public void setOutputsChangedListener(Runnable outputsChangedListener) {
        this.outputsChangedListener = outputsChangedListener;
    }

    public List<LabIngredient> getInputs() {
        List<LabSlotPair> inputs = new ArrayList<>();
        for (LabSlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.INPUT && !pair.data().isEmpty()) {
                inputs.add(pair);
            }
        }
        LabRecipeMachine support = support();
        if (support != null && support.gridLayout()) {
            int width = effectiveGridWidth();
            int height = effectiveGridHeight();
            LabIngredient[] cells = new LabIngredient[width * height];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new LabIngredient.Item(ItemStack.EMPTY);
            }
            for (LabSlotPair pair : inputs) {
                if (pair.gx() >= 0 && pair.gx() < width && pair.gy() >= 0 && pair.gy() < height) {
                    cells[pair.gy() * width + pair.gx()] = pair.data().toIngredient();
                }
            }
            return List.of(cells);
        }
        inputs.sort((LabSlotPair a, LabSlotPair b) -> {
            int row = Integer.compare(a.gy(), b.gy());
            return row != 0 ? row : Integer.compare(a.gx(), b.gx());
        });
        List<LabIngredient> ordered = new ArrayList<>(inputs.size());
        for (LabSlotPair pair : inputs) {
            ordered.add(pair.data().toIngredient());
        }
        return ordered;
    }

    public List<LabRecipeOutput> getOutputs() {
        List<LabRecipeOutput> outputs = new ArrayList<>();
        for (LabSlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.OUTPUT) {
                LabRecipeOutput output = pair.data().toOutput();
                if (output != null) {
                    outputs.add(output);
                }
            }
        }
        return outputs;
    }

    public List<LabRecipeSettingsWidget.OutputRow> getOutputRows() {
        LabRecipeMachine support = support();
        if (support == null || !support.supportsChance()) {
            return List.of();
        }
        List<LabRecipeSettingsWidget.OutputRow> rows = new ArrayList<>();
        for (LabSlotPair pair : slotPairs) {
            if (pair.role() == RecipeIngredientRole.OUTPUT
                    && pair.data().kind == LabSlotKind.ITEM && !pair.data().stack.isEmpty()) {
                LabSlotData data = pair.data();
                rows.add(new LabRecipeSettingsWidget.OutputRow(data.stack,
                        () -> data.chance,
                        value -> data.setChance(value)));
            }
        }
        return rows;
    }

    public void clearPhantoms() {
        for (LabSlotPair pair : slotPairs) {
            pair.data().clear();
        }
        paint.setPendingPick(null);
        notifyOutputsChanged();
    }

    private void rebuild() {
        LabRecipeMachine support = support();
        if (support != null && support.gridLayout()) {
            Map<Long, LabSlotData> snapshot = new HashMap<>();
            for (LabSlotPair pair : slotPairs) {
                if (pair.role() == RecipeIngredientRole.INPUT && pair.gx() >= 0 && pair.gy() >= 0) {
                    snapshot.put(LabGridLayoutBuilder.gridKey(pair.gx(), pair.gy()), pair.data());
                }
            }
            clearAllWidgets();
            slotPairs.clear();
            new LabGridLayoutBuilder(this).build(snapshot);
            return;
        }
        if (support != null && isFixedLayout(support)) {
            clearAllWidgets();
            slotPairs.clear();
            new LabFixedLayoutBuilder(this).build(support);
            return;
        }
        clearAllWidgets();
        slotPairs.clear();
        buildGenericLayout();
    }

    private static boolean isFixedLayout(LabRecipeMachine support) {
        return !support.inputSlots().isEmpty() || !support.outputSlots().isEmpty();
    }

    private void buildGenericLayout() {
        IRecipeLayoutDrawable<?> source = jeiLayout != null ? jeiLayout : sampleLayout;
        int layoutW;
        int layoutH;
        if (source != null) {
            Rect2i rect = source.getRect();
            layoutW = rect.getWidth();
            layoutH = rect.getHeight();
        } else {
            layoutW = 0;
            layoutH = 0;
        }
        if (layoutW <= 0 || layoutH <= 0) {
            setBackground(IGuiTexture.EMPTY);
            return;
        }
        int ox = (getSizeWidth() - layoutW) / 2;
        int oy = (getSizeHeight() - layoutH) / 2;
        if (source == null) {
            return;
        }
        Recipe<?> original = original();
        List<ProcessingOutput> rollable = rollableResults(original);
        List<Float> ieChances = support() == null || support().outputChances(original).isEmpty()
                ? List.of()
                : support().outputChances(original);
        int itemInputIndex = 0;
        int itemOutputIndex = 0;
        for (IRecipeSlotView view : source.getRecipeSlotsView().getSlotViews()) {
            if (!(view instanceof IRecipeSlotDrawable drawable)
                    || view.getRole() == RecipeIngredientRole.RENDER_ONLY) {
                continue;
            }
            Rect2i rect;
            try {
                rect = drawable.getRect();
            } catch (RuntimeException | LinkageError ignored) {
                continue;
            }
            LabSlotData data = new LabSlotData();
            boolean fluidSlot = entry != null
                    && Services.platform().readFluidIngredient(view).map(data::setFluidValue).orElse(false);
            if (fluidSlot) {
                LabPhantomFluidSlotWidget slot = new LabPhantomFluidSlotWidget(data, ox + rect.getX(), oy + rect.getY());
                slot.setClientSideWidget();
                slot.setDragOwner(this);
                slot.setRole(view.getRole());
                addWidget(slot);
            } else {
                ItemStack stack = ItemStack.EMPTY;
                if (entry != null) {
                    for (ItemStack s : view.getIngredients(VanillaTypes.ITEM_STACK).toList()) {
                        stack = s;
                        break;
                    }
                }
                data.setItemValue(stack);
                applyIngredientKind(data, original, itemInputIndex, view.getRole() == RecipeIngredientRole.INPUT);
                if (view.getRole() == RecipeIngredientRole.INPUT) {
                    itemInputIndex++;
                }
                LabPhantomHandler handler = new LabPhantomHandler(data);
                LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0,
                        ox + rect.getX(), oy + rect.getY());
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(this);
                slot.setRole(view.getRole());
                addWidget(slot);
            }
            if (view.getRole() == RecipeIngredientRole.OUTPUT && !fluidSlot) {
                applyChance(data, rollable, ieChances, itemOutputIndex);
                itemOutputIndex++;
            }

            int gx = (rect.getX() - 1) / 18;
            int gy = (rect.getY() - 1) / 18;
            addSlotPair(new LabSlotPair(data, view, view.getRole(), gx, gy));
        }
        notifyOutputsChanged();
    }

    LabMachine machine() {
        return machine;
    }

    LabRecipeIndex.LabRecipeEntry entry() {
        return entry;
    }

    IRecipeLayoutDrawable<?> jeiLayout() {
        return jeiLayout;
    }

    IRecipeLayoutDrawable<?> sampleLayout() {
        return sampleLayout;
    }

    Recipe<?> original() {
        return entry == null ? null : LabRecipeIndex.recipeById(entry.id());
    }

    private LabRecipeMachine support() {
        return machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
    }

    void addSlotPair(LabSlotPair pair) {
        slotPairs.add(pair);
    }

    void notifyOutputsChanged() {
        if (outputsChangedListener != null) {
            outputsChangedListener.run();
        }
    }

    void applyIngredientKind(LabSlotData data, Recipe<?> original, int index, boolean input) {
        if (!input || original == null) {
            return;
        }
        LabRecipeMachine support = support();
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

    void applyChance(LabSlotData data, List<ProcessingOutput> rollable, List<Float> ieChances, int index) {
        if (!ieChances.isEmpty() && index < ieChances.size()) {
            data.setChance(ieChances.get(index));
            return;
        }
        if (index < rollable.size()) {
            data.setChance(rollable.get(index).getChance());
        }
    }

    static List<ProcessingOutput> rollableResults(Recipe<?> original) {
        if (original instanceof ProcessingRecipe<?> processing) {
            return processing.getRollableResults();
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

    void paintSlot(int button, LabSlotData data) {
        paint.paintSlot(button, data);
    }

    void adjustStackCount(LabSlotData data, int delta) {
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
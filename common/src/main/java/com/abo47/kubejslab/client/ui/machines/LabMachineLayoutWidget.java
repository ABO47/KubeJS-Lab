package com.abo47.kubejslab.client.ui.machines;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.base.LabColors;
import com.abo47.kubejslab.client.ui.base.LabLayout;
import com.abo47.kubejslab.client.ui.picker.LabPick;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.platform.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import com.abo47.kubejslab.client.jei.LabJeiPlugin;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeSettingsWidget;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

public final class LabMachineLayoutWidget extends WidgetGroup {
    private LabMachine machine;
    private LabRecipeIndex.LabRecipeEntry entry;
    private IRecipeLayoutDrawable<?> jeiLayout;
    private IRecipeLayoutDrawable<?> sampleLayout;
    private final List<SlotPair> slotPairs = new ArrayList<>();
    private int paintButton = -1;
    private final Set<SlotData> paintedSlots = new HashSet<>();
    private LabPick pendingPick;
    private Runnable outputsChangedListener;
    private int gridWidth = 3;
    private int gridHeight = 3;
    private int outputCount = 1;

    public LabMachineLayoutWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setGridSize(int width, int height) {
        int w = Math.max(1, Math.min(9, width));
        int h = Math.max(1, Math.min(9, height));
        if (gridWidth == w && gridHeight == h) {
            return;
        }
        gridWidth = w;
        gridHeight = h;
        rebuild();
    }

    public void setOutputCount(int count) {
        int clamped = Math.max(1, Math.min(6, count));
        if (outputCount == clamped) {
            return;
        }
        outputCount = clamped;
        rebuild();
    }

    public void setMachine(LabMachine machine) {
        if (this.machine != machine) {
            this.gridWidth = 3;
            this.gridHeight = 3;
            this.outputCount = 1;
            this.slotPairs.clear();
            this.pendingPick = null;
        }
        this.machine = machine;
        this.entry = null;
        this.jeiLayout = null;
        this.sampleLayout = machine == null ? null : findSampleLayout(machine);
        rebuild();
    }

    public void showRecipe(LabRecipeIndex.LabRecipeEntry entry) {
        this.entry = entry;
        this.jeiLayout = entry == null ? null : findJeiLayout(entry);
        this.slotPairs.clear();
        rebuild();
    }

    public void setPendingPick(LabPick pendingPick) {
        this.pendingPick = pendingPick;
    }

    public void setOutputsChangedListener(Runnable outputsChangedListener) {
        this.outputsChangedListener = outputsChangedListener;
    }

    public List<LabIngredient> getInputs() {
        List<SlotPair> inputs = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role == RecipeIngredientRole.INPUT && !pair.data.isEmpty()) {
                inputs.add(pair);
            }
        }
        LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
        if (support != null && support.gridLayout()) {
            int width = Math.max(1, gridWidth);
            int height = Math.max(1, gridHeight);
            LabIngredient[] cells = new LabIngredient[width * height];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new LabIngredient.Item(ItemStack.EMPTY);
            }
            for (SlotPair pair : inputs) {
                if (pair.gx >= 0 && pair.gx < width && pair.gy >= 0 && pair.gy < height) {
                    cells[pair.gy * width + pair.gx] = pair.data.toIngredient();
                }
            }
            return List.of(cells);
        }
        inputs.sort((SlotPair a, SlotPair b) -> {
            int row = Integer.compare(a.gy(), b.gy());
            return row != 0 ? row : Integer.compare(a.gx(), b.gx());
        });
        List<LabIngredient> ordered = new ArrayList<>(inputs.size());
        for (SlotPair pair : inputs) {
            ordered.add(pair.data.toIngredient());
        }
        return ordered;
    }

    public List<LabRecipeOutput> getOutputs() {
        List<LabRecipeOutput> outputs = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role == RecipeIngredientRole.OUTPUT) {
                LabRecipeOutput output = pair.data.toOutput();
                if (output != null) {
                    outputs.add(output);
                }
            }
        }
        return outputs;
    }

    public List<LabRecipeSettingsWidget.OutputRow> getOutputRows() {
        LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
        if (support == null || !support.supportsChance()) {
            return List.of();
        }
        List<LabRecipeSettingsWidget.OutputRow> rows = new ArrayList<>();
        for (SlotPair pair : slotPairs) {
            if (pair.role == RecipeIngredientRole.OUTPUT
                    && pair.data.kind == SlotKind.ITEM && !pair.data.stack.isEmpty()) {
                SlotData data = pair.data;
                rows.add(new LabRecipeSettingsWidget.OutputRow(data.stack,
                        () -> data.chance,
                        value -> data.chance = Math.max(0f, Math.min(1f, value))));
            }
        }
        return rows;
    }

    public void clearPhantoms() {
        for (SlotPair pair : slotPairs) {
            pair.data.clear();
        }
        pendingPick = null;
        notifyOutputsChanged();
    }

    private void rebuild() {
        LabRecipeMachine support = machine == null ? null : LabRecipeMachines.get(machine.recipeTypeUid());
        if (support != null && support.gridLayout()) {
            Map<Long, SlotData> snapshot = new HashMap<>();
            for (SlotPair pair : slotPairs) {
                if (pair.role == RecipeIngredientRole.INPUT && pair.gx >= 0 && pair.gy >= 0) {
                    snapshot.put(gridKey(pair.gx, pair.gy), pair.data);
                }
            }
            clearAllWidgets();
            slotPairs.clear();
            rebuildGrid(snapshot);
            return;
        }
        clearAllWidgets();
        slotPairs.clear();

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
        Recipe<?> original = entry == null ? null : LabRecipeIndex.recipeById(entry.id());
        List<ProcessingOutput> rollable = rollableResults(original);
        int itemInputIndex = 0;
        int itemOutputIndex = 0;
        for (IRecipeSlotView view : source.getRecipeSlotsView().getSlotViews()) {
            if (!(view instanceof IRecipeSlotDrawable drawable)) {
                continue;
            }
            Rect2i rect;
            try {
                rect = drawable.getRect();
            } catch (RuntimeException | LinkageError ignored) {
                continue;
            }
            SlotData data = new SlotData();
            boolean fluidSlot = Services.platform().readFluidIngredient(view).map(data::setFluidValue).orElse(false);
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
                PhantomHandler handler = new PhantomHandler(data);
                LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0,
                        ox + rect.getX(), oy + rect.getY());
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(this);
                slot.setRole(view.getRole());
                addWidget(slot);
            }
            if (view.getRole() == RecipeIngredientRole.OUTPUT && !fluidSlot) {
                applyChance(data, rollable, itemOutputIndex);
                itemOutputIndex++;
            }

            int gx = (rect.getX() - 1) / 18;
            int gy = (rect.getY() - 1) / 18;
            slotPairs.add(new SlotPair(data, view, view.getRole(), gx, gy));
        }
        if (entry == null && support != null && support.supportsOutputCount()) {
            int existing = 0;
            for (SlotPair pair : slotPairs) {
                if (pair.role == RecipeIngredientRole.OUTPUT) {
                    existing++;
                }
            }
            while (existing < outputCount) {
                SlotData data = new SlotData();
                LabPhantomSlotWidget slot = new LabPhantomSlotWidget(new PhantomHandler(data), 0,
                        ox + 2 + existing * 18, oy + layoutH + 4);
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(this);
                slot.setRole(RecipeIngredientRole.OUTPUT);
                addWidget(slot);
                slotPairs.add(new SlotPair(data, null, RecipeIngredientRole.OUTPUT, existing, layoutH / 18));
                existing++;
            }
        }
        notifyOutputsChanged();
    }

    private void rebuildGrid(Map<Long, SlotData> snapshot) {
        int w = Math.max(1, gridWidth);
        int h = Math.max(1, gridHeight);
        int ox = LabLayout.MACHINE_PAD;
        int oy = Math.max(LabLayout.MACHINE_PAD, (getSizeHeight() - h * 18) / 2);

        Recipe<?> original = entry == null ? null : LabRecipeIndex.recipeById(entry.id());
        KubeJSLab.LOGGER.info("[MechCrafting] rebuildGrid: grid {}x{}, snapshot cells={}, original={}", w, h,
                snapshot.size(), original == null ? "null" : original.getId());

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
                        KubeJSLab.LOGGER.info("[MechCrafting] cell ({},{}) loaded from original: {}", gy, gx, json);
                    }
                }
                PhantomHandler handler = new PhantomHandler(data);
                LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0, ox + gx * 18, oy + gy * 18);
                slot.setClearSlotOnRightClick(true);
                slot.setClientSideWidget();
                slot.setDragOwner(this);
                slot.setRole(RecipeIngredientRole.INPUT);
                addWidget(slot);
                slotPairs.add(new SlotPair(data, null, RecipeIngredientRole.INPUT, gx, gy));
            }
        }

        IRecipeLayoutDrawable<?> source = jeiLayout != null ? jeiLayout : sampleLayout;
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
                SlotData data = new SlotData();
                boolean fluidSlot = Services.platform().readFluidIngredient(view).map(data::setFluidValue).orElse(false);
                if (fluidSlot) {
                    LabPhantomFluidSlotWidget slot = new LabPhantomFluidSlotWidget(data, ox + rect.getX(), oy + rect.getY());
                    slot.setClientSideWidget();
                    slot.setDragOwner(this);
                    slot.setRole(RecipeIngredientRole.OUTPUT);
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
                    PhantomHandler handler = new PhantomHandler(data);
                    LabPhantomSlotWidget slot = new LabPhantomSlotWidget(handler, 0, ox + rect.getX(), oy + rect.getY());
                    slot.setClearSlotOnRightClick(true);
                    slot.setClientSideWidget();
                    slot.setDragOwner(this);
                    slot.setRole(RecipeIngredientRole.OUTPUT);
                    addWidget(slot);
                }
                slotPairs.add(new SlotPair(data, view, RecipeIngredientRole.OUTPUT, (rect.getX() - 1) / 18,
                        (rect.getY() - 1) / 18));
            }
        }
        notifyOutputsChanged();
    }

    private static long gridKey(int gx, int gy) {
        return (long) gx << 32 | gy;
    }

    private static Ingredient gridIngredientAt(Recipe<?> original, int row, int col) {
        if (!(original instanceof MechanicalCraftingRecipe crafting)) {
            KubeJSLab.LOGGER.warn("[MechCrafting] gridIngredientAt: original {} is not a MechanicalCraftingRecipe ({})",
                    original.getId(), original.getClass().getName());
            return Ingredient.EMPTY;
        }
        ShapedRecipe shaped = crafting;
        int width = Math.max(1, Math.min(9, shaped.getWidth()));
        int index = row * width + col;
        List<Ingredient> ingredients = shaped.getIngredients();
        Ingredient result = index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
        if (!result.isEmpty()) {
            KubeJSLab.LOGGER.debug("[MechCrafting] gridIngredientAt({},{}): width={}, index={} -> {}", row, col, width,
                    index, result.toJson());
        }
        return result;
    }

    private static ItemStack firstIngredientStack(Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static List<ProcessingOutput> rollableResults(Recipe<?> original) {
        if (original instanceof ProcessingRecipe<?> processing) {
            return processing.getRollableResults();
        }
        return List.of();
    }

    private void applyIngredientKind(SlotData data, Recipe<?> original, int index, boolean input) {
        if (!input || original == null) {
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

    private static void applyChance(SlotData data, List<ProcessingOutput> rollable, int index) {
        if (index < rollable.size()) {
            data.setChance(rollable.get(index).getChance());
        }
    }

    private IRecipeLayoutDrawable<?> findSampleLayout(LabMachine machine) {
        if (LabJeiPlugin.runtime() == null) {
            return null;
        }
        return findSample(LabJeiPlugin.runtime(), machine.category());
    }

    private IRecipeLayoutDrawable<?> findJeiLayout(LabRecipeIndex.LabRecipeEntry entry) {
        if (LabJeiPlugin.runtime() == null || machine == null) {
            return null;
        }
        return findEntry(LabJeiPlugin.runtime(), machine.category(), entry.id());
    }

    private static <R> IRecipeLayoutDrawable<?> findSample(IJeiRuntime runtime, IRecipeCategory<R> category) {
        IRecipeManager manager = runtime.getRecipeManager();
        try (Stream<R> stream = manager.createRecipeLookup(category.getRecipeType()).includeHidden().get()) {
            return stream
                    .findFirst()
                    .flatMap(recipe -> manager.createRecipeLayoutDrawable(category, recipe, emptyFocus(runtime)))
                    .orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static <R> IRecipeLayoutDrawable<?> findEntry(
            IJeiRuntime runtime, IRecipeCategory<R> category, ResourceLocation entryId) {
        IRecipeManager manager = runtime.getRecipeManager();
        try (Stream<R> stream = manager.createRecipeLookup(category.getRecipeType()).includeHidden().get()) {
            return stream
                    .filter(recipe -> entryId.equals(category.getRegistryName(recipe)))
                    .findFirst()
                    .flatMap(recipe -> manager.createRecipeLayoutDrawable(category, recipe, emptyFocus(runtime)))
                    .orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static IFocusGroup emptyFocus(IJeiRuntime runtime) {
        return runtime.getJeiHelpers().getFocusFactory().createFocusGroup(Collections.emptyList());
    }

    private record SlotPair(SlotData data, IRecipeSlotView view, RecipeIngredientRole role, int gx, int gy) {
    }

    enum SlotKind {
        ITEM,
        TAG,
        FLUID
    }

    static final class SlotData {
        SlotKind kind = SlotKind.ITEM;
        ItemStack stack = ItemStack.EMPTY;
        ResourceLocation tag;
        FluidStack fluid = FluidStack.empty();
        float chance = 1f;

        void setChance(float chance) {
            this.chance = Math.max(0f, Math.min(1f, chance));
        }

        void setItemValue(ItemStack stack) {
            this.kind = SlotKind.ITEM;
            this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
            this.tag = null;
        }

        void setTagValue(ResourceLocation tag) {
            this.kind = SlotKind.TAG;
            this.tag = tag;
            if (this.stack.isEmpty()) {
                this.stack = firstTagMember(tag);
            }
        }

        boolean setFluidValue(FluidStack fluid) {
            if (fluid == null || fluid.isEmpty()) {
                return false;
            }
            this.kind = SlotKind.FLUID;
            this.fluid = fluid.copy();
            this.stack = ItemStack.EMPTY;
            this.tag = null;
            return true;
        }

        boolean isEmpty() {
            return switch (kind) {
                case FLUID -> fluid.isEmpty();
                case TAG -> tag == null;
                case ITEM -> stack.isEmpty();
            };
        }

        void clear() {
            kind = SlotKind.ITEM;
            stack = ItemStack.EMPTY;
            tag = null;
            fluid = FluidStack.empty();
            chance = 1f;
        }

        LabIngredient toIngredient() {
            return switch (kind) {
                case FLUID -> new LabIngredient.Fluid(fluid);
                case TAG -> tag == null ? new LabIngredient.Item(stack) : new LabIngredient.Tag(tag);
                case ITEM -> new LabIngredient.Item(stack);
            };
        }

        LabRecipeOutput toOutput() {
            return switch (kind) {
                case FLUID -> fluid.isEmpty() ? null : new LabRecipeOutput.Fluid(fluid);
                case TAG -> stack.isEmpty() ? null : new LabRecipeOutput.Item(stack, chance);
                case ITEM -> stack.isEmpty() ? null : new LabRecipeOutput.Item(stack, chance);
            };
        }

        private static ItemStack firstTagMember(ResourceLocation tag) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, tag);
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
                return new ItemStack(holder);
            }
            return ItemStack.EMPTY;
        }
    }

    void beginPaint(int button) {
        paintButton = button;
        paintedSlots.clear();
    }

    boolean isPainting(int button) {
        return paintButton == button;
    }

    void endPaint() {
        paintButton = -1;
        paintedSlots.clear();
        notifyOutputsChanged();
    }

    void paintSlot(int button, SlotData data) {
        if (gui == null) {
            return;
        }
        if (!paintedSlots.add(data)) {
            return;
        }
        ItemStack carried = gui.getModularUIContainer().getCarried();
        if (button == LabColors.MOUSE_BUTTON_RIGHT && carried.isEmpty()) {
            data.clear();
            return;
        }
        if (pendingPick != null) {
            applyPick(data, pendingPick);
            return;
        }
        if (carried.isEmpty()) {
            return;
        }
        if (data.kind == SlotKind.FLUID) {
            IFluidTransfer transfer = FluidTransferHelper.getFluidTransfer(carried);
            if (transfer != null && transfer.getTanks() > 0) {
                FluidStack content = transfer.getFluidInTank(0);
                if (!content.isEmpty()) {
                    data.setFluidValue(content.copy(1000));
                }
            }
            return;
        }
        ItemStack current = data.stack;
        if (current.isEmpty() || !ItemStack.isSameItem(current, carried)) {
            data.setItemValue(carried.copyWithCount(1));
        } else {
            data.setItemValue(current.copyWithCount(Math.min(current.getCount() + 1, current.getMaxStackSize())));
        }
    }

    private static void applyPick(SlotData data, LabPick pick) {
        if (pick instanceof LabPick.Item item) {
            if (data.kind != SlotKind.FLUID) {
                data.setItemValue(item.stack().copyWithCount(1));
            }
            return;
        }
        if (pick instanceof LabPick.Tag tag) {
            if (data.kind != SlotKind.FLUID) {
                data.setTagValue(tag.tag());
            }
            return;
        }
        if (pick instanceof LabPick.Fluid fluid) {
            if (data.kind == SlotKind.FLUID) {
                data.setFluidValue(fluid.fluid());
            }
        }
    }

    private void notifyOutputsChanged() {
        if (outputsChangedListener != null) {
            outputsChangedListener.run();
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        if (paintButton == button) {
            endPaint();
            handled = true;
        }
        return handled;
    }

    static final class PhantomHandler implements IItemTransfer {
        private final SlotData data;

        PhantomHandler(SlotData data) {
            this.data = data;
        }

        SlotData data() {
            return data;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return data.stack;
        }

        @Override
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate, boolean notifyChanges) {
            if (simulate) {
                return ItemStack.EMPTY;
            }
            data.setItemValue(stack);
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate, boolean notifyChanges) {
            if (simulate) {
                return data.stack.copy();
            }
            ItemStack extracted = data.stack;
            data.setItemValue(ItemStack.EMPTY);
            return extracted;
        }

        @Override
        public int getSlotLimit(int index) {
            return 64;
        }

        @Override
        public boolean isItemValid(int index, ItemStack stack) {
            return true;
        }

        @Override
        public Object createSnapshot() {
            return new Object[] {data.stack};
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            data.setItemValue((ItemStack) ((Object[]) snapshot)[0]);
        }
    }
}

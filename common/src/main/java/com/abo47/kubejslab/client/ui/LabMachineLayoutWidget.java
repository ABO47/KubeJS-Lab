package com.abo47.kubejslab.client.ui;

import java.util.Collections;
import java.util.stream.Stream;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;

import com.abo47.kubejslab.client.jei.LabJeiPlugin;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

public final class LabMachineLayoutWidget extends WidgetGroup {
    private LabMachine machine;
    private LabRecipeIndex.LabRecipeEntry entry;
    private IRecipeLayoutDrawable<?> jeiLayout;
    private IRecipeLayoutDrawable<?> sampleLayout;

    public LabMachineLayoutWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public void setMachine(LabMachine machine) {
        this.machine = machine;
        this.entry = null;
        this.jeiLayout = null;
        this.sampleLayout = machine == null ? null : findSampleLayout(machine);
        rebuild();
    }

    public void showRecipe(LabRecipeIndex.LabRecipeEntry entry) {
        this.entry = entry;
        this.jeiLayout = entry == null ? null : findJeiLayout(entry);
        rebuild();
    }

    private void rebuild() {
        clearAllWidgets();

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
            ItemStack stack = ItemStack.EMPTY;
            if (entry != null) {
                for (ItemStack s : view.getIngredients(VanillaTypes.ITEM_STACK).toList()) {
                    stack = s;
                    break;
                }
            }
            PhantomSlotWidget slot = new PhantomSlotWidget(new PhantomHandler(stack), 0,
                    ox + rect.getX(), oy + rect.getY());
            slot.setClearSlotOnRightClick(true);
            slot.setClientSideWidget();
            addWidget(slot);
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

    private static final class PhantomHandler implements IItemTransfer {
        private ItemStack stack;

        PhantomHandler(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return stack;
        }

        @Override
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate, boolean notifyChanges) {
            if (simulate) {
                return ItemStack.EMPTY;
            }
            this.stack = stack.copy();
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate, boolean notifyChanges) {
            if (simulate) {
                return stack.copy();
            }
            ItemStack extracted = stack;
            stack = ItemStack.EMPTY;
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
            return new Object[] {stack};
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            stack = (ItemStack) ((Object[]) snapshot)[0];
        }
    }
}
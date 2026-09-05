package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.phys.shapes.CollisionContext;

import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;


public final class BlockIndex {
    private static final Map<ResourceLocation, BlockEntry> ENTRIES = new HashMap<>();

    static {
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            if (id.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == null) {
                continue;
            }
            ENTRIES.put(id, BlockEntry.of(id, block));
        }
    }

    private BlockIndex() {
    }

    public static List<BlockEntry> search(String query, boolean kubejsOnly) {
        String normalizedQuery = SearchNormalizer.normalizeQuery(query);
        List<BlockEntry> matches = new ArrayList<>();
        for (BlockEntry entry : ENTRIES.values()) {
            if (entry.kubejs() != kubejsOnly) {
                continue;
            }
            if (normalizedQuery.isBlank() || entry.matches(normalizedQuery)) {
                matches.add(entry);
            }
        }
        matches.sort(Comparator.comparing(BlockEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(BlockEntry::id));
        return matches;
    }

    public static BlockEntry entryById(ResourceLocation id) {
        return ENTRIES.get(id);
    }

    public static String typeOf(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == null) {
            return "basic";
        }
        if (block instanceof CropBlock) {
            return "crop";
        }
        if (block instanceof DaylightDetectorBlock) {
            return "detector";
        }
        if (block instanceof FallingBlock) {
            return "falling";
        }
        if (block instanceof StairBlock) {
            return "stairs";
        }
        if (block instanceof SlabBlock) {
            return "slab";
        }
        if (block instanceof FenceBlock) {
            return "fence";
        }
        if (block instanceof FenceGateBlock) {
            return "fence_gate";
        }
        if (block instanceof WallBlock) {
            return "wall";
        }
        if (block instanceof PressurePlateBlock) {
            return "pressure_plate";
        }
        if (block instanceof ButtonBlock) {
            return "button";
        }
        if (block instanceof CarpetBlock) {
            return "carpet";
        }
        if (block.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return "cardinal";
        }
        return "basic";
    }

    public static BlockFieldValues prefillValues(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == null) {
            return BlockFieldValues.defaults();
        }
        var state = block.defaultBlockState();
        boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED);
        StringBuilder joinedTags = new StringBuilder();
        for (ResourceLocation tag : block.builtInRegistryHolder().tags()
                .map(TagKey::location).sorted().toList()) {
            if (joinedTags.length() > 0) joinedTags.append(',');
            joinedTags.append(tag);
        }
        return new BlockFieldValues(block.getName().getString(), "", "", "", "",
                destroySpeedOf(state), block.getExplosionResistance(), false, state.getLightEmission(),
                soundTypeName(state.getSoundType()), state.requiresCorrectToolForDrops(),
                noCollisionOf(state), waterlogged, false, false, true,
                0f, 0f, 0f, joinedTags.toString(), "", "", 0, 0, 0f, "", "", "");
    }

    private static float destroySpeedOf(BlockState state) {
        try {
            return state.getDestroySpeed(null, null);
        } catch (RuntimeException e) {
            return BlockFieldValues.DEFAULT_HARDNESS;
        }
    }

    private static boolean noCollisionOf(BlockState state) {
        try {
            return state.getCollisionShape(null, null, CollisionContext.empty()).isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String soundTypeName(SoundType soundType) {
        if (soundType == SoundType.WOOD) return "wood";
        if (soundType == SoundType.STONE) return "stone";
        if (soundType == SoundType.METAL) return "metal";
        if (soundType == SoundType.GRAVEL) return "gravel";
        if (soundType == SoundType.GRASS) return "grass";
        if (soundType == SoundType.SAND) return "sand";
        if (soundType == SoundType.GLASS) return "glass";
        if (soundType == SoundType.WOOL) return "wool";
        if (soundType == SoundType.SNOW) return "snow";
        if (soundType == SoundType.CROP) return "crop";
        if (soundType == SoundType.SLIME_BLOCK) return "slime";
        if (soundType == SoundType.ANVIL) return "anvil";
        if (soundType == SoundType.LADDER) return "ladder";
        if (soundType == SoundType.HONEY_BLOCK) return "honey";
        if (soundType == SoundType.AMETHYST) return "amethyst";
        if (soundType == SoundType.DEEPSLATE) return "deepslate";
        if (soundType == SoundType.NETHERRACK) return "netherrack";
        if (soundType == SoundType.CANDLE) return "candle";
        if (soundType == SoundType.SCULK) return "sculk";
        return "";
    }

    public record BlockEntry(ResourceLocation id, ItemStack stack, String name, boolean kubejs,
            String normalizedId, String normalizedName) {
        public static BlockEntry of(ResourceLocation id, Block block) {
            String name = block.getName().getString();
            Item item = block.asItem();
            ItemStack stack = item instanceof BlockItem && item != net.minecraft.world.item.Items.AIR
                    ? new ItemStack(item)
                    : ItemStack.EMPTY;
            return new BlockEntry(id, stack, name, id.getNamespace().equals("kubejs"),
                    SearchNormalizer.normalizeUserSearch(id.toString()),
                    SearchNormalizer.normalizeUserSearch(name));
        }

        public boolean matches(String normalizedQuery) {
            return normalizedId.contains(normalizedQuery) || normalizedName.contains(normalizedQuery);
        }
    }
}

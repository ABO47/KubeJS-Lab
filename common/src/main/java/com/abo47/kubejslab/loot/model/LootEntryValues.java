package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

public record LootEntryValues(String type, String item, String tag, String lootTable, String countType,
        float countValue, float countMin, float countMax, int weight, int quality,
        float lootBonusMin, float lootBonusMax, List<String> conditionNotes,
        String toolRequirement, boolean entryKilledByPlayer, float entryChance, float entryChanceLooting,
        int alternativeGroup, boolean fortuneBonus,
        int lootBonusLimit, boolean explosionDecay, String extraConditions, String extraFunctions) {

    public LootEntryValues {
        type = type == null ? "" : type;
        item = item == null ? "" : item;
        tag = tag == null ? "" : tag;
        lootTable = lootTable == null ? "" : lootTable;
        countType = countType == null ? "" : countType;
        countValue = Math.max(0f, countValue);
        countMin = Math.max(0f, countMin);
        countMax = Math.max(0f, countMax);
        weight = Math.max(1, weight);
        quality = Math.max(0, quality);
        lootBonusMin = Math.max(0f, lootBonusMin);
        lootBonusMax = Math.max(0f, lootBonusMax);
        conditionNotes = conditionNotes == null ? List.of() : List.copyOf(conditionNotes);
        toolRequirement = toolRequirement == null ? "" : toolRequirement;
        entryChance = Math.max(0f, Math.min(1f, entryChance));
        entryChanceLooting = Math.max(0f, entryChanceLooting);
        alternativeGroup = Math.max(0, alternativeGroup);
        lootBonusLimit = Math.max(0, lootBonusLimit);
        extraConditions = extraConditions == null ? "" : extraConditions;
        extraFunctions = extraFunctions == null ? "" : extraFunctions;
    }

    public static LootEntryValues defaults() {
        return new LootEntryValues("item", "", "", "", "constant", 1f, 0f, 0f, 1, 0, 0f, 0f, List.of(),
                "", false, 1f, 0f, 0, false, 0, false, "", "");
    }

    public static void write(FriendlyByteBuf buf, LootEntryValues v) {
        buf.writeUtf(v.type(), 32767);
        buf.writeUtf(v.item(), 32767);
        buf.writeUtf(v.tag(), 32767);
        buf.writeUtf(v.lootTable(), 32767);
        buf.writeUtf(v.countType(), 32767);
        buf.writeFloat(v.countValue());
        buf.writeFloat(v.countMin());
        buf.writeFloat(v.countMax());
        buf.writeVarInt(v.weight());
        buf.writeVarInt(v.quality());
        buf.writeFloat(v.lootBonusMin());
        buf.writeFloat(v.lootBonusMax());
        buf.writeVarInt(Math.min(v.conditionNotes().size(), 16));
        for (int i = 0; i < Math.min(v.conditionNotes().size(), 16); i++) {
            buf.writeUtf(v.conditionNotes().get(i), 256);
        }
        buf.writeUtf(v.toolRequirement(), 32);
        buf.writeBoolean(v.entryKilledByPlayer());
        buf.writeFloat(v.entryChance());
        buf.writeFloat(v.entryChanceLooting());
        buf.writeVarInt(v.alternativeGroup());
        buf.writeBoolean(v.fortuneBonus());
        buf.writeVarInt(v.lootBonusLimit());
        buf.writeBoolean(v.explosionDecay());
        buf.writeUtf(v.extraConditions(), 2048);
        buf.writeUtf(v.extraFunctions(), 2048);
    }

    public static LootEntryValues read(FriendlyByteBuf buf) {
        String type = buf.readUtf();
        String item = buf.readUtf();
        String tag = buf.readUtf();
        String lootTable = buf.readUtf();
        String countType = buf.readUtf();
        float countValue = buf.readFloat();
        float countMin = buf.readFloat();
        float countMax = buf.readFloat();
        int weight = Math.max(1, Math.min(1000, buf.readVarInt()));
        int quality = Math.max(0, Math.min(1000, buf.readVarInt()));
        float lootBonusMin = Math.max(0f, buf.readFloat());
        float lootBonusMax = Math.max(0f, buf.readFloat());
        int noteCount = Math.max(0, Math.min(16, buf.readVarInt()));
        ArrayList<String> notes = new ArrayList<>(noteCount);
        for (int i = 0; i < noteCount; i++) {
            notes.add(buf.readUtf(256));
        }
        String toolRequirement = buf.readUtf(32);
        boolean entryKilledByPlayer = buf.readBoolean();
        float entryChance = buf.readFloat();
        float entryChanceLooting = buf.readFloat();
        int alternativeGroup = Math.max(0, Math.min(64, buf.readVarInt()));
        boolean fortuneBonus = buf.readBoolean();
        int lootBonusLimit = Math.max(0, Math.min(1000, buf.readVarInt()));
        boolean explosionDecay = buf.readBoolean();
        String extraConditions = buf.readUtf(2048);
        String extraFunctions = buf.readUtf(2048);
        return new LootEntryValues(type, item, tag, lootTable, countType, countValue, countMin, countMax,
                weight, quality, lootBonusMin, lootBonusMax, notes, toolRequirement, entryKilledByPlayer,
                entryChance, entryChanceLooting, alternativeGroup, fortuneBonus, lootBonusLimit, explosionDecay,
                extraConditions, extraFunctions);
    }
}

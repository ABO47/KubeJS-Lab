package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

public record LabLootPoolValues(String rollsType, float rollsValue, float rollsMin, float rollsMax, int rollsN,
        float rollsP, boolean survivesExplosion, float randomChance, boolean killedByPlayer, boolean furnaceSmelt,
        boolean lootingEnchant, float lootingCount, int lootingLimit, List<LabLootEntryValues> entries) {

    public LabLootPoolValues {
        rollsType = rollsType == null ? "" : rollsType;
        rollsValue = Math.max(0f, rollsValue);
        rollsMin = Math.max(0f, rollsMin);
        rollsMax = Math.max(0f, rollsMax);
        rollsN = Math.max(0, rollsN);
        rollsP = Math.max(0f, Math.min(1f, rollsP));
        randomChance = Math.max(0f, Math.min(1f, randomChance));
        lootingCount = Math.max(0f, lootingCount);
        lootingLimit = Math.max(0, lootingLimit);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static LabLootPoolValues defaults() {
        return new LabLootPoolValues("constant", 1f, 0f, 0f, 0, 0.5f, true, 1f, false, false, false, 0f, 0,
                List.of(LabLootEntryValues.defaults()));
    }

    public static void write(FriendlyByteBuf buf, LabLootPoolValues v) {
        buf.writeUtf(v.rollsType(), 32767);
        buf.writeFloat(v.rollsValue());
        buf.writeFloat(v.rollsMin());
        buf.writeFloat(v.rollsMax());
        buf.writeVarInt(v.rollsN());
        buf.writeFloat(v.rollsP());
        buf.writeBoolean(v.survivesExplosion());
        buf.writeFloat(v.randomChance());
        buf.writeBoolean(v.killedByPlayer());
        buf.writeBoolean(v.furnaceSmelt());
        buf.writeBoolean(v.lootingEnchant());
        buf.writeFloat(v.lootingCount());
        buf.writeVarInt(v.lootingLimit());
        buf.writeVarInt(v.entries().size());
        for (LabLootEntryValues e : v.entries()) {
            LabLootEntryValues.write(buf, e);
        }
    }

    public static LabLootPoolValues read(FriendlyByteBuf buf) {
        String rollsType = buf.readUtf();
        float rollsValue = buf.readFloat();
        float rollsMin = buf.readFloat();
        float rollsMax = buf.readFloat();
        int rollsN = Math.max(0, Math.min(1000, buf.readVarInt()));
        float rollsP = buf.readFloat();
        boolean survivesExplosion = buf.readBoolean();
        float randomChance = buf.readFloat();
        boolean killedByPlayer = buf.readBoolean();
        boolean furnaceSmelt = buf.readBoolean();
        boolean lootingEnchant = buf.readBoolean();
        float lootingCount = buf.readFloat();
        int lootingLimit = Math.max(0, Math.min(1000, buf.readVarInt()));
        int entryCount = Math.min(buf.readVarInt(), 64);
        List<LabLootEntryValues> entries = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            entries.add(LabLootEntryValues.read(buf));
        }
        if (entries.isEmpty()) {
            entries.add(LabLootEntryValues.defaults());
        }
        return new LabLootPoolValues(rollsType, rollsValue, rollsMin, rollsMax, rollsN, rollsP, survivesExplosion,
                randomChance, killedByPlayer, furnaceSmelt, lootingEnchant, lootingCount, lootingLimit, entries);
    }
}

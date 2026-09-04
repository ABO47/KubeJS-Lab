package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;


public final class LabLootNotes {
    public static final String PREFIX = "kubejslab.loot.note.";

    public static final String KILLED_BY_PLAYER = PREFIX + "killed_by_player";
    public static final String SURVIVES_EXPLOSION = PREFIX + "survives_explosion";
    public static final String CHANCE = PREFIX + "chance";
    public static final String CHANCE_LOOTING = PREFIX + "chance_looting";
    public static final String MATCH_TOOL = PREFIX + "match_tool";
    public static final String ENCHANT = PREFIX + "enchant";
    public static final String ENCHANT_LEVELED = PREFIX + "enchant_leveled";
    public static final String MATCHING_TOOL = PREFIX + "matching_tool";
    public static final String WHILE_BURNING = PREFIX + "while_burning";
    public static final String INVERTED = PREFIX + "inverted";
    public static final String ANY_OF = PREFIX + "any_of";
    public static final String ALL_OF = PREFIX + "all_of";
    public static final String OR = PREFIX + "or";
    public static final String AND = PREFIX + "and";
    public static final String BINOMIAL = PREFIX + "binomial";
    public static final String COUNT_PROVIDER = PREFIX + "count_provider";
    public static final String ENCHANT_BONUS = PREFIX + "enchant_bonus";
    public static final String ROLLS_PROVIDER = PREFIX + "rolls_provider";
    public static final String SMELTED = PREFIX + "smelted";
    public static final String SMELTED_BURNING = PREFIX + "smelted_burning";
    public static final String COPY_STATE = PREFIX + "copy_state";
    public static final String COPY_NAME = PREFIX + "copy_name";
    public static final String KEEP_CONTENTS = PREFIX + "keep_contents";
    public static final String ENCHANT_RANDOM = PREFIX + "enchant_random";
    public static final String ENCHANTED = PREFIX + "enchanted";
    public static final String PLAYER_HEAD = PREFIX + "player_head";
    public static final String NBT_EDITS = PREFIX + "nbt_edits";

    private LabLootNotes() {
    }

    public static String encode(String key, String... args) {
        StringBuilder sb = new StringBuilder(key);
        for (String arg : args) {
            sb.append('|').append(arg == null ? "" : arg);
        }
        return sb.toString();
    }

    public static boolean isEncoded(String note) {
        return note != null && note.startsWith(PREFIX) && note.length() > PREFIX.length()
                && !note.substring(PREFIX.length(), PREFIX.length() + 1).contains(" ");
    }

    public static String keyOf(String note) {
        int end = note.indexOf('|');
        return end < 0 ? note : note.substring(0, end);
    }

    public static List<String> argsOf(String note) {
        List<String> args = new ArrayList<>();
        int start = note.indexOf('|');
        while (start >= 0) {
            int end = note.indexOf('|', start + 1);
            args.add(end < 0 ? note.substring(start + 1) : note.substring(start + 1, end));
            start = end;
        }
        return args;
    }

    public static String vanillaRef(String translationKey) {
        return "@" + translationKey;
    }

    public static boolean isVanillaRef(String arg) {
        return arg != null && arg.startsWith("@") && arg.length() > 1;
    }
}

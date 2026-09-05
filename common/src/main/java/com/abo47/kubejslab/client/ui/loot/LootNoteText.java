package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.abo47.kubejslab.loot.model.LootNotes;


public final class LootNoteText {
    private LootNoteText() {
    }

    @Nullable
    public static Component resolve(@Nullable String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        if (!LootNotes.isEncoded(note)) {
            return Component.literal(note);
        }
        String key = LootNotes.keyOf(note);
        List<String> args = LootNotes.argsOf(note);
        if (LootNotes.ANY_OF.equals(key)) {
            return joinComponents(args, I18n.get(LootNotes.OR));
        }
        if (LootNotes.ALL_OF.equals(key)) {
            return joinComponents(args, I18n.get(LootNotes.AND));
        }
        Object[] resolved = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            resolved[i] = resolveArg(args.get(i));
        }
        return Component.translatable(key, resolved);
    }

    private static Object resolveArg(String arg) {
        if (LootNotes.isVanillaRef(arg)) {
            return Component.translatable(arg.substring(1));
        }
        if (LootNotes.isEncoded(arg)) {
            Component nested = resolve(arg);
            return nested == null ? "" : nested;
        }
        return arg;
    }

    @Nullable
    private static Component joinComponents(List<String> args, String joiner) {
        MutableComponent out = null;
        for (String arg : args) {
            Component part = resolve(arg);
            if (part == null) {
                continue;
            }
            out = out == null ? part.copy() : out.append(joiner).append(part);
        }
        return out;
    }

    public static String resolveString(@Nullable String note) {
        if (note == null || note.isBlank()) {
            return "";
        }
        if (!LootNotes.isEncoded(note)) {
            return note;
        }
        String key = LootNotes.keyOf(note);
        List<String> args = LootNotes.argsOf(note);
        if (LootNotes.ANY_OF.equals(key)) {
            return joinStrings(args, I18n.get(LootNotes.OR));
        }
        if (LootNotes.ALL_OF.equals(key)) {
            return joinStrings(args, I18n.get(LootNotes.AND));
        }
        Object[] resolved = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            resolved[i] = resolveStringArg(args.get(i));
        }
        try {
            return I18n.get(key, resolved);
        } catch (Exception ignored) {
            return note;
        }
    }

    private static String resolveStringArg(String arg) {
        if (LootNotes.isVanillaRef(arg)) {
            return I18n.get(arg.substring(1));
        }
        if (LootNotes.isEncoded(arg)) {
            return resolveString(arg);
        }
        return arg;
    }

    public static String joinStrings(List<String> notes, String joiner) {
        List<String> parts = new ArrayList<>();
        for (String note : notes) {
            String resolved = resolveString(note);
            if (!resolved.isBlank()) {
                parts.add(resolved);
            }
        }
        return String.join(joiner, parts);
    }
}

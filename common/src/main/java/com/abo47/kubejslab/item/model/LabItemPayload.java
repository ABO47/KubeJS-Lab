package com.abo47.kubejslab.item.model;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;


public record LabItemPayload(@Nullable ResourceLocation target, String type, LabItemFieldValues values,
        List<String> tags, List<LabItemAction> actions) {

    public LabItemPayload {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        if (tags == null) {
            throw new IllegalArgumentException("tags must not be null");
        }
        if (actions == null) {
            throw new IllegalArgumentException("actions must not be null");
        }
    }
}

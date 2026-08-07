package com.abo47.kubejslab.recipe.model;

public enum ClocheRenderType {
    CROP,
    STACKING,
    STEM,
    GENERIC;

    public static ClocheRenderType cycle(ClocheRenderType current) {
        ClocheRenderType[] values = values();
        return values[(current.ordinal() + 1) % values.length];
    }

    public static ClocheRenderType byName(String name) {
        for (ClocheRenderType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return GENERIC;
    }
}
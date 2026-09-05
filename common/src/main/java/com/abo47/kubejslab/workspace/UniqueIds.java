package com.abo47.kubejslab.workspace;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;


public final class UniqueIds {
	public static ResourceLocation labId(String path) {
		return new ResourceLocation("kubejs", "lab/" + path);
	}

	public static ResourceLocation uniqueId(ResourceLocation base, Predicate<ResourceLocation> taken) {
		ResourceLocation id = base;
		int suffix = 2;
		while (taken.test(id)) {
			id = new ResourceLocation(base.getNamespace(), base.getPath() + "_" + suffix);
			suffix++;
		}
		return id;
	}

	public static String slugify(String value) {
		StringBuilder sb = new StringBuilder();
		for (char c : value.toLowerCase().toCharArray()) {
			if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '-' || c == '_' || c == '.') {
				sb.append(c);
			} else if (c == ' ') {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	private UniqueIds() {
	}
}
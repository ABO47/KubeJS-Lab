# Multiloader Template

Empty Forge + Fabric multiloader mod template for Minecraft 1.20.1.

## Stack

- Minecraft 1.20.1, Parchment 1.20.1:2023.09.03 mappings
- Forge 47.4.10, Fabric Loader 0.15.11 + Fabric API 0.92.2
- Architectury Loom 1.6-SNAPSHOT, architectury-plugin 3.4-SNAPSHOT, Gradle 8.8, Java 17
- LDLib 1.0.50 (common/forge/fabric), MixinExtras 0.3.5 (forge)
- EMI 1.1.12, JEI 15.17.0.74, REI 12.0.625 (compile-only APIs by default)
- JUnit Jupiter 5.10.2 for common unit tests
- Batik-based SVG to PNG asset tasks (`generateUiIconPngs`, `generateUiTexturePngs`, `generateModLogoPng`)

All versions live in `gradle.properties`.

## Layout

```
common/   shared code (model, platform services, registries, client)
forge/    Forge loader bootstrap + Forge-specific code
fabric/   Fabric loader bootstrap + Fabric-specific code
art/      SVG sources for icons, textures, and the mod logo
tools/    build-time SVG rasterizer used by the asset tasks
```

## Renaming the template

1. Search and replace `templatemod` (and `Template Mod`, `com.example.templatemod`) across the repo:
   - `gradle.properties` — mod_id, mod_name, mod_group_id, mod_authors, mod_description
   - `settings.gradle` — rootProject.name and the three project names
   - `TemplateMod.java` — MOD_ID and MOD_NAME constants
   - `en_us.json`, `mods.toml`, `fabric.mod.json`, mixin configs, `pack.mcmeta`
   - `common/src/main/resources/assets/templatemod/` folder name
2. Rename `art/source-logo/templatemod.svg` to your logo file and adjust the
   `generateModLogoPng` task path in the root `build.gradle`.
3. Replace the example item (`TemplateContent`, `ForgeContent`, `FabricContent`,
   `templatemod_item.json`, its lang key) with your own content.

## Commands

```
gradlew :templatemod-common:compileJava
gradlew :templatemod-forge:runClient
gradlew :templatemod-fabric:runClient
gradlew :templatemod-common:test
gradlew generateModLogoPng
```

The root build disables tests globally; the common subproject re-enables them.

## Release workflow

`.github/workflows/release.yml` builds both loaders, stages the jars, and publishes a
GitHub release. It requires a changelog at `changelogs/<version>.md` (the release
version comes from `mod_version` in `gradle.properties` unless overridden).

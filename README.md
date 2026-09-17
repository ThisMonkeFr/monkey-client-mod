# Monkey Client mod 0.7.0

Eight Minecraft versions: 26.3, 26.2, 26.1.2, 26.1.1, 26.1, 1.21.11, 1.21.10 and 1.21.9. Fabric builds use Loader 0.19.5+ and the matching Fabric API. Forge builds are provided for every listed version except 26.3, for which Forge has no official release. Java 25 is required for 26.x; Java 21 for 1.21.x.

## Controls

- Hold Right Shift for 0.3 seconds to open the animated client landing menu.
- Client Settings opens the compact module grid; settings open beside a module/HUD preview.
- Theme selects Monkey (default) or Vanilla and can match the launcher accent.
- Profiles saves whole module configurations, with create/apply/save/copy/rename/delete and clipboard import/export.
- HUD opens the position editor. Drag to move, scroll to resize, Ctrl-drag to snap.
- Zoom defaults to C. The world, both hands and HUD scale together. Only the crosshair fades smoothly.
- F2 renders a 3840 by 2160 screenshot, then restores the normal framebuffer size. A brief capture flash and real thumbnail appear for about two seconds. Captures appear in the launcher Screenshots tab.
- Waypoints manager defaults to M; the module settings allow rebinding it. There are exactly 20 Minecraft block icons; block and marker colors can be changed separately.
- Container previews appear without Shift by default. Hold Alt + Left Control over a container to pin a read-only preview, then move over its contents for item tooltips. The inspection key is configurable.

Settings persist in `config/monkeyclient.json`. Named module profiles persist in `config/monkeyclient/mod-profiles.json`. Custom item PNGs belong in `config/monkeyclient/textures/`.

World Editor follows native video render-distance changes in both directions. Fabric includes Sodium and the terrain cache supports extended distances; Forge uses the native renderer's 32-chunk limit. Creative flight's speed slider reaches 100. Hold Left Alt to boost by default, rebind the key or choose Automatic in Toggle Sprint settings. Tier Display supports a selected MCPVP kit, the best MCPVP kit, and best gamemode across all three integrated tier providers.

## Building the version-specific releases

Install Python 3.11+ and JDK 25. From this directory run:

```powershell
python tools/build-release.py --version 26.2 --loader fabric --java 'C:/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot/bin/java.exe'
python tools/build-release.py --version 1.21.11 --loader forge --java 'C:/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot/bin/java.exe'
```

Use your actual Java executable path. The tool downloads pinned SDK dependencies from Mojang, Fabric, Maven Central, Modrinth and Forge, compiles the small build helpers, generates the version adapters, compiles Java 21/25 bytecode, audits the mixins and packages the correct JAR. Legacy Fabric builds are remapped to intermediary names. Forge uses its official installer to generate patched named classes. Downloads are cached under `build/sdk`; output is under `build/release/<version>-<loader>/`. No Minecraft classes or full mapping files are distributed in the mod.

`tools/versions.lock.json` records the exact game metadata and Fabric API/Sodium download checksums. `src/main/java` targets 26.2; `ports` and `tools/port-sources.py` contain the API adaptations. The base Gradle project remains useful for 26.2 development; use the Python release builder for the complete loader/version matrix.

All 15 release combinations compile and pass their native mixin audits. The fresh-download source builder was also verified for 26.2 Fabric. Automated runtime checks boot the client on a Linux virtual display and verify the dimensions of a real F2 capture. See RELEASE-NOTES.md for the verification scope.

The Monkey menu uses smaller module cards and adapts its height to the selected category. Custom cape colours use Minecraft's elytra transparency mask to preserve the normal wing silhouette. Creative flight synchronizes speed with the integrated server; remote servers retain control over permitted movement speeds.

The pause menu also has a Monkey Client button. Launcher 0.9 copies saved module profiles and settings into newly created profiles. Forge resources declare the exact pack format for each game version; 26.3 uses RenderPearl-compatible post-processing shaders. Test-only world probes under `tools/qa` are never included in release JARs.

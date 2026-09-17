# Third-party components

Monkey Client includes the following components, with their original notices preserved. The distributed source archive includes the modified integration sources and resources. Monkey Client's original code is MIT; the Bobby-derived integration is LGPL-3.0-or-later.

- Bobby 5.2.15, Minecraft 26.2 branch: https://github.com/Johni0702/bobby/tree/master. Copyright Johannes Becker and contributors, LGPL-3.0-or-later; see BOBBY-LICENSE.md. Sources are relocated to `gg.monkeyclient.integration.cache`. Changes connect configuration to World Editor, use `.monkey-cache`, support enabling/disabling in the same world, and add a sparse region-header index so distance 1000 does not enqueue millions of absent chunks. The standalone Bobby command/configuration UI is omitted. External Bobby disables this integrated copy's mixins.
- AppleSkin 3.0.10, 26.2-fabric: https://github.com/squeek502/AppleSkin/tree/26.2-fabric. Unlicense; see APPLE-SKIN-LICENSE.txt. Native overlay/tooltip code and textures are relocated or retained under their resource namespace, connected to the Monkey module, with integrated-server food data synchronization. Existing external AppleSkin takes precedence.
- ShulkerBoxTooltip 5.4.0, 26.2.x: https://github.com/MisterPeModder/ShulkerBoxTooltip/tree/26.2.x. MIT; see SHULKER-TOOLTIP-LICENSE.txt. The vanilla-style nine-slice container frame is reused. Monkey Client implements its own compact/full container component renderer.
- Sodium (Fabric), original unmodified version-specific JARs: https://github.com/CaffeineMC/sodium / https://modrinth.com/mod/sodium. Copyright CaffeineMC and contributors, PolyForm Shield 1.0.0; each nested JAR retains its license. Versions: 0.9.2 for Minecraft 26.3, 26.2 and 26.1.2; 0.8.9 for 26.1.1 and 26.1; 0.8.14 for 1.21.11; 0.7.3 for 1.21.10 and 1.21.9. Exact download URLs and SHA-512 hashes are in tools/versions.lock.json. Forge builds do not bundle Sodium.
- MixinExtras 0.5.5: https://github.com/LlamaLad7/MixinExtras. MIT, copyright LlamaLad7 and contributors. Forge builds include the original Forge JAR through JarJar, including its license and nested common library. Fabric Loader provides the Fabric runtime.
- Minecraft GUI sprites, block textures and bitmap font glyphs belong to Mojang/Microsoft. Waypoint icons use 20 original block textures.
- Armor warning artwork: see ARMOR-ASSET-LICENSE.txt.
- Tier/provider/waypoint artwork was supplied by the user. The MCPVP crossed-sword provider mark was processed with ImageGen to remove the flat background and retain a transparent icon; the other provided icons were packed into small bitmap-font assets.

Minecraft and its assets belong to Mojang/Microsoft. This project is not an official Minecraft product.

# Publishing mod updates

The launcher checks this repository's latest stable GitHub release before starting a modded profile. Source commits alone do not update players.

1. Build every supported version and loader using `tools/build-release.py` as documented in README.md.
2. Create a draft release with the mod version as its tag, such as `0.5.0`.
3. Upload the 26.2 Fabric build as `monkeyclient-0-legacy-26.2-fabric.jar`, plus the convenient manual-download alias `monkeyclient-0.5.0.jar`. Launcher 0.6.0 selects the first JAR from the release API. GitHub sorts assets by name, so the `0-legacy` prefix keeps this compatible build ahead of the version-specific builds. Verify the first matching `monkeyclient*.jar` asset and its hash before publishing; upload order is not sufficient.
4. Upload all 15 version/loader JARs and `monkeyclient.json`. The catalog has a `releases` array with `version`, `minecraft`, `loader`, `file`, and `sha256` for each build. Each filename must exactly match a release asset. Use the same catalog and binaries in the launcher's `bundled` directory.
5. Verify all SHA-256 hashes and publish the completed draft as the latest stable release.

Launcher 0.7.0 and later use the catalog to select the exact build. The unqualified legacy JAR is only for 26.2 Fabric and is not a universal Minecraft mod. Forge 26.3 is not included because no official Forge build is available for it. Publish a launcher update with a refreshed bundle when adding a new supported version.
